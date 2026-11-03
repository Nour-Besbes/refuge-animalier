package tn.enicarthage.gestionRefugeAnimalier.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Soin (consultation ou vaccination) realise par un veterinaire sur un animal. */
public class Soin {

    private int id;
    private LocalDate dateSoin;
    private TypeSoin type;
    private String motif;
    private String diagnostic;
    private String traitement;
    private LocalDate prochaineVisite;
    private String remarque;
    private int animalId;
    private int utilisateurId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Soin() {
    }

    public Soin(int id,
            LocalDate dateSoin,
            TypeSoin type,
            String motif,
            String diagnostic,
            String traitement,
            LocalDate prochaineVisite,
            String remarque,
            int animalId,
            int utilisateurId) {
        this.id = id;
        this.dateSoin = dateSoin;
        this.type = type;
        this.motif = motif;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.prochaineVisite = prochaineVisite;
        this.remarque = remarque;
        this.animalId = animalId;
        this.utilisateurId = utilisateurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateSoin() {
        return dateSoin;
    }

    public void setDateSoin(LocalDate dateSoin) {
        this.dateSoin = dateSoin;
    }

    public TypeSoin getType() {
        return type;
    }

    public void setType(TypeSoin type) {
        this.type = type;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getTraitement() {
        return traitement;
    }

    public void setTraitement(String traitement) {
        this.traitement = traitement;
    }

    public LocalDate getProchaineVisite() {
        return prochaineVisite;
    }

    public void setProchaineVisite(LocalDate prochaineVisite) {
        this.prochaineVisite = prochaineVisite;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
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
        return "Soin{id=" + id
                + ", dateSoin=" + dateSoin
                + ", type=" + type
                + ", motif=" + motif
                + ", diagnostic=" + diagnostic
                + ", traitement=" + traitement
                + ", prochaineVisite=" + prochaineVisite
                + ", remarque=" + remarque
                + ", animalId=" + animalId
                + ", utilisateurId=" + utilisateurId
                + '}';
    }
}
