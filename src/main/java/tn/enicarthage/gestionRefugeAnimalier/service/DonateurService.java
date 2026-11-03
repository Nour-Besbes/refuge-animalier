package tn.enicarthage.gestionRefugeAnimalier.service;

import tn.enicarthage.gestionRefugeAnimalier.dao.DonateurDAO;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.model.Donateur;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDonateur;
import tn.enicarthage.gestionRefugeAnimalier.util.ValidationUtil;

import java.util.List;

/**
 * Gestion des donateurs.
 * Regles : nom et type obligatoires ; prenom obligatoire pour un PARTICULIER ;
 * email facultatif mais valide et unique ; un donateur ayant des dons ne peut
 * pas etre supprime (l'historique des dons est conserve).
 */
public class DonateurService {

    private final DonateurDAO donateurDAO;

    public DonateurService() {
        this.donateurDAO = new DonateurDAO();
    }

    public List<Donateur> lister(String recherche, TypeDonateur type) {
        return donateurDAO.rechercher(ValidationUtil.nettoyer(recherche), type);
    }

    public Donateur obtenir(int id) {
        Donateur donateur = donateurDAO.findById(id);
        if (donateur == null) {
            throw new ResourceNotFoundException("Donateur introuvable : " + id);
        }
        return donateur;
    }

    public Donateur creer(Donateur donateur) {
        valider(donateur);
        verifierEmailLibre(donateur.getEmail(), 0);
        return donateurDAO.create(donateur);
    }

    public Donateur modifier(int id, Donateur donateur) {
        obtenir(id);
        donateur.setId(id);
        valider(donateur);
        verifierEmailLibre(donateur.getEmail(), id);
        donateurDAO.update(donateur);
        return obtenir(id);
    }

    public void supprimer(int id) {
        obtenir(id);
        // Cle etrangere don.donateur_id : suppression refusee (409) si des dons existent
        donateurDAO.delete(id);
    }

    private void valider(Donateur donateur) {
        if (donateur == null) {
            throw new IllegalArgumentException("Corps de la requete manquant.");
        }
        donateur.setNom(ValidationUtil.exigerTexte(donateur.getNom(), "nom"));
        ValidationUtil.exigerValeur(donateur.getType(), "type (PARTICULIER, ENTREPRISE, ASSOCIATION)");
        donateur.setPrenom(ValidationUtil.nettoyer(donateur.getPrenom()));
        if (donateur.getType() == TypeDonateur.PARTICULIER && donateur.getPrenom() == null) {
            throw new IllegalArgumentException("Le prenom est obligatoire pour un donateur PARTICULIER.");
        }
        donateur.setEmail(ValidationUtil.verifierEmail(donateur.getEmail(), false));
        donateur.setAdresse(ValidationUtil.nettoyer(donateur.getAdresse()));
        donateur.setTelephone(ValidationUtil.nettoyer(donateur.getTelephone()));
    }

    private void verifierEmailLibre(String email, int idActuel) {
        if (email == null) {
            return;
        }
        Donateur autre = donateurDAO.findByEmail(email);
        if (autre != null && autre.getId() != idActuel) {
            throw new ConflictException("Cet email est deja utilise par un autre donateur : " + email);
        }
    }
}
