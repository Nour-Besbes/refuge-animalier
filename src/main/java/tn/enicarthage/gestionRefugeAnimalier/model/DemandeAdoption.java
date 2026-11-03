package tn.enicarthage.gestionRefugeAnimalier.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Demande d'adoption d'un animal effectuee par un adoptant. */
public class DemandeAdoption {

    private int id;
    private LocalDate dateDemande;
    private String motif;
    private StatutDemande statut;
    private int adoptantId;
    private int animalId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DemandeAdoption() {
    }

    public DemandeAdoption(int id,
            LocalDate dateDemande,
            String motif,
            StatutDemande statut,
            int adoptantId,
            int animalId) {
        this.id = id;
        this.dateDemande = dateDemande;
        this.motif = motif;
        this.statut = statut;
        this.adoptantId = adoptantId;
        this.animalId = animalId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDate dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
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
        return "DemandeAdoption{id=" + id
                + ", dateDemande=" + dateDemande
                + ", motif=" + motif
                + ", statut=" + statut
                + ", adoptantId=" + adoptantId
                + ", animalId=" + animalId
                + '}';
    }
}
