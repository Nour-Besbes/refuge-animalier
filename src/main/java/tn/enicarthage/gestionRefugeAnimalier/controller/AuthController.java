package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.dto.LoginRequest;
import tn.enicarthage.gestionRefugeAnimalier.dto.LoginResponse;
import tn.enicarthage.gestionRefugeAnimalier.dto.UtilisateurResponse;
import tn.enicarthage.gestionRefugeAnimalier.model.Role;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.service.AuthService;

/** Authentification : connexion, deconnexion, utilisateur courant, controle des roles. */
public class AuthController {

    private final AuthService authService = new AuthService();

    public LoginResponse login(LoginRequest request) {
        return authService.login(request);
    }

    public void logout(String token) {
        authService.logout(token);
    }

    public UtilisateurResponse moi(Utilisateur utilisateur) {
        return UtilisateurResponse.depuis(utilisateur);
    }

    /** Renvoie l'utilisateur du jeton s'il a l'un des roles demandes (sinon 401 ou 403). */
    public Utilisateur verifier(String token, Role... rolesAutorises) {
        Utilisateur utilisateur = authService.utilisateurConnecte(token);
        authService.autoriser(utilisateur, rolesAutorises);
        return utilisateur;
    }
}
