package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.config.database.TransactionManager;
import tn.enicarthage.gestionRefugeAnimalier.dao.AdoptionDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.AnimalDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.DemandeAdoptionDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Adoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutDemande;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestion des adoptions.
 * Regles :
 * - une adoption ne peut etre enregistree que si une demande ACCEPTEE existe
 *   pour le meme couple (adoptant, animal) ;
 * - un animal a au plus UNE adoption non annulee (multiplicite 0..1 du diagramme) ;
 * - statut initial EN_COURS ou CONFIRME ; transitions autorisees :
 *   EN_COURS -> CONFIRME | ANNULE, CONFIRME -> ANNULE ; ANNULE est definitif ;
 * - le statut de l'animal suit l'adoption (dans la meme transaction) :
 *   EN_COURS -> EN_ATTENTE_ADOPTION, CONFIRME -> ADOPTE, ANNULE -> DISPONIBLE ;
 * - l'adoptant et l'animal d'une adoption ne peuvent pas etre changes.
 */
public class AdoptionService {

    private final AdoptionDAO adoptionDAO;
    private final AnimalDAO animalDAO;
    private final DemandeAdoptionDAO demandeDAO;
    private final AnimalService animalService;
    private final AdoptantService adoptantService;

    public AdoptionService() {
        this.adoptionDAO = new AdoptionDAO();
        this.animalDAO = new AnimalDAO();
        this.demandeDAO = new DemandeAdoptionDAO();
        this.animalService = new AnimalService();
        this.adoptantService = new AdoptantService();
    }

    public List<Adoption> lister(String recherche, StatutAdoption statut, Integer adoptantId, Integer animalId) {
        return adoptionDAO.rechercher(ValidationUtil.nettoyer(recherche), statut, adoptantId, animalId);
    }

    public Adoption obtenir(int id) {
        Adoption adoption = adoptionDAO.findById(id);
        if (adoption == null) {
            throw new ResourceNotFoundException("Adoption introuvable : " + id);
        }
        return adoption;
    }

    public Adoption enregistrer(Adoption adoption) {
        if (adoption == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        ValidationUtil.exigerId(adoption.getAdoptantId(), "adoptantId");
        ValidationUtil.exigerId(adoption.getAnimalId(), "animalId");
        adoptantService.obtenir(adoption.getAdoptantId());
        animalService.obtenir(adoption.getAnimalId());

        if (adoptionDAO.findActiveByAnimalId(adoption.getAnimalId()) != null) {
            throw new ConflictException("Cet animal a deja une adoption en cours ou confirmee.");
        }
        if (!demandeDAO.existe(adoption.getAdoptantId(), adoption.getAnimalId(), StatutDemande.ACCEPTEE)) {
            throw new ConflictException("Aucune demande d'adoption ACCEPTEE pour cet adoptant et cet animal.");
        }
        if (adoption.getStatut() == null) {
            adoption.setStatut(StatutAdoption.EN_COURS);
        }
        if (adoption.getStatut() == StatutAdoption.ANNULE) {
            throw new IllegalArgumentException("Une adoption ne peut pas etre creee avec le statut ANNULE.");
        }
        validerChamps(adoption);

        TransactionManager.executer(connection -> {
            adoptionDAO.create(connection, adoption);
            animalDAO.updateStatut(connection, adoption.getAnimalId(), statutAnimalPour(adoption.getStatut()));
            return null;
        });
        return obtenir(adoption.getId());
    }

    public Adoption modifier(int id, Adoption modifications) {
        if (modifications == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        Adoption existante = obtenir(id);
        verifierInchange(modifications.getAdoptantId(), existante.getAdoptantId(), "adoptantId");
        verifierInchange(modifications.getAnimalId(), existante.getAnimalId(), "animalId");

        StatutAdoption nouveauStatut = modifications.getStatut() == null
                ? existante.getStatut() : modifications.getStatut();
        verifierTransition(existante.getStatut(), nouveauStatut);

        boolean statutChange = nouveauStatut != existante.getStatut();
        existante.setStatut(nouveauStatut);
        existante.setCommentaire(modifications.getCommentaire());
        if (modifications.getDateAdoption() != null) {
            existante.setDateAdoption(modifications.getDateAdoption());
        }
        validerChamps(existante);

        TransactionManager.executer(connection -> {
            adoptionDAO.update(connection, existante);
            // Le statut de l'animal ne suit que si le statut de l'adoption change
            if (statutChange) {
                animalDAO.updateStatut(connection, existante.getAnimalId(), statutAnimalPour(nouveauStatut));
            }
            return null;
        });
        return obtenir(id);
    }

    private void validerChamps(Adoption adoption) {
        if (adoption.getDateAdoption() == null) {
            adoption.setDateAdoption(LocalDate.now());
        }
        ValidationUtil.verifierPasDansLeFutur(adoption.getDateAdoption(), "dateAdoption");
        adoption.setCommentaire(ValidationUtil.nettoyer(adoption.getCommentaire()));
    }

    private void verifierInchange(int valeurRecue, int valeurActuelle, String champ) {
        // 0 = champ absent du JSON : accepte
        if (valeurRecue != 0 && valeurRecue != valeurActuelle) {
            throw new IllegalArgumentException("Le champ '" + champ + "' d'une adoption ne peut pas etre modifie.");
        }
    }

    private void verifierTransition(StatutAdoption actuel, StatutAdoption nouveau) {
        if (actuel == nouveau) {
            return;
        }
        boolean autorisee = (actuel == StatutAdoption.EN_COURS
                && (nouveau == StatutAdoption.CONFIRME || nouveau == StatutAdoption.ANNULE))
                || (actuel == StatutAdoption.CONFIRME && nouveau == StatutAdoption.ANNULE);
        if (!autorisee) {
            throw new ConflictException("Transition de statut interdite : " + actuel + " -> " + nouveau + ".");
        }
    }

    private StatutAnimal statutAnimalPour(StatutAdoption statut) {
        switch (statut) {
            case CONFIRME:
                return StatutAnimal.ADOPTE;
            case ANNULE:
                return StatutAnimal.DISPONIBLE;
            default:
                return StatutAnimal.EN_ATTENTE_ADOPTION;
        }
    }
}
