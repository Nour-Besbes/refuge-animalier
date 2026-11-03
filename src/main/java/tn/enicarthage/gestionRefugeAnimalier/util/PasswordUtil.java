package tn.enicarthage.gestionRefugeAnimalier.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hachage des mots de passe avec PBKDF2 (inclus dans le JDK, aucune dependance).
 * Format stocke en base : pbkdf2$iterations$sel(base64)$hash(base64)
 * Le mot de passe en clair n'est jamais enregistre.
 */
public final class PasswordUtil {

    private static final String ALGORITHME = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int TAILLE_CLE = 256;
    private static final int TAILLE_SEL = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hacher(String motDePasse) {
        byte[] sel = new byte[TAILLE_SEL];
        RANDOM.nextBytes(sel);
        byte[] hash = calculer(motDePasse.toCharArray(), sel, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(sel) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifier(String motDePasse, String hashStocke) {
        if (motDePasse == null || hashStocke == null) {
            return false;
        }
        String[] parties = hashStocke.split("\\$");
        if (parties.length != 4 || !"pbkdf2".equals(parties[0])) {
            return false;
        }
        int iterations = Integer.parseInt(parties[1]);
        byte[] sel = Base64.getDecoder().decode(parties[2]);
        byte[] attendu = Base64.getDecoder().decode(parties[3]);
        byte[] calcule = calculer(motDePasse.toCharArray(), sel, iterations);
        return MessageDigest.isEqual(attendu, calcule);
    }

    private static byte[] calculer(char[] motDePasse, byte[] sel, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(motDePasse, sel, iterations, TAILLE_CLE);
            return SecretKeyFactory.getInstance(ALGORITHME).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Erreur de hachage du mot de passe", e);
        }
    }

    /** Petit outil : java PasswordUtil monMotDePasse -> affiche le hash a coller dans database.sql */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(hacher(args[0]));
        } else {
            System.out.println("Usage : PasswordUtil <motDePasse>");
        }
    }
}
