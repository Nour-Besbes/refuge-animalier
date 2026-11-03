package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.Don;
import tn.enicarthage.gestionRefugeAnimalier.model.Donateur;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDonateur;
import tn.enicarthage.gestionRefugeAnimalier.service.DonService;
import tn.enicarthage.gestionRefugeAnimalier.service.DonateurService;

import java.util.List;

/** Gestion des donateurs (ajout, modification, suppression, consultation, recherche). */
public class DonateurController {

    private final DonateurService donateurService = new DonateurService();
    private final DonService donService = new DonService();

    public List<Donateur> lister(String recherche, TypeDonateur type) {
        return donateurService.lister(recherche, type);
    }

    public Donateur obtenir(int id) {
        return donateurService.obtenir(id);
    }

    public Donateur creer(Donateur donateur) {
        return donateurService.creer(donateur);
    }

    public Donateur modifier(int id, Donateur donateur) {
        return donateurService.modifier(id, donateur);
    }

    public void supprimer(int id) {
        donateurService.supprimer(id);
    }

    public List<Don> dons(int id) {
        return donService.listerParDonateur(id);
    }
}
