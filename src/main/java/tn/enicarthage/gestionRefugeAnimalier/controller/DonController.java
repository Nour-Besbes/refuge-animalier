package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.Don;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDon;
import tn.enicarthage.gestionRefugeAnimalier.service.DonService;

import java.util.List;

/** Gestion des dons. */
public class DonController {

    private final DonService donService = new DonService();

    public List<Don> lister(TypeDon type, Integer donateurId) {
        return donService.lister(type, donateurId);
    }

    public Don obtenir(int id) {
        return donService.obtenir(id);
    }

    public Don creer(Don don) {
        return donService.creer(don);
    }

    public Don modifier(int id, Don don) {
        return donService.modifier(id, don);
    }

    public void supprimer(int id) {
        donService.supprimer(id);
    }
}
