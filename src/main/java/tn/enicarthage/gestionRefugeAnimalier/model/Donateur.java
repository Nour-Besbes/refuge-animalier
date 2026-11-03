package tn.enicarthage.gestionRefugeAnimalier.model;

import java.time.LocalDateTime;

/** Personne, entreprise ou association qui fait des dons au refuge. */
public class Donateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    private TypeDonateur type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Donateur() {
    }

    public Donateur(int id,
            String nom,
            String prenom,
            String email,
            String adresse,
            String telephone,
            TypeDonateur type) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
        this.type = type;
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

    public TypeDonateur getType() {
        return type;
    }

    public void setType(TypeDonateur type) {
        this.type = type;
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
        return "Donateur{id=" + id
                + ", nom=" + nom
                + ", prenom=" + prenom
                + ", email=" + email
                + ", adresse=" + adresse
                + ", telephone=" + telephone
                + ", type=" + type
                + '}';
    }
}
