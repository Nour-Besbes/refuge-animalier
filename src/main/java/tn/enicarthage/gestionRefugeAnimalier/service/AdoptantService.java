package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.AdoptantDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Adoptant;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestion des adoptants.
 * Regles : nom, prenom, email (unique, valide) et telephone obligatoires ;
 * date d'inscription par defaut aujourd'hui, jamais dans le futur.
 */
public class AdoptantService {

    private final AdoptantDAO adoptantDAO;

    public AdoptantService() {
        this.adoptantDAO = new AdoptantDAO();
    }

    public List<Adoptant> lister(String recherche) {
        return adoptantDAO.rechercher(ValidationUtil.nettoyer(recherche));
    }

    public Adoptant obtenir(int id) {
        Adoptant adoptant = adoptantDAO.findById(id);
        if (adoptant == null) {
            throw new ResourceNotFoundException("Adoptant introuvable : " + id);
        }
        return adoptant;
    }

    public Adoptant creer(Adoptant adoptant) {
        valider(adoptant);
        verifierEmailLibre(adoptant.getEmail(), 0);
        return adoptantDAO.create(adoptant);
    }

    public Adoptant modifier(int id, Adoptant adoptant) {
        Adoptant existant = obtenir(id);
        adoptant.setId(id);
        if (adoptant.getDateInscription() == null) {
            adoptant.setDateInscription(existant.getDateInscription());
        }
        valider(adoptant);
        verifierEmailLibre(adoptant.getEmail(), id);
        adoptantDAO.update(adoptant);
        return obtenir(id);
    }

    public void supprimer(int id) {
        obtenir(id);
        // Un adoptant ayant des demandes ou adoptions est protege par les cles etrangeres (409)
        adoptantDAO.delete(id);
    }

    private void valider(Adoptant adoptant) {
        if (adoptant == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        adoptant.setNom(ValidationUtil.exigerTexte(adoptant.getNom(), "nom"));
        adoptant.setPrenom(ValidationUtil.exigerTexte(adoptant.getPrenom(), "prenom"));
        adoptant.setEmail(ValidationUtil.verifierEmail(adoptant.getEmail(), true));
        adoptant.setTelephone(ValidationUtil.exigerTexte(adoptant.getTelephone(), "telephone"));
        adoptant.setAdresse(ValidationUtil.nettoyer(adoptant.getAdresse()));
        if (adoptant.getDateInscription() == null) {
            adoptant.setDateInscription(LocalDate.now());
        }
        ValidationUtil.verifierPasDansLeFutur(adoptant.getDateInscription(), "dateInscription");
    }

    private void verifierEmailLibre(String email, int idActuel) {
        Adoptant autre = adoptantDAO.findByEmail(email);
        if (autre != null && autre.getId() != idActuel) {
            throw new ConflictException("Cet email est deja utilise par un autre adoptant : " + email);
        }
    }
}
