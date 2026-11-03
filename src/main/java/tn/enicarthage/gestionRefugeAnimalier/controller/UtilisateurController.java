package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.dto.UtilisateurRequest;
import tn.enicarthage.gestionRefugeAnimalier.dto.UtilisateurResponse;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.service.UtilisateurService;

import java.util.List;
import java.util.stream.Collectors;

/** Gestion des utilisateurs (administrateur). Convertit les modeles en UtilisateurResponse. */
public class UtilisateurController {

    private final UtilisateurService utilisateurService = new UtilisateurService();

    public List<UtilisateurResponse> lister() {
        return utilisateurService.lister().stream()
                .map(UtilisateurResponse::depuis)
                .collect(Collectors.toList());
    }

    public UtilisateurResponse obtenir(int id) {
        return UtilisateurResponse.depuis(utilisateurService.obtenir(id));
    }

    public UtilisateurResponse creer(UtilisateurRequest request) {
        return UtilisateurResponse.depuis(utilisateurService.creer(request));
    }

    public UtilisateurResponse modifier(int id, UtilisateurRequest request) {
        return UtilisateurResponse.depuis(utilisateurService.modifier(id, request));
    }

    public void supprimer(int id, Utilisateur utilisateurConnecte) {
        utilisateurService.supprimer(id, utilisateurConnecte);
    }
}
