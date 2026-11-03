package tn.enicarthage.gestionRefugeAnimalier.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Verifications simples reutilisees par tous les services.
 * Chaque erreur leve une IllegalArgumentException, traduite en HTTP 400 par le Router.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private ValidationUtil() {
    }

    /** Supprime les espaces en debut/fin ; renvoie null si la chaine est vide. */
    public static String nettoyer(String valeur) {
        if (valeur == null) {
            return null;
        }
        String resultat = valeur.trim();
        return resultat.isEmpty() ? null : resultat;
    }

    public static String exigerTexte(String valeur, String champ) {
        String resultat = nettoyer(valeur);
        if (resultat == null) {
            throw new IllegalArgumentException("Le champ '" + champ + "' est obligatoire.");
        }
        return resultat;
    }

    public static <T> T exigerValeur(T valeur, String champ) {
        if (valeur == null) {
            throw new IllegalArgumentException("Le champ '" + champ + "' est obligatoire ou invalide.");
        }
        return valeur;
    }

    public static void exigerId(int id, String champ) {
        if (id <= 0) {
            throw new IllegalArgumentException("Le champ '" + champ + "' est obligatoire (identifiant > 0).");
        }
    }

    /** Verifie le format d'un email ; renvoie l'email nettoye (ou null s'il est facultatif et vide). */
    public static String verifierEmail(String email, boolean obligatoire) {
        String resultat = obligatoire ? exigerTexte(email, "email") : nettoyer(email);
        if (resultat != null && !EMAIL.matcher(resultat).matches()) {
            throw new IllegalArgumentException("Format d'email invalide : " + resultat);
        }
        return resultat == null ? null : resultat.toLowerCase();
    }

    public static void verifierPasDansLeFutur(LocalDate date, String champ) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Le champ '" + champ + "' ne peut pas etre dans le futur.");
        }
    }
}
