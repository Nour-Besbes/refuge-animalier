package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.DemandeAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutDemande;
import tn.enicarthage.gestionRefugeAnimalier.service.DemandeAdoptionService;

import java.util.List;

/** Gestion des demandes d'adoption (enregistrer, accepter, refuser, consulter). */
public class DemandeAdoptionController {

    private final DemandeAdoptionService demandeService = new DemandeAdoptionService();

    public List<DemandeAdoption> lister(StatutDemande statut, Integer adoptantId, Integer animalId) {
        return demandeService.lister(statut, adoptantId, animalId);
    }

    public DemandeAdoption obtenir(int id) {
        return demandeService.obtenir(id);
    }

    public DemandeAdoption enregistrer(DemandeAdoption demande) {
        return demandeService.enregistrer(demande);
    }

    public DemandeAdoption accepter(int id) {
        return demandeService.accepter(id);
    }

    public DemandeAdoption refuser(int id) {
        return demandeService.refuser(id);
    }
}
