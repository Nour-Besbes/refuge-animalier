package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.UtilisateurDAO;
import tn.enicarthage.gestionRefugeAnimalier.dto.LoginRequest;
import tn.enicarthage.gestionRefugeAnimalier.dto.LoginResponse;
import tn.enicarthage.gestionRefugeAnimalier.dto.UtilisateurResponse;
import tn.enicarthage.gestionRefugeAnimalier.exception.ForbiddenException;
import tn.enicarthage.gestionRefugeAnimalier.exception.UnauthorizedException;
import tn.enicarthage.gestionRefugeAnimalier.model.Role;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.util.PasswordUtil;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentification par jeton (token) aleatoire conserve EN MEMOIRE.
 * Limites assumees (projet pedagogique) : les sessions sont perdues au redemarrage
 * du serveur et ne sont pas partagees entre plusieurs serveurs. Pas de HTTPS en local.
 */
public class AuthService {

    private static final Duration DUREE_SESSION = Duration.ofHours(8);
    private static final SecureRandom RANDOM = new SecureRandom();

    // Partage par toutes les instances : token -> session
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private record Session(int utilisateurId, Instant expiration) {
    }

    private final UtilisateurDAO utilisateurDAO;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Email et mot de passe obligatoires.");
        }
        String email = ValidationUtil.exigerTexte(request.getEmail(), "email").toLowerCase();
        ValidationUtil.exigerTexte(request.getMotDePasse(), "motDePasse");

        Utilisateur utilisateur = utilisateurDAO.findByEmail(email);
        // Meme message dans les deux cas : on ne revele pas si l'email existe
        if (utilisateur == null || !PasswordUtil.verifier(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect.");
        }

        String token = genererToken();
        SESSIONS.put(token, new Session(utilisateur.getId(), Instant.now().plus(DUREE_SESSION)));
        return new LoginResponse(token, UtilisateurResponse.depuis(utilisateur));
    }

    public void logout(String token) {
        if (token != null) {
            SESSIONS.remove(token);
        }
    }

    /** Renvoie l'utilisateur associe au jeton, recharge depuis la base (role a jour). */
    public Utilisateur utilisateurConnecte(String token) {
        if (token == null) {
            throw new UnauthorizedException("Authentification requise (en-tete Authorization: Bearer <token>).");
        }
        Session session = SESSIONS.get(token);
        if (session == null || session.expiration().isBefore(Instant.now())) {
            SESSIONS.remove(token);
            throw new UnauthorizedException("Session invalide ou expiree. Reconnectez-vous.");
        }
        Utilisateur utilisateur = utilisateurDAO.findById(session.utilisateurId());
        if (utilisateur == null) {
            SESSIONS.remove(token);
            throw new UnauthorizedException("Utilisateur de la session introuvable.");
        }
        return utilisateur;
    }

    /** Verifie que l'utilisateur possede l'un des roles autorises, sinon 403. */
    public void autoriser(Utilisateur utilisateur, Role... rolesAutorises) {
        if (Arrays.stream(rolesAutorises).noneMatch(role -> role == utilisateur.getRole())) {
            throw new ForbiddenException("Acces refuse pour le role " + utilisateur.getRole() + ".");
        }
    }

    private String genererToken() {
        byte[] octets = new byte[32];
        RANDOM.nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }
}
