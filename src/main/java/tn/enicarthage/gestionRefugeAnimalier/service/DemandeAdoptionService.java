package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.config.database.TransactionManager;
import tn.enicarthage.gestionRefugeAnimalier.dao.AnimalDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.DemandeAdoptionDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Animal;
import tn.enicarthage.gestionRefugeAnimalier.model.DemandeAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutDemande;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestion des demandes d'adoption.
 * Regles :
 * - une demande est toujours creee EN_ATTENTE, pour un animal DISPONIBLE ou deja EN_ATTENTE_ADOPTION ;
 * - un adoptant ne peut pas avoir deux demandes EN_ATTENTE pour le meme animal ;
 * - a l'enregistrement, l'animal passe EN_ATTENTE_ADOPTION ;
 * - seule une demande EN_ATTENTE peut etre acceptee ou refusee ;
 * - accepter une demande refuse automatiquement les autres demandes EN_ATTENTE du meme animal ;
 * - refuser la derniere demande en attente (sans demande acceptee) remet l'animal DISPONIBLE.
 */
public class DemandeAdoptionService {

    private final DemandeAdoptionDAO demandeDAO;
    private final AnimalDAO animalDAO;
    private final AnimalService animalService;
    private final AdoptantService adoptantService;

    public DemandeAdoptionService() {
        this.demandeDAO = new DemandeAdoptionDAO();
        this.animalDAO = new AnimalDAO();
        this.animalService = new AnimalService();
        this.adoptantService = new AdoptantService();
    }

    public List<DemandeAdoption> lister(StatutDemande statut, Integer adoptantId, Integer animalId) {
        return demandeDAO.rechercher(statut, adoptantId, animalId);
    }

    public DemandeAdoption obtenir(int id) {
        DemandeAdoption demande = demandeDAO.findById(id);
        if (demande == null) {
            throw new ResourceNotFoundException("Demande d'adoption introuvable : " + id);
        }
        return demande;
    }

    public DemandeAdoption enregistrer(DemandeAdoption demande) {
        if (demande == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        ValidationUtil.exigerId(demande.getAdoptantId(), "adoptantId");
        ValidationUtil.exigerId(demande.getAnimalId(), "animalId");
        adoptantService.obtenir(demande.getAdoptantId());
        Animal animal = animalService.obtenir(demande.getAnimalId());

        if (animal.getStatut() != StatutAnimal.DISPONIBLE && animal.getStatut() != StatutAnimal.EN_ATTENTE_ADOPTION) {
            throw new ConflictException("L'animal " + animal.getNom() + " n'est pas adoptable (statut "
                    + animal.getStatut() + ").");
        }
        if (demandeDAO.existe(demande.getAdoptantId(), demande.getAnimalId(), StatutDemande.EN_ATTENTE)) {
            throw new ConflictException("Cet adoptant a deja une demande en attente pour cet animal.");
        }
        if (demande.getDateDemande() == null) {
            demande.setDateDemande(LocalDate.now());
        }
        ValidationUtil.verifierPasDansLeFutur(demande.getDateDemande(), "dateDemande");
        demande.setMotif(ValidationUtil.nettoyer(demande.getMotif()));
        demande.setStatut(StatutDemande.EN_ATTENTE);

        // Transaction : la demande ET le statut de l'animal
        TransactionManager.executer(connection -> {
            demandeDAO.create(connection, demande);
            animalDAO.updateStatut(connection, demande.getAnimalId(), StatutAnimal.EN_ATTENTE_ADOPTION);
            return null;
        });
        return obtenir(demande.getId());
    }

    public DemandeAdoption accepter(int id) {
        DemandeAdoption demande = exigerEnAttente(id);
        Animal animal = animalService.obtenir(demande.getAnimalId());
        if (animal.getStatut() == StatutAnimal.ADOPTE) {
            throw new ConflictException("L'animal " + animal.getNom() + " est deja adopte.");
        }
        TransactionManager.executer(connection -> {
            if (demandeDAO.compterParAnimalEtStatut(connection, demande.getAnimalId(), StatutDemande.ACCEPTEE) > 0) {
                throw new ConflictException("Une autre demande a deja ete acceptee pour cet animal.");
            }
            demandeDAO.updateStatut(connection, id, StatutDemande.ACCEPTEE);
            demandeDAO.refuserAutresDemandesEnAttente(connection, demande.getAnimalId(), id);
            animalDAO.updateStatut(connection, demande.getAnimalId(), StatutAnimal.EN_ATTENTE_ADOPTION);
            return null;
        });
        return obtenir(id);
    }

    public DemandeAdoption refuser(int id) {
        DemandeAdoption demande = exigerEnAttente(id);
        TransactionManager.executer(connection -> {
            demandeDAO.updateStatut(connection, id, StatutDemande.REFUSEE);
            int enAttente = demandeDAO.compterParAnimalEtStatut(connection, demande.getAnimalId(), StatutDemande.EN_ATTENTE);
            int acceptees = demandeDAO.compterParAnimalEtStatut(connection, demande.getAnimalId(), StatutDemande.ACCEPTEE);
            Animal animal = animalDAO.findById(demande.getAnimalId());
            if (enAttente == 0 && acceptees == 0 && animal.getStatut() == StatutAnimal.EN_ATTENTE_ADOPTION) {
                animalDAO.updateStatut(connection, demande.getAnimalId(), StatutAnimal.DISPONIBLE);
            }
            return null;
        });
        return obtenir(id);
    }

    private DemandeAdoption exigerEnAttente(int id) {
        DemandeAdoption demande = obtenir(id);
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new ConflictException("Seule une demande EN_ATTENTE peut etre traitee (statut actuel : "
                    + demande.getStatut() + ").");
        }
        return demande;
    }
}
