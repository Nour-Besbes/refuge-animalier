package tn.enicarthage.gestionRefugeAnimalier.controller;

import tn.enicarthage.gestionRefugeAnimalier.dto.DashboardResponse;
import tn.enicarthage.gestionRefugeAnimalier.service.DashboardService;

/** Tableau de bord de l'administrateur. */
public class DashboardController {

    private final DashboardService dashboardService = new DashboardService();

    public DashboardResponse consulter() {
        return dashboardService.calculer();
    }
}
