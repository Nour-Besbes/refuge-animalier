package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.Adoptant;
import tn.enicarthage.gestionRefugeAnimalier.service.AdoptantService;

import java.util.List;

/** Gestion des adoptants (ajout, modification, suppression, consultation, recherche). */
public class AdoptantController {

    private final AdoptantService adoptantService = new AdoptantService();

    public List<Adoptant> lister(String recherche) {
        return adoptantService.lister(recherche);
    }

    public Adoptant obtenir(int id) {
        return adoptantService.obtenir(id);
    }

    public Adoptant creer(Adoptant adoptant) {
        return adoptantService.creer(adoptant);
    }

    public Adoptant modifier(int id, Adoptant adoptant) {
        return adoptantService.modifier(id, adoptant);
    }

    public void supprimer(int id) {
        adoptantService.supprimer(id);
    }
}
