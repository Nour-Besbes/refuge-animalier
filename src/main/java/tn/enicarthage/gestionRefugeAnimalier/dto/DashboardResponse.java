package tn.enicarthage.gestionRefugeAnimalier.dto;

/** Indicateurs du tableau de bord de l'administrateur (GET /dashboard). */
public class DashboardResponse {

    private int nombreTotalAnimaux;
    private int nombreAnimauxDisponibles;
    private int nombreAnimauxAdoptes;
    private int nombreAnimauxEnSoins;
    private int nombreDemandesEnAttente;
    private int nombreAdoptionsRealisees;
    private int nombreDonateurs;
    private int nombreUtilisateurs;

    public DashboardResponse() {
    }

    public DashboardResponse(int nombreTotalAnimaux, int nombreAnimauxDisponibles, int nombreAnimauxAdoptes,
                             int nombreAnimauxEnSoins, int nombreDemandesEnAttente,
                             int nombreAdoptionsRealisees, int nombreDonateurs, int nombreUtilisateurs) {
        this.nombreTotalAnimaux = nombreTotalAnimaux;
        this.nombreAnimauxDisponibles = nombreAnimauxDisponibles;
        this.nombreAnimauxAdoptes = nombreAnimauxAdoptes;
        this.nombreAnimauxEnSoins = nombreAnimauxEnSoins;
        this.nombreDemandesEnAttente = nombreDemandesEnAttente;
        this.nombreAdoptionsRealisees = nombreAdoptionsRealisees;
        this.nombreDonateurs = nombreDonateurs;
        this.nombreUtilisateurs = nombreUtilisateurs;
    }

    public int getNombreTotalAnimaux() {
        return nombreTotalAnimaux;
    }

    public int getNombreAnimauxDisponibles() {
        return nombreAnimauxDisponibles;
    }

    public int getNombreAnimauxAdoptes() {
        return nombreAnimauxAdoptes;
    }

    public int getNombreAnimauxEnSoins() {
        return nombreAnimauxEnSoins;
    }

    public int getNombreDemandesEnAttente() {
        return nombreDemandesEnAttente;
    }

    public int getNombreAdoptionsRealisees() {
        return nombreAdoptionsRealisees;
    }

    public int getNombreDonateurs() {
        return nombreDonateurs;
    }

    public int getNombreUtilisateurs() {
        return nombreUtilisateurs;
    }
}
