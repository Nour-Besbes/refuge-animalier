package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.DonDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Don;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDon;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Gestion des dons.
 * Regles : donateur existant et type obligatoires ; pour un don en ARGENT le montant
 * est obligatoire et strictement positif ; pour les autres types il est facultatif
 * (valeur estimee) mais jamais negatif ; date par defaut aujourd'hui, jamais dans le futur.
 */
public class DonService {

    private final DonDAO donDAO;
    private final DonateurService donateurService;

    public DonService() {
        this.donDAO = new DonDAO();
        this.donateurService = new DonateurService();
    }

    public List<Don> lister(TypeDon type, Integer donateurId) {
        return donDAO.rechercher(type, donateurId);
    }

    public List<Don> listerParDonateur(int donateurId) {
        donateurService.obtenir(donateurId);
        return donDAO.findByDonateurId(donateurId);
    }

    public Don obtenir(int id) {
        Don don = donDAO.findById(id);
        if (don == null) {
            throw new ResourceNotFoundException("Don introuvable : " + id);
        }
        return don;
    }

    public Don creer(Don don) {
        valider(don);
        return donDAO.create(don);
    }

    public Don modifier(int id, Don don) {
        obtenir(id);
        don.setId(id);
        valider(don);
        donDAO.update(don);
        return obtenir(id);
    }

    public void supprimer(int id) {
        obtenir(id);
        donDAO.delete(id);
    }

    private void valider(Don don) {
        if (don == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        ValidationUtil.exigerId(don.getDonateurId(), "donateurId");
        donateurService.obtenir(don.getDonateurId());
        ValidationUtil.exigerValeur(don.getTypeDon(), "typeDon (ARGENT, NOURRITURE, MEDICAMENT, MATERIEL, AUTRE)");

        BigDecimal montant = don.getMontant();
        if (don.getTypeDon() == TypeDon.ARGENT && (montant == null || montant.signum() <= 0)) {
            throw new IllegalArgumentException("Un don en ARGENT doit avoir un montant strictement positif.");
        }
        if (montant != null && montant.signum() < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas etre negatif.");
        }
        if (don.getDateDon() == null) {
            don.setDateDon(LocalDate.now());
        }
        ValidationUtil.verifierPasDansLeFutur(don.getDateDon(), "dateDon");
        don.setDescription(ValidationUtil.nettoyer(don.getDescription()));
    }
}
