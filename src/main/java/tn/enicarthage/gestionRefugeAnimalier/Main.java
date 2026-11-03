package tn.enicarthage.gestionRefugeAnimalier;

import java.io.IOException;
import java.sql.Connection;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;

/** Point d'entree : verifie d'abord la connexion MySQL, puis demarre le serveur sur le port 8080. */
public class Main {

    private static final int PORT = 8686;

    public static void main(String[] args) {
        if (!testerConnexion()) {
            System.out.println("Verifiez que MySQL est demarre et que database.sql est execute.");
            return;
        }

        try {
            ApiServer serveur = new ApiServer(PORT);
            serveur.demarrer();
            System.out.println("Serveur HTTP demarre sur http://localhost:" + PORT);
            System.out.println("Testez avec Postman : POST http://localhost:" + PORT + "/auth/login");
        } catch (IOException e) {
            System.out.println("Impossible de demarrer le serveur : " + e.getMessage());
        }
    }

    private static boolean testerConnexion() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connexion reussie a MySQL.");
            return true;
        } catch (Exception e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
            return false;
        }
    }
}
