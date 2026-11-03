package tn.enicarthage.gestionRefugeAnimalier.dto;

import tn.enicarthage.gestionRefugeAnimalier.model.Role;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;

import java.time.LocalDateTime;

/** Utilisateur renvoye au frontend : le mot de passe (meme hache) n'est JAMAIS expose. */
public class UtilisateurResponse {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UtilisateurResponse() {
    }

    public static UtilisateurResponse depuis(Utilisateur utilisateur) {
        UtilisateurResponse response = new UtilisateurResponse();
        response.id = utilisateur.getId();
        response.nom = utilisateur.getNom();
        response.prenom = utilisateur.getPrenom();
        response.email = utilisateur.getEmail();
        response.telephone = utilisateur.getTelephone();
        response.role = utilisateur.getRole();
        response.createdAt = utilisateur.getCreatedAt();
        response.updatedAt = utilisateur.getUpdatedAt();
        return response;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
