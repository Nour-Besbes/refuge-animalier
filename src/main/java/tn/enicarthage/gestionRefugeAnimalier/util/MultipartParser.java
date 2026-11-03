package tn.enicarthage.gestionRefugeAnimalier.util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class MultipartParser {

    /** Un fichier recu dans le formulaire. */
    public record Fichier(String nomOriginal, String typeContenu, byte[] contenu) {
    }

    /** Resultat du decodage : champs texte + fichiers, indexes par leur "name". */
    public record Formulaire(Map<String, String> champs, Map<String, Fichier> fichiers) {

        public String champ(String nom) {
            return champs.get(nom);
        }

        public Fichier fichier(String nom) {
            return fichiers.get(nom);
        }
    }

    private static final Pattern BOUNDARY = Pattern.compile("boundary=(\"?)([^\";]+)\\1");
    private static final Pattern NAME = Pattern.compile("\\bname=\"([^\"]*)\"");
    private static final Pattern FILENAME = Pattern.compile("\\bfilename=\"([^\"]*)\"");
    private static final byte[] FIN_EN_TETES = "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);

    private MultipartParser() {
    }

    public static boolean estMultipart(String contentType) {
        return contentType != null && contentType.toLowerCase().startsWith("multipart/form-data");
    }

    public static Formulaire decoder(byte[] corps, String contentType) {
        Matcher m = BOUNDARY.matcher(contentType == null ? "" : contentType);
        if (!m.find()) {
            throw new IllegalArgumentException("En-tete multipart/form-data sans boundary.");
        }
        byte[] separateur = ("--" + m.group(2)).getBytes(StandardCharsets.ISO_8859_1);

        Map<String, String> champs = new HashMap<>();
        Map<String, Fichier> fichiers = new HashMap<>();

        int position = indexOf(corps, separateur, 0);
        if (position < 0) {
            throw new IllegalArgumentException("Corps multipart invalide (boundary introuvable).");
        }
        while (true) {
            int debutPartie = position + separateur.length;
            // "--" juste apres le separateur = fin du formulaire
            if (debutPartie + 1 < corps.length && corps[debutPartie] == '-' && corps[debutPartie + 1] == '-') {
                break;
            }
            debutPartie = sauterRetourLigne(corps, debutPartie);
            int finEnTetes = indexOf(corps, FIN_EN_TETES, debutPartie);
            if (finEnTetes < 0) {
                throw new IllegalArgumentException("Corps multipart invalide (en-tetes de partie).");
            }
            int prochain = indexOf(corps, separateur, finEnTetes);
            if (prochain < 0) {
                throw new IllegalArgumentException("Corps multipart invalide (partie non terminee).");
            }
            // Le contenu s'arrete avant le "\r\n" qui precede le separateur suivant
            int debutContenu = finEnTetes + FIN_EN_TETES.length;
            int finContenu = prochain;
            if (finContenu - 2 >= debutContenu && corps[finContenu - 2] == '\r' && corps[finContenu - 1] == '\n') {
                finContenu -= 2;
            }

            String enTetes = new String(corps, debutPartie, finEnTetes - debutPartie, StandardCharsets.UTF_8);
            ajouterPartie(enTetes, extraire(corps, debutContenu, finContenu), champs, fichiers);
            position = prochain;
        }
        return new Formulaire(champs, fichiers);
    }

    private static void ajouterPartie(String enTetes, byte[] contenu,
                                      Map<String, String> champs, Map<String, Fichier> fichiers) {
        String disposition = null;
        String typeContenu = null;
        for (String ligne : enTetes.split("\r\n")) {
            String minuscule = ligne.toLowerCase();
            if (minuscule.startsWith("content-disposition:")) {
                disposition = ligne;
            } else if (minuscule.startsWith("content-type:")) {
                typeContenu = ligne.substring("content-type:".length()).trim();
            }
        }
        if (disposition == null) {
            return; // partie sans nom : ignoree
        }
        Matcher nom = NAME.matcher(disposition);
        if (!nom.find()) {
            return;
        }
        Matcher nomFichier = FILENAME.matcher(disposition);
        if (nomFichier.find()) {
            // Champ fichier laisse vide dans le formulaire : filename="" et aucun octet
            if (contenu.length > 0) {
                fichiers.put(nom.group(1), new Fichier(nomFichier.group(1), typeContenu, contenu));
            }
        } else {
            champs.put(nom.group(1), new String(contenu, StandardCharsets.UTF_8));
        }
    }

    private static int sauterRetourLigne(byte[] corps, int position) {
        if (position + 1 < corps.length && corps[position] == '\r' && corps[position + 1] == '\n') {
            return position + 2;
        }
        return position;
    }

    private static byte[] extraire(byte[] source, int debut, int fin) {
        byte[] resultat = new byte[Math.max(0, fin - debut)];
        System.arraycopy(source, debut, resultat, 0, resultat.length);
        return resultat;
    }

    /** Recherche d'une sequence d'octets (equivalent de String.indexOf pour byte[]). */
    private static int indexOf(byte[] source, byte[] motif, int depart) {
        externe:
        for (int i = Math.max(0, depart); i <= source.length - motif.length; i++) {
            for (int j = 0; j < motif.length; j++) {
                if (source[i + j] != motif[j]) {
                    continue externe;
                }
            }
            return i;
        }
        return -1;
    }
}