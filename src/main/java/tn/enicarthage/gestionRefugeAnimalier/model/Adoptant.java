package tn.enicarthage.gestionRefugeAnimalier.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Personne qui souhaite adopter ou qui a adopte un animal. */
public class Adoptant {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    private LocalDate dateInscription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Adoptant() {
    }

    public Adoptant(int id,
            String nom,
            String prenom,
            String email,
            String adresse,
            String telephone,
            LocalDate dateInscription) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
        this.dateInscription = dateInscription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Adoptant{id=" + id
                + ", nom=" + nom
                + ", prenom=" + prenom
                + ", email=" + email
                + ", adresse=" + adresse
                + ", telephone=" + telephone
                + ", dateInscription=" + dateInscription
                + '}';
    }
}
