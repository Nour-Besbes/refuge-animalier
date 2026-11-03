package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.model.Animal;
import tn.enicarthage.gestionRefugeAnimalier.model.Espece;
import tn.enicarthage.gestionRefugeAnimalier.model.Soin;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.service.AnimalService;
import tn.enicarthage.gestionRefugeAnimalier.service.SoinService;

import java.util.List;

/** Gestion des animaux et consultation de l'historique medical. */
public class AnimalController {

    private final AnimalService animalService = new AnimalService();
    private final SoinService soinService = new SoinService();

    public List<Animal> lister(String nom, Espece espece, StatutAnimal statut) {
        return animalService.lister(nom, espece, statut);
    }

    public Animal obtenir(int id) {
        return animalService.obtenir(id);
    }

    public Animal creer(Animal animal) {
        return animalService.creer(animal);
    }

    public Animal modifier(int id, Animal animal) {
        return animalService.modifier(id, animal);
    }

    public void supprimer(int id) {
        animalService.supprimer(id);
    }

    public Animal changerPhoto(int id, byte[] contenu) {
        return animalService.changerPhoto(id, contenu);
    }

    public Animal supprimerPhoto(int id) {
        return animalService.supprimerPhoto(id);
    }

    public byte[] image(String nomFichier) {
        return animalService.lireImage(nomFichier);
    }

    public List<Soin> historiqueMedical(int id) {
        return soinService.historique(id);
    }
}
