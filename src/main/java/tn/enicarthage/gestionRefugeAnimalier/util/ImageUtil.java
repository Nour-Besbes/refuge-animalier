package tn.enicarthage.gestionRefugeAnimalier.util;

import tn.enicarthage.gestionRefugeAnimalier.exception.DataAccessException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stockage des photos sur le disque (dossier uploads/animaux par defaut,
 * modifiable avec la variable d'environnement UPLOAD_DIR).
 * La base de donnees ne contient que le NOM du fichier (colonne animal.photo).
 *
 * Securite :
 * - le format est detecte a partir des premiers octets du fichier (signature),
 *   pas a partir du nom ou de l'en-tete Content-Type, qui peuvent etre falsifies ;
 * - le nom est genere par le serveur (UUID) : impossible d'ecrire hors du dossier ;
 * - en lecture, seuls les noms au format genere sont acceptes (pas de "../").
 */
public final class ImageUtil {

    public static final int TAILLE_MAX = 5 * 1024 * 1024; // 5 Mo

    private static final Path DOSSIER = Paths.get(
            System.getenv("UPLOAD_DIR") == null ? "uploads/animaux" : System.getenv("UPLOAD_DIR"));
    private static final Pattern NOM_VALIDE = Pattern.compile("^[a-f0-9\\-]{36}\\.(jpg|png|webp)$");

    private ImageUtil() {
    }

    /** Renvoie "jpg", "png" ou "webp" selon la signature du fichier, sinon null. */
    public static String detecterExtension(byte[] contenu) {
        if (contenu.length >= 3 && (contenu[0] & 0xFF) == 0xFF && (contenu[1] & 0xFF) == 0xD8
                && (contenu[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (contenu.length >= 8 && (contenu[0] & 0xFF) == 0x89 && contenu[1] == 'P' && contenu[2] == 'N'
                && contenu[3] == 'G') {
            return "png";
        }
        if (contenu.length >= 12 && contenu[0] == 'R' && contenu[1] == 'I' && contenu[2] == 'F' && contenu[3] == 'F'
                && contenu[8] == 'W' && contenu[9] == 'E' && contenu[10] == 'B' && contenu[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /** Enregistre l'image sous un nom unique et renvoie ce nom. */
    public static String enregistrer(byte[] contenu, String extension) {
        String nom = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(DOSSIER);
            Files.write(DOSSIER.resolve(nom), contenu);
            return nom;
        } catch (IOException e) {
            throw new DataAccessException("Impossible d'enregistrer l'image : " + e.getMessage(), e);
        }
    }

    public static byte[] lire(String nom) {
        if (nom == null || !NOM_VALIDE.matcher(nom).matches()) {
            throw new ResourceNotFoundException("Image introuvable : " + nom);
        }
        Path fichier = DOSSIER.resolve(nom);
        if (!Files.exists(fichier)) {
            throw new ResourceNotFoundException("Image introuvable : " + nom);
        }
        try {
            return Files.readAllBytes(fichier);
        } catch (IOException e) {
            throw new DataAccessException("Impossible de lire l'image : " + e.getMessage(), e);
        }
    }

    /** Supprime l'image si elle existe (aucune erreur si elle est deja absente). */
    public static void supprimer(String nom) {
        if (nom == null || !NOM_VALIDE.matcher(nom).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(DOSSIER.resolve(nom));
        } catch (IOException e) {
            System.err.println("Image non supprimee (" + nom + ") : " + e.getMessage());
        }
    }

    public static String typeMime(String nom) {
        if (nom.endsWith(".png")) {
            return "image/png";
        }
        if (nom.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
