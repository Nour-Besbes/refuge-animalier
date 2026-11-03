package tn.enicarthage.gestionRefugeAnimalier.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Adoption d'un animal par un adoptant (0..1 adoption par animal). */
public class Adoption {

    private int id;
    private LocalDate dateAdoption;
    private StatutAdoption statut;
    private String commentaire;
    private int adoptantId;
    private int animalId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Adoption() {
    }

    public Adoption(int id,
            LocalDate dateAdoption,
            StatutAdoption statut,
            String commentaire,
            int adoptantId,
            int animalId) {
        this.id = id;
        this.dateAdoption = dateAdoption;
        this.statut = statut;
        this.commentaire = commentaire;
        this.adoptantId = adoptantId;
        this.animalId = animalId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateAdoption() {
        return dateAdoption;
    }

    public void setDateAdoption(LocalDate dateAdoption) {
        this.dateAdoption = dateAdoption;
    }

    public StatutAdoption getStatut() {
        return statut;
    }

    public void setStatut(StatutAdoption statut) {
        this.statut = statut;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getAdoptantId() {
        return adoptantId;
    }

    public void setAdoptantId(int adoptantId) {
        this.adoptantId = adoptantId;
    }

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
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
        return "Adoption{id=" + id
                + ", dateAdoption=" + dateAdoption
                + ", statut=" + statut
                + ", commentaire=" + commentaire
                + ", adoptantId=" + adoptantId
                + ", animalId=" + animalId
                + '}';
    }
}
