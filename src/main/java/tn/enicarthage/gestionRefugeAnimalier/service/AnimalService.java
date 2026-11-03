package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.AnimalDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Animal;
import tn.enicarthage.gestionRefugeAnimalier.model.Espece;
import tn.enicarthage.gestionRefugeAnimalier.model.EtatSante;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.util.ImageUtil;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Gestion des animaux.
 * Regles :
 * - nom, espece et sexe obligatoires ; dates ni dans le futur, naissance <= arrivee ;
 * - statut par defaut DISPONIBLE, etat de sante par defaut BON, arrivee par defaut aujourd'hui ;
 * - les statuts EN_ATTENTE_ADOPTION et ADOPTE sont geres UNIQUEMENT par le processus
 *   demandes/adoptions : ils ne peuvent pas etre choisis ni quittes a la main ;
 * - la photo n'est jamais modifiee par le JSON : elle passe par changerPhoto / supprimerPhoto
 *   (image JPEG, PNG ou WEBP de 5 Mo maximum).
 */
public class AnimalService {

    private static final Set<StatutAnimal> STATUTS_MANUELS =
            EnumSet.of(StatutAnimal.DISPONIBLE, StatutAnimal.EN_SOIN, StatutAnimal.INDISPONIBLE);

    private final AnimalDAO animalDAO;

    public AnimalService() {
        this.animalDAO = new AnimalDAO();
    }

    public List<Animal> lister(String nom, Espece espece, StatutAnimal statut) {
        return animalDAO.rechercher(ValidationUtil.nettoyer(nom), espece, statut);
    }

    public Animal obtenir(int id) {
        Animal animal = animalDAO.findById(id);
        if (animal == null) {
            throw new ResourceNotFoundException("Animal introuvable : " + id);
        }
        return animal;
    }

    public Animal creer(Animal animal) {
        valider(animal);
        animal.setPhoto(null); // la photo est envoyee ensuite via PUT /animaux/{id}/photo
        if (animal.getStatut() == null) {
            animal.setStatut(StatutAnimal.DISPONIBLE);
        }
        if (!STATUTS_MANUELS.contains(animal.getStatut())) {
            throw new IllegalArgumentException("Statut initial autorise : DISPONIBLE, EN_SOIN ou INDISPONIBLE.");
        }
        return animalDAO.create(animal);
    }

    public Animal modifier(int id, Animal animal) {
        Animal existant = obtenir(id);
        animal.setId(id);
        valider(animal);
        animal.setPhoto(existant.getPhoto()); // la photo ne change pas via le JSON
        if (animal.getStatut() == null) {
            animal.setStatut(existant.getStatut());
        }
        if (animal.getStatut() != existant.getStatut()
                && (!STATUTS_MANUELS.contains(existant.getStatut()) || !STATUTS_MANUELS.contains(animal.getStatut()))) {
            throw new ConflictException("Changement de statut " + existant.getStatut() + " -> " + animal.getStatut()
                    + " interdit : les statuts EN_ATTENTE_ADOPTION et ADOPTE sont geres par les demandes et adoptions.");
        }
        animalDAO.update(animal);
        return obtenir(id);
    }

    public void supprimer(int id) {
        Animal animal = obtenir(id);
        if (animal.getStatut() == StatutAnimal.ADOPTE || animal.getStatut() == StatutAnimal.EN_ATTENTE_ADOPTION) {
            throw new ConflictException("Impossible de supprimer un animal adopte ou en attente d'adoption.");
        }
        // Si l'animal possede des soins/demandes/adoptions, la cle etrangere bloque (409)
        animalDAO.delete(id);
        ImageUtil.supprimer(animal.getPhoto()); // le fichier n'est supprime qu'apres la ligne SQL
    }

    /** Ajoute ou remplace la photo de l'animal ; l'ancienne image est supprimee du disque. */
    public Animal changerPhoto(int id, byte[] contenu) {
        Animal animal = obtenir(id);
        if (contenu == null || contenu.length == 0) {
            throw new IllegalArgumentException("Aucune image recue.");
        }
        if (contenu.length > ImageUtil.TAILLE_MAX) {
            throw new IllegalArgumentException("Image trop volumineuse (5 Mo maximum).");
        }
        String extension = ImageUtil.detecterExtension(contenu);
        if (extension == null) {
            throw new IllegalArgumentException("Format non supporte : seules les images JPEG, PNG et WEBP sont acceptees.");
        }

        String nouveauNom = ImageUtil.enregistrer(contenu, extension);
        try {
            animalDAO.updatePhoto(id, nouveauNom);
        } catch (RuntimeException e) {
            ImageUtil.supprimer(nouveauNom); // pas de fichier orphelin si la base echoue
            throw e;
        }
        ImageUtil.supprimer(animal.getPhoto());
        return obtenir(id);
    }

    public Animal supprimerPhoto(int id) {
        Animal animal = obtenir(id);
        if (animal.getPhoto() != null) {
            animalDAO.updatePhoto(id, null);
            ImageUtil.supprimer(animal.getPhoto());
        }
        return obtenir(id);
    }

    /** Contenu d'une image (servie publiquement pour les balises <img>). */
    public byte[] lireImage(String nomFichier) {
        return ImageUtil.lire(nomFichier);
    }

    private void valider(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        animal.setNom(ValidationUtil.exigerTexte(animal.getNom(), "nom"));
        ValidationUtil.exigerValeur(animal.getEspece(), "espece (CHAT, CHIEN, AUTRE)");
        ValidationUtil.exigerValeur(animal.getSexe(), "sexe (MALE, FEMELLE)");
        animal.setRace(ValidationUtil.nettoyer(animal.getRace()));
        animal.setDescription(ValidationUtil.nettoyer(animal.getDescription()));
        if (animal.getEtatSante() == null) {
            animal.setEtatSante(EtatSante.BON);
        }
        if (animal.getDateArrivee() == null) {
            animal.setDateArrivee(LocalDate.now());
        }
        ValidationUtil.verifierPasDansLeFutur(animal.getDateArrivee(), "dateArrivee");
        ValidationUtil.verifierPasDansLeFutur(animal.getDateNaissance(), "dateNaissance");
        if (animal.getDateNaissance() != null && animal.getDateNaissance().isAfter(animal.getDateArrivee())) {
            throw new IllegalArgumentException("La date de naissance doit preceder la date d'arrivee.");
        }
    }
}
