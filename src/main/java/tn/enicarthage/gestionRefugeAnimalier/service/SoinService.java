package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.SoinDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Soin;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeSoin;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestion des soins par le veterinaire et historique medical.
 * Regles :
 * - animal existant et type (CONSULTATION / VACCINATION) obligatoires ;
 * - date du soin par defaut aujourd'hui, jamais dans le futur ;
 * - prochaine visite, si renseignee, posterieure a la date du soin ;
 * - le veterinaire auteur du soin est l'utilisateur connecte (jamais choisi dans le JSON).
 */
public class SoinService {

    private final SoinDAO soinDAO;
    private final AnimalService animalService;

    public SoinService() {
        this.soinDAO = new SoinDAO();
        this.animalService = new AnimalService();
    }

    public List<Soin> lister(TypeSoin type, Integer animalId) {
        return soinDAO.rechercher(type, animalId);
    }

    /** Historique medical : verifie d'abord que l'animal existe (404 sinon). */
    public List<Soin> historique(int animalId) {
        animalService.obtenir(animalId);
        return soinDAO.findByAnimalId(animalId);
    }

    public Soin obtenir(int id) {
        Soin soin = soinDAO.findById(id);
        if (soin == null) {
            throw new ResourceNotFoundException("Soin introuvable : " + id);
        }
        return soin;
    }

    public Soin creer(Soin soin, Utilisateur veterinaire) {
        valider(soin);
        soin.setUtilisateurId(veterinaire.getId());
        return soinDAO.create(soin);
    }

    public Soin modifier(int id, Soin soin) {
        Soin existant = obtenir(id);
        soin.setId(id);
        valider(soin);
        soin.setUtilisateurId(existant.getUtilisateurId());
        soinDAO.update(soin);
        return obtenir(id);
    }

    public void supprimer(int id) {
        obtenir(id);
        soinDAO.delete(id);
    }

    private void valider(Soin soin) {
        if (soin == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        ValidationUtil.exigerId(soin.getAnimalId(), "animalId");
        animalService.obtenir(soin.getAnimalId());
        ValidationUtil.exigerValeur(soin.getType(), "type (CONSULTATION, VACCINATION)");
        if (soin.getDateSoin() == null) {
            soin.setDateSoin(LocalDate.now());
        }
        ValidationUtil.verifierPasDansLeFutur(soin.getDateSoin(), "dateSoin");
        if (soin.getProchaineVisite() != null && !soin.getProchaineVisite().isAfter(soin.getDateSoin())) {
            throw new IllegalArgumentException("La prochaine visite doit etre posterieure a la date du soin.");
        }
        soin.setMotif(ValidationUtil.nettoyer(soin.getMotif()));
        soin.setDiagnostic(ValidationUtil.nettoyer(soin.getDiagnostic()));
        soin.setTraitement(ValidationUtil.nettoyer(soin.getTraitement()));
        soin.setRemarque(ValidationUtil.nettoyer(soin.getRemarque()));
    }
}
