package tn.enicarthage.gestionRefugeAnimalier.config.database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe utilitaire unique qui fournit les connexions MySQL (getConnection)
 * et qui sait creer la base + les tables depuis le script database.sql.
 *
 * Les valeurs par defaut correspondent a WAMP (root, mot de passe vide, port 3306).
 * Elles peuvent etre remplacees, sans modifier le code, par des variables
 * d'environnement : DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD.
 * Ainsi, aucun mot de passe reel n'est ecrit dans le depot Git.
 */
public final class DatabaseConnection {

    private static final String HOST = lire("DB_HOST", "localhost");
    private static final int PORT = Integer.parseInt(lire("DB_PORT", "3306"));
    private static final String DATABASE = lire("DB_NAME", "gestion_refuge_animalier");
    private static final String USER = lire("DB_USER", "root");
    private static final String PASSWORD = lire("DB_PASSWORD", "");
    private static final String OPTIONS =
            "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                    + "&allowMultiQueries=true";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + OPTIONS;
    private static final String SERVER_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + OPTIONS;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Lance ce main une seule fois (ou apres chaque modification de database.sql)
     * pour creer la base, les tables et les donnees de test.
     */
    public static void main(String[] args) {

        Path databaseScript = Paths.get(
                "src", "main", "resources", "database.sql"
        );

        try {
            initializeDatabase(Files.readString(databaseScript, StandardCharsets.UTF_8));
            System.out.println("DB + tables creees avec succes.");
        } catch (IOException | SQLException e) {
            System.err.println("Erreur d'initialisation de la base : " + e.getMessage());
            System.exit(1);
        }
    }

    private static void initializeDatabase(String databaseSql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            String database = "`" + DATABASE.replace("`", "``") + "`";
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            statement.execute("USE " + database);
            statement.execute(databaseSql);
        }
    }

    // Lit une variable d'environnement, sinon renvoie la valeur par defaut
    private static String lire(String nom, String valeurParDefaut) {
        String valeur = System.getenv(nom);
        return (valeur == null || valeur.isBlank()) ? valeurParDefaut : valeur;
    }
}
