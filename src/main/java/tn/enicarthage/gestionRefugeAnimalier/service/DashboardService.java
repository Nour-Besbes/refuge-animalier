package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.AdoptionDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.AnimalDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.DemandeAdoptionDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.DonateurDAO;
import tn.enicarthage.gestionRefugeAnimalier.dao.UtilisateurDAO;
import tn.enicarthage.gestionRefugeAnimalier.dto.DashboardResponse;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutDemande;

/**
 * Calcule les indicateurs du tableau de bord.
 * "Adoptions realisees" = adoptions au statut CONFIRME.
 */
public class DashboardService {

    private final AnimalDAO animalDAO;
    private final DemandeAdoptionDAO demandeDAO;
    private final AdoptionDAO adoptionDAO;
    private final DonateurDAO donateurDAO;
    private final UtilisateurDAO utilisateurDAO;

    public DashboardService() {
        this.animalDAO = new AnimalDAO();
        this.demandeDAO = new DemandeAdoptionDAO();
        this.adoptionDAO = new AdoptionDAO();
        this.donateurDAO = new DonateurDAO();
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public DashboardResponse calculer() {
        return new DashboardResponse(
                animalDAO.compterTous(),
                animalDAO.compterParStatut(StatutAnimal.DISPONIBLE),
                animalDAO.compterParStatut(StatutAnimal.ADOPTE),
                animalDAO.compterParStatut(StatutAnimal.EN_SOIN),
                demandeDAO.compterParStatut(StatutDemande.EN_ATTENTE),
                adoptionDAO.compterParStatut(StatutAdoption.CONFIRME),
                donateurDAO.compterTous(),
                utilisateurDAO.compterTous()
        );
    }
}
