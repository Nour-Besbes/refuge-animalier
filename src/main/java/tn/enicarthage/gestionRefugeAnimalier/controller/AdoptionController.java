package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.Adoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAdoption;
import tn.enicarthage.gestionRefugeAnimalier.service.AdoptionService;

import java.util.List;

/** Gestion des adoptions (enregistrer, consulter, rechercher, modifier). */
public class AdoptionController {

    private final AdoptionService adoptionService = new AdoptionService();

    public List<Adoption> lister(String recherche, StatutAdoption statut, Integer adoptantId, Integer animalId) {
        return adoptionService.lister(recherche, statut, adoptantId, animalId);
    }

    public Adoption obtenir(int id) {
        return adoptionService.obtenir(id);
    }

    public Adoption enregistrer(Adoption adoption) {
        return adoptionService.enregistrer(adoption);
    }

    public Adoption modifier(int id, Adoption adoption) {
        return adoptionService.modifier(id, adoption);
    }
}
