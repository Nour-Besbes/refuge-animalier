package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.Soin;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeSoin;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.service.SoinService;

import java.util.List;

/** Gestion des soins (veterinaire). */
public class SoinController {

    private final SoinService soinService = new SoinService();

    public List<Soin> lister(TypeSoin type, Integer animalId) {
        return soinService.lister(type, animalId);
    }

    public Soin obtenir(int id) {
        return soinService.obtenir(id);
    }

    public Soin creer(Soin soin, Utilisateur veterinaire) {
        return soinService.creer(soin, veterinaire);
    }

    public Soin modifier(int id, Soin soin) {
        return soinService.modifier(id, soin);
    }

    public void supprimer(int id) {
        soinService.supprimer(id);
    }
}
