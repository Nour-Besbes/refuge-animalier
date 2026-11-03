package tn.enicarthage.gestionRefugeAnimalier.dto;

import tn.enicarthage.gestionRefugeAnimalier.model.Role;

/**
 * Corps JSON recu pour creer ou modifier un utilisateur.
 * motDePasse est en clair ici : il est hache par UtilisateurService avant l'enregistrement.
 * En modification, un motDePasse vide signifie "ne pas changer le mot de passe".
 */
public class UtilisateurRequest {

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private Role role;

    public UtilisateurRequest() {
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
