package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.UtilisateurDAO;
import tn.enicarthage.gestionRefugeAnimalier.dto.UtilisateurRequest;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Role;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.util.PasswordUtil;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.util.List;

/**
 * Gestion des utilisateurs par l'administrateur (employes et veterinaires).
 * Regles :
 * - l'administrateur cree uniquement des EMPLOYE ou VETERINAIRE ;
 * - email unique et valide, mot de passe d'au moins 6 caracteres (stocke hache) ;
 * - un administrateur ne peut pas etre supprime, ni changer de role ;
 * - on ne peut pas se supprimer soi-meme.
 */
public class UtilisateurService {

    private static final int LONGUEUR_MIN_MOT_DE_PASSE = 6;

    private final UtilisateurDAO utilisateurDAO;

    public UtilisateurService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public List<Utilisateur> lister() {
        return utilisateurDAO.findAll();
    }

    public Utilisateur obtenir(int id) {
        Utilisateur utilisateur = utilisateurDAO.findById(id);
        if (utilisateur == null) {
            throw new ResourceNotFoundException("Utilisateur introuvable : " + id);
        }
        return utilisateur;
    }

    public Utilisateur creer(UtilisateurRequest request) {
        exigerCorps(request);
        Utilisateur utilisateur = new Utilisateur();
        remplir(utilisateur, request);

        Role role = ValidationUtil.exigerValeur(request.getRole(), "role");
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("L'administrateur ne peut creer que des EMPLOYE ou VETERINAIRE.");
        }
        utilisateur.setRole(role);
        utilisateur.setMotDePasse(hacherMotDePasse(request.getMotDePasse()));
        verifierEmailLibre(utilisateur.getEmail(), 0);
        return utilisateurDAO.create(utilisateur);
    }

    public Utilisateur modifier(int id, UtilisateurRequest request) {
        exigerCorps(request);
        Utilisateur existant = obtenir(id);
        remplir(existant, request);

        Role nouveauRole = ValidationUtil.exigerValeur(request.getRole(), "role");
        if (existant.getRole() == Role.ADMIN && nouveauRole != Role.ADMIN) {
            throw new ConflictException("Le role d'un administrateur ne peut pas etre modifie.");
        }
        if (existant.getRole() != Role.ADMIN && nouveauRole == Role.ADMIN) {
            throw new IllegalArgumentException("Un utilisateur ne peut pas etre promu ADMIN.");
        }
        existant.setRole(nouveauRole);

        // Mot de passe facultatif en modification : vide = inchange
        if (ValidationUtil.nettoyer(request.getMotDePasse()) != null) {
            existant.setMotDePasse(hacherMotDePasse(request.getMotDePasse()));
        }
        verifierEmailLibre(existant.getEmail(), id);
        utilisateurDAO.update(existant);
        return obtenir(id);
    }

    public void supprimer(int id, Utilisateur utilisateurConnecte) {
        Utilisateur utilisateur = obtenir(id);
        if (utilisateur.getId() == utilisateurConnecte.getId()) {
            throw new ConflictException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        if (utilisateur.getRole() == Role.ADMIN) {
            throw new ConflictException("Un administrateur ne peut pas etre supprime.");
        }
        utilisateurDAO.delete(id);
    }

    private void remplir(Utilisateur utilisateur, UtilisateurRequest request) {
        utilisateur.setNom(ValidationUtil.exigerTexte(request.getNom(), "nom"));
        utilisateur.setPrenom(ValidationUtil.exigerTexte(request.getPrenom(), "prenom"));
        utilisateur.setEmail(ValidationUtil.verifierEmail(request.getEmail(), true));
        utilisateur.setTelephone(ValidationUtil.nettoyer(request.getTelephone()));
    }

    private String hacherMotDePasse(String motDePasse) {
        String valeur = ValidationUtil.exigerTexte(motDePasse, "motDePasse");
        if (valeur.length() < LONGUEUR_MIN_MOT_DE_PASSE) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins "
                    + LONGUEUR_MIN_MOT_DE_PASSE + " caracteres.");
        }
        return PasswordUtil.hacher(valeur);
    }

    private void verifierEmailLibre(String email, int idActuel) {
        Utilisateur autre = utilisateurDAO.findByEmail(email);
        if (autre != null && autre.getId() != idActuel) {
            throw new ConflictException("Cet email est deja utilise : " + email);
        }
    }

    private void exigerCorps(Object corps) {
        if (corps == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
    }
}
