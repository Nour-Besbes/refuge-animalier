package tn.enicarthage.gestionRefugeAnimalier.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Animal accueilli par le refuge. */
public class Animal {

    private int id;
    private String nom;
    private Espece espece;
    private String race;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String description;
    private EtatSante etatSante;
    private StatutAnimal statut;
    private LocalDate dateArrivee;
    private String photo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Animal() {
    }

    public Animal(int id,
            String nom,
            Espece espece,
            String race,
            LocalDate dateNaissance,
            Sexe sexe,
            String description,
            EtatSante etatSante,
            StatutAnimal statut,
            LocalDate dateArrivee,
            String photo) {
        this.id = id;
        this.nom = nom;
        this.espece = espece;
        this.race = race;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.description = description;
        this.etatSante = etatSante;
        this.statut = statut;
        this.dateArrivee = dateArrivee;
        this.photo = photo;
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

    public Espece getEspece() {
        return espece;
    }

    public void setEspece(Espece espece) {
        this.espece = espece;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EtatSante getEtatSante() {
        return etatSante;
    }

    public void setEtatSante(EtatSante etatSante) {
        this.etatSante = etatSante;
    }

    public StatutAnimal getStatut() {
        return statut;
    }

    public void setStatut(StatutAnimal statut) {
        this.statut = statut;
    }

    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDate dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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
        return "Animal{id=" + id
                + ", nom=" + nom
                + ", espece=" + espece
                + ", race=" + race
                + ", dateNaissance=" + dateNaissance
                + ", sexe=" + sexe
                + ", description=" + description
                + ", etatSante=" + etatSante
                + ", statut=" + statut
                + ", dateArrivee=" + dateArrivee
                + ", photo=" + photo
                + '}';
    }
}
