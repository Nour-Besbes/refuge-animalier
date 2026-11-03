package tn.enicarthage.gestionRefugeAnimalier.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Don (argent, nourriture, medicament, materiel, autre) fait par un donateur. */
public class Don {

    private int id;
    private LocalDate dateDon;
    private TypeDon typeDon;
    private BigDecimal montant;
    private String description;
    private int donateurId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Don() {
    }

    public Don(int id,
            LocalDate dateDon,
            TypeDon typeDon,
            BigDecimal montant,
            String description,
            int donateurId) {
        this.id = id;
        this.dateDon = dateDon;
        this.typeDon = typeDon;
        this.montant = montant;
        this.description = description;
        this.donateurId = donateurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateDon() {
        return dateDon;
    }

    public void setDateDon(LocalDate dateDon) {
        this.dateDon = dateDon;
    }

    public TypeDon getTypeDon() {
        return typeDon;
    }

    public void setTypeDon(TypeDon typeDon) {
        this.typeDon = typeDon;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDonateurId() {
        return donateurId;
    }

    public void setDonateurId(int donateurId) {
        this.donateurId = donateurId;
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
        return "Don{id=" + id
                + ", dateDon=" + dateDon
                + ", typeDon=" + typeDon
                + ", montant=" + montant
                + ", description=" + description
                + ", donateurId=" + donateurId
                + '}';
    }
}
