package tn.enicarthage.gestionRefugeAnimalier.router;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import tn.enicarthage.gestionRefugeAnimalier.controller.AdoptantController;
import tn.enicarthage.gestionRefugeAnimalier.controller.AdoptionController;
import tn.enicarthage.gestionRefugeAnimalier.controller.AnimalController;
import tn.enicarthage.gestionRefugeAnimalier.controller.AuthController;
import tn.enicarthage.gestionRefugeAnimalier.controller.DashboardController;
import tn.enicarthage.gestionRefugeAnimalier.controller.DemandeAdoptionController;
import tn.enicarthage.gestionRefugeAnimalier.controller.DonController;
import tn.enicarthage.gestionRefugeAnimalier.controller.DonateurController;
import tn.enicarthage.gestionRefugeAnimalier.controller.SoinController;
import tn.enicarthage.gestionRefugeAnimalier.controller.UtilisateurController;
import tn.enicarthage.gestionRefugeAnimalier.dto.LoginRequest;
import tn.enicarthage.gestionRefugeAnimalier.dto.UtilisateurRequest;
import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ForbiddenException;
import tn.enicarthage.gestionRefugeAnimalier.exception.ResourceNotFoundException;
import tn.enicarthage.gestionRefugeAnimalier.exception.UnauthorizedException;
import tn.enicarthage.gestionRefugeAnimalier.model.Adoptant;
import tn.enicarthage.gestionRefugeAnimalier.model.Adoption;
import tn.enicarthage.gestionRefugeAnimalier.model.Animal;
import tn.enicarthage.gestionRefugeAnimalier.model.DemandeAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.Don;
import tn.enicarthage.gestionRefugeAnimalier.model.Donateur;
import tn.enicarthage.gestionRefugeAnimalier.model.Espece;
import tn.enicarthage.gestionRefugeAnimalier.model.Role;
import tn.enicarthage.gestionRefugeAnimalier.model.Soin;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutDemande;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDon;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDonateur;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeSoin;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.util.ImageUtil;
import tn.enicarthage.gestionRefugeAnimalier.util.MultipartParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Pivot entre le serveur HTTP et les controleurs (template du guide, adapte au refuge) :
 * associe URL + methode HTTP au controleur, convertit le JSON avec Gson,
 * verifie le role de l'utilisateur connecte et traduit les exceptions en codes HTTP.
 */
public class Router {

    // Represente une reponse HTTP (statut + corps JSON)
    private record Reponse(int statut, String corps) {
    }

    // Groupes de roles issus du diagramme de cas d'utilisation
    private static final Role[] ADMIN = {Role.ADMIN};
    private static final Role[] PERSONNEL = {Role.ADMIN, Role.EMPLOYE};
    private static final Role[] TOUS = {Role.ADMIN, Role.EMPLOYE, Role.VETERINAIRE};
    private static final Role[] VETERINAIRE = {Role.VETERINAIRE};

    // Origine autorisee pour React (CORS) : variable d'environnement CORS_ORIGIN, sinon toutes
    private static final String CORS_ORIGIN =
            System.getenv("CORS_ORIGIN") == null ? "*" : System.getenv("CORS_ORIGIN");

    // Gson configure pour gerer LocalDate / LocalDateTime au format ISO ("2026-10-20")
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> {
                        try {
                            return json.isJsonNull() ? null : LocalDate.parse(json.getAsString());
                        } catch (DateTimeParseException e) {
                            throw new JsonParseException("Date invalide (format attendu AAAA-MM-JJ) : "
                                    + json.getAsString());
                        }
                    })
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                            json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString()))
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    // Controleurs (instancies directement ici)
    private final AuthController authController = new AuthController();
    private final UtilisateurController utilisateurController = new UtilisateurController();
    private final AnimalController animalController = new AnimalController();
    private final AdoptantController adoptantController = new AdoptantController();
    private final DemandeAdoptionController demandeController = new DemandeAdoptionController();
    private final AdoptionController adoptionController = new AdoptionController();
    private final SoinController soinController = new SoinController();
    private final DonateurController donateurController = new DonateurController();
    private final DonController donController = new DonController();
    private final DashboardController dashboardController = new DashboardController();

    // Point d'entree unique pour toutes les requetes HTTP
    public void dispatcher(HttpExchange exchange) throws IOException {
        ajouterEntetesCors(exchange.getResponseHeaders());
        String methode = exchange.getRequestMethod();

        // Requete "preflight" envoyee automatiquement par le navigateur avant un appel React
        if ("OPTIONS".equals(methode)) {
            envoyer(exchange, 204, "");
            return;
        }

        String chemin = exchange.getRequestURI().getPath();
        String[] segments = chemin.split("/");

        try {
            // Images : reponse binaire (et non JSON), publique pour fonctionner dans <img src="...">
            if (segments.length == 3 && "images".equals(segments[1]) && "GET".equals(methode)) {
                envoyerImage(exchange, segments[2]);
                return;
            }
            Reponse reponse = router(methode, segments, exchange);
            envoyer(exchange, reponse.statut(), reponse.corps());
        } catch (ResourceNotFoundException e) {
            envoyer(exchange, 404, messageErreur(e.getMessage()));
        } catch (UnauthorizedException e) {
            envoyer(exchange, 401, messageErreur(e.getMessage()));
        } catch (ForbiddenException e) {
            envoyer(exchange, 403, messageErreur(e.getMessage()));
        } catch (ConflictException e) {
            envoyer(exchange, 409, messageErreur(e.getMessage()));
        } catch (JsonParseException e) {
            envoyer(exchange, 400, messageErreur("JSON invalide : " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            envoyer(exchange, 400, messageErreur(e.getMessage()));
        } catch (Exception e) {
            // Detail technique dans la console du serveur uniquement (pas expose au client)
            System.err.println("Erreur interne sur " + methode + " " + chemin + " : " + e.getMessage());
            e.printStackTrace();
            envoyer(exchange, 500, messageErreur("Erreur interne du serveur."));
        }
    }

    // Dispatch par premiere ressource (/animaux, /adoptants, ...)
    private Reponse router(String methode, String[] segments, HttpExchange exchange) throws IOException {
        if (segments.length < 2 || segments[1].isBlank()) {
            throw new ResourceNotFoundException("Route vide ou inconnue.");
        }

        switch (segments[1]) {
            case "auth":
                return routesAuth(methode, segments, exchange);
            case "utilisateurs":
                return routesUtilisateur(methode, segments, exchange);
            case "animaux":
                return routesAnimal(methode, segments, exchange);
            case "adoptants":
                return routesAdoptant(methode, segments, exchange);
            case "demandes":
                return routesDemande(methode, segments, exchange);
            case "adoptions":
                return routesAdoption(methode, segments, exchange);
            case "soins":
                return routesSoin(methode, segments, exchange);
            case "donateurs":
                return routesDonateur(methode, segments, exchange);
            case "dons":
                return routesDon(methode, segments, exchange);
            case "dashboard":
                return routesDashboard(methode, segments, exchange);
            default:
                throw new ResourceNotFoundException("Route inconnue : /" + segments[1]);
        }
    }

    // ---------------------------------------------------------
    // POST /auth/login   (public)
    // POST /auth/logout  (connecte)
    // GET  /auth/moi     (connecte)
    // ---------------------------------------------------------
    private Reponse routesAuth(String methode, String[] segments, HttpExchange exchange) throws IOException {
        if (segments.length == 3) {
            String action = segments[2];
            if ("login".equals(action) && "POST".equals(methode)) {
                LoginRequest request = lireCorps(exchange, LoginRequest.class);
                return new Reponse(200, gson.toJson(authController.login(request)));
            }
            if ("logout".equals(action) && "POST".equals(methode)) {
                authController.logout(token(exchange));
                return new Reponse(204, "");
            }
            if ("moi".equals(action) && "GET".equals(methode)) {
                Utilisateur utilisateur = authController.verifier(token(exchange), TOUS);
                return new Reponse(200, gson.toJson(authController.moi(utilisateur)));
            }
        }
        throw new ResourceNotFoundException("Route inconnue : " + methode + " " + String.join("/", segments));
    }

    // ---------------------------------------------------------
    // /utilisateurs  et  /utilisateurs/{id}         [ADMIN]
    // ---------------------------------------------------------
    private Reponse routesUtilisateur(String methode, String[] segments, HttpExchange exchange) throws IOException {
        Utilisateur connecte = authController.verifier(token(exchange), ADMIN);
        if (segments.length == 2) {
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(utilisateurController.lister()));
                case "POST": {
                    UtilisateurRequest request = lireCorps(exchange, UtilisateurRequest.class);
                    return new Reponse(201, gson.toJson(utilisateurController.creer(request)));
                }
                default:
                    throw methodeNonSupportee(methode, "/utilisateurs");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(utilisateurController.obtenir(id)));
                case "PUT": {
                    UtilisateurRequest request = lireCorps(exchange, UtilisateurRequest.class);
                    return new Reponse(200, gson.toJson(utilisateurController.modifier(id, request)));
                }
                case "DELETE":
                    utilisateurController.supprimer(id, connecte);
                    return new Reponse(204, "");
                default:
                    throw methodeNonSupportee(methode, "/utilisateurs/{id}");
            }
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // GET    /animaux?nom=&espece=&statut=          [TOUS]
    // GET    /animaux/{id}                          [TOUS]
    // POST   /animaux   PUT/DELETE /animaux/{id}    [ADMIN, EMPLOYE]
    // GET    /animaux/{id}/soins  (historique)     [VETERINAIRE]
    //   POST et PUT acceptent du JSON (sans photo) OU du multipart/form-data :
    //   champs texte de l'animal + fichier "photo" + champ "supprimerPhoto=true" (PUT)
    // GET    /images/{nomFichier}                  [public]
    // ---------------------------------------------------------
    private Reponse routesAnimal(String methode, String[] segments, HttpExchange exchange) throws IOException {
        String token = token(exchange);
        if (segments.length == 2) {
            switch (methode) {
                case "GET": {
                    authController.verifier(token, TOUS);
                    Map<String, String> params = parametres(exchange);
                    return new Reponse(200, gson.toJson(animalController.lister(
                            params.get("nom"),
                            enumParam(params, "espece", Espece.class),
                            enumParam(params, "statut", StatutAnimal.class))));
                }
                case "POST": {
                    authController.verifier(token, PERSONNEL);
                    AnimalRecu recu = lireAnimal(exchange);
                    return new Reponse(201, gson.toJson(animalController.creer(recu.animal(), recu.photo())));
                }
                default:
                    throw methodeNonSupportee(methode, "/animaux");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    authController.verifier(token, TOUS);
                    return new Reponse(200, gson.toJson(animalController.obtenir(id)));
                case "PUT": {
                    authController.verifier(token, PERSONNEL);
                    AnimalRecu recu = lireAnimal(exchange);
                    return new Reponse(200, gson.toJson(
                            animalController.modifier(id, recu.animal(), recu.photo(), recu.supprimerPhoto())));
                }
                case "DELETE":
                    authController.verifier(token, PERSONNEL);
                    animalController.supprimer(id);
                    return new Reponse(204, "");
                default:
                    throw methodeNonSupportee(methode, "/animaux/{id}");
            }
        }
        if (segments.length == 4 && "soins".equals(segments[3]) && "GET".equals(methode)) {
            authController.verifier(token, VETERINAIRE);
            return new Reponse(200, gson.toJson(animalController.historiqueMedical(entier(segments[2]))));
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // /adoptants?recherche=  et  /adoptants/{id}    [ADMIN, EMPLOYE]
    // ---------------------------------------------------------
    private Reponse routesAdoptant(String methode, String[] segments, HttpExchange exchange) throws IOException {
        authController.verifier(token(exchange), PERSONNEL);
        if (segments.length == 2) {
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(adoptantController.lister(
                            parametres(exchange).get("recherche"))));
                case "POST": {
                    Adoptant adoptant = lireCorps(exchange, Adoptant.class);
                    return new Reponse(201, gson.toJson(adoptantController.creer(adoptant)));
                }
                default:
                    throw methodeNonSupportee(methode, "/adoptants");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(adoptantController.obtenir(id)));
                case "PUT": {
                    Adoptant adoptant = lireCorps(exchange, Adoptant.class);
                    return new Reponse(200, gson.toJson(adoptantController.modifier(id, adoptant)));
                }
                case "DELETE":
                    adoptantController.supprimer(id);
                    return new Reponse(204, "");
                default:
                    throw methodeNonSupportee(methode, "/adoptants/{id}");
            }
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // GET  /demandes?statut=&adoptantId=&animalId=   [ADMIN, EMPLOYE]
    // POST /demandes            GET /demandes/{id}
    // PUT  /demandes/{id}/accepter   PUT /demandes/{id}/refuser
    // ---------------------------------------------------------
    private Reponse routesDemande(String methode, String[] segments, HttpExchange exchange) throws IOException {
        authController.verifier(token(exchange), PERSONNEL);
        if (segments.length == 2) {
            switch (methode) {
                case "GET": {
                    Map<String, String> params = parametres(exchange);
                    return new Reponse(200, gson.toJson(demandeController.lister(
                            enumParam(params, "statut", StatutDemande.class),
                            entierParam(params, "adoptantId"),
                            entierParam(params, "animalId"))));
                }
                case "POST": {
                    DemandeAdoption demande = lireCorps(exchange, DemandeAdoption.class);
                    return new Reponse(201, gson.toJson(demandeController.enregistrer(demande)));
                }
                default:
                    throw methodeNonSupportee(methode, "/demandes");
            }
        }
        if (segments.length == 3 && "GET".equals(methode)) {
            return new Reponse(200, gson.toJson(demandeController.obtenir(entier(segments[2]))));
        }
        if (segments.length == 4 && "PUT".equals(methode)) {
            int id = entier(segments[2]);
            if ("accepter".equals(segments[3])) {
                return new Reponse(200, gson.toJson(demandeController.accepter(id)));
            }
            if ("refuser".equals(segments[3])) {
                return new Reponse(200, gson.toJson(demandeController.refuser(id)));
            }
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // GET  /adoptions?recherche=&statut=&adoptantId=&animalId=   [ADMIN, EMPLOYE]
    // POST /adoptions    GET /adoptions/{id}    PUT /adoptions/{id}
    // ---------------------------------------------------------
    private Reponse routesAdoption(String methode, String[] segments, HttpExchange exchange) throws IOException {
        authController.verifier(token(exchange), PERSONNEL);
        if (segments.length == 2) {
            switch (methode) {
                case "GET": {
                    Map<String, String> params = parametres(exchange);
                    return new Reponse(200, gson.toJson(adoptionController.lister(
                            params.get("recherche"),
                            enumParam(params, "statut", StatutAdoption.class),
                            entierParam(params, "adoptantId"),
                            entierParam(params, "animalId"))));
                }
                case "POST": {
                    Adoption adoption = lireCorps(exchange, Adoption.class);
                    return new Reponse(201, gson.toJson(adoptionController.enregistrer(adoption)));
                }
                default:
                    throw methodeNonSupportee(methode, "/adoptions");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(adoptionController.obtenir(id)));
                case "PUT": {
                    Adoption adoption = lireCorps(exchange, Adoption.class);
                    return new Reponse(200, gson.toJson(adoptionController.modifier(id, adoption)));
                }
                default:
                    throw methodeNonSupportee(methode, "/adoptions/{id}");
            }
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // /soins?type=&animalId=  et  /soins/{id}       [VETERINAIRE]
    // ---------------------------------------------------------
    private Reponse routesSoin(String methode, String[] segments, HttpExchange exchange) throws IOException {
        Utilisateur veterinaire = authController.verifier(token(exchange), VETERINAIRE);
        if (segments.length == 2) {
            switch (methode) {
                case "GET": {
                    Map<String, String> params = parametres(exchange);
                    return new Reponse(200, gson.toJson(soinController.lister(
                            enumParam(params, "type", TypeSoin.class),
                            entierParam(params, "animalId"))));
                }
                case "POST": {
                    Soin soin = lireCorps(exchange, Soin.class);
                    return new Reponse(201, gson.toJson(soinController.creer(soin, veterinaire)));
                }
                default:
                    throw methodeNonSupportee(methode, "/soins");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(soinController.obtenir(id)));
                case "PUT": {
                    Soin soin = lireCorps(exchange, Soin.class);
                    return new Reponse(200, gson.toJson(soinController.modifier(id, soin)));
                }
                case "DELETE":
                    soinController.supprimer(id);
                    return new Reponse(204, "");
                default:
                    throw methodeNonSupportee(methode, "/soins/{id}");
            }
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // /donateurs?recherche=&type=  /donateurs/{id}  /donateurs/{id}/dons   [ADMIN]
    // ---------------------------------------------------------
    private Reponse routesDonateur(String methode, String[] segments, HttpExchange exchange) throws IOException {
        authController.verifier(token(exchange), ADMIN);
        if (segments.length == 2) {
            switch (methode) {
                case "GET": {
                    Map<String, String> params = parametres(exchange);
                    return new Reponse(200, gson.toJson(donateurController.lister(
                            params.get("recherche"),
                            enumParam(params, "type", TypeDonateur.class))));
                }
                case "POST": {
                    Donateur donateur = lireCorps(exchange, Donateur.class);
                    return new Reponse(201, gson.toJson(donateurController.creer(donateur)));
                }
                default:
                    throw methodeNonSupportee(methode, "/donateurs");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(donateurController.obtenir(id)));
                case "PUT": {
                    Donateur donateur = lireCorps(exchange, Donateur.class);
                    return new Reponse(200, gson.toJson(donateurController.modifier(id, donateur)));
                }
                case "DELETE":
                    donateurController.supprimer(id);
                    return new Reponse(204, "");
                default:
                    throw methodeNonSupportee(methode, "/donateurs/{id}");
            }
        }
        if (segments.length == 4 && "dons".equals(segments[3]) && "GET".equals(methode)) {
            return new Reponse(200, gson.toJson(donateurController.dons(entier(segments[2]))));
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // /dons?type=&donateurId=  et  /dons/{id}       [ADMIN]
    // ---------------------------------------------------------
    private Reponse routesDon(String methode, String[] segments, HttpExchange exchange) throws IOException {
        authController.verifier(token(exchange), ADMIN);
        if (segments.length == 2) {
            switch (methode) {
                case "GET": {
                    Map<String, String> params = parametres(exchange);
                    return new Reponse(200, gson.toJson(donController.lister(
                            enumParam(params, "type", TypeDon.class),
                            entierParam(params, "donateurId"))));
                }
                case "POST": {
                    Don don = lireCorps(exchange, Don.class);
                    return new Reponse(201, gson.toJson(donController.creer(don)));
                }
                default:
                    throw methodeNonSupportee(methode, "/dons");
            }
        }
        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":
                    return new Reponse(200, gson.toJson(donController.obtenir(id)));
                case "PUT": {
                    Don don = lireCorps(exchange, Don.class);
                    return new Reponse(200, gson.toJson(donController.modifier(id, don)));
                }
                case "DELETE":
                    donController.supprimer(id);
                    return new Reponse(204, "");
                default:
                    throw methodeNonSupportee(methode, "/dons/{id}");
            }
        }
        throw routeInconnue(segments);
    }

    // ---------------------------------------------------------
    // GET /dashboard                                 [ADMIN]
    // ---------------------------------------------------------
    private Reponse routesDashboard(String methode, String[] segments, HttpExchange exchange) {
        authController.verifier(token(exchange), ADMIN);
        if (segments.length == 2 && "GET".equals(methode)) {
            return new Reponse(200, gson.toJson(dashboardController.consulter()));
        }
        throw routeInconnue(segments);
    }

    // =========================================================
    // Outils
    // =========================================================

    // Lit tout le corps de la requete et le convertit en objet Java avec Gson
    private <T> T lireCorps(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String corps = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            if (corps.isBlank()) {
                throw new IllegalArgumentException("Corps de la requete vide : un JSON est attendu.");
            }
            T objet = gson.fromJson(corps, type);
            if (objet == null) {
                throw new IllegalArgumentException("Corps JSON invalide.");
            }
            return objet;
        }
    }

    // Animal recu par POST/PUT /animaux : donnees + photo facultative
    private record AnimalRecu(Animal animal, byte[] photo, boolean supprimerPhoto) {
    }

    // Taille maximale d'un formulaire : l'image (5 Mo) + les champs texte
    private static final int TAILLE_MAX_FORMULAIRE = ImageUtil.TAILLE_MAX + 64 * 1024;

    /**
     * Lit un animal en JSON (application/json) ou depuis un formulaire (multipart/form-data).
     * Pour le formulaire, les champs texte sont convertis en JSON puis passes a Gson :
     * on reutilise ainsi les memes conversions (dates ISO, enums) que pour le JSON.
     */
    private AnimalRecu lireAnimal(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (!MultipartParser.estMultipart(contentType)) {
            return new AnimalRecu(lireCorps(exchange, Animal.class), null, false);
        }

        byte[] corps;
        try (InputStream is = exchange.getRequestBody()) {
            corps = is.readNBytes(TAILLE_MAX_FORMULAIRE + 1);
        }
        if (corps.length > TAILLE_MAX_FORMULAIRE) {
            throw new IllegalArgumentException("Formulaire trop volumineux (image de 5 Mo maximum).");
        }
        MultipartParser.Formulaire formulaire = MultipartParser.decoder(corps, contentType);

        JsonObject json = new JsonObject();
        formulaire.champs().forEach((cle, valeur) -> {
            // Champ vide = non renseigne (ex. date de naissance inconnue)
            if (!valeur.isBlank() && !"supprimerPhoto".equals(cle)) {
                json.addProperty(cle, valeur.trim());
            }
        });
        Animal animal = gson.fromJson(json, Animal.class);
        MultipartParser.Fichier photo = formulaire.fichier("photo");
        boolean supprimerPhoto = "true".equalsIgnoreCase(formulaire.champ("supprimerPhoto"));
        return new AnimalRecu(animal, photo == null ? null : photo.contenu(), supprimerPhoto);
    }

    // Envoie une image (GET /images/{nomFichier})
    private void envoyerImage(HttpExchange exchange, String nomFichier) throws IOException {
        byte[] image = animalController.image(nomFichier);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", ImageUtil.typeMime(nomFichier));
        headers.set("Cache-Control", "public, max-age=86400"); // nom unique : l'image ne change jamais
        headers.set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(200, image.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(image);
        }
    }

    // Convertit l'ID de l'URL ; leve IllegalArgumentException (400) s'il est invalide
    private int entier(String valeur) {
        try {
            return Integer.parseInt(valeur);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identifiant invalide : " + valeur);
        }
    }

    // Lit les parametres de la query string (?cle=valeur&...)
    private Map<String, String> parametres(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String paire : query.split("&")) {
            String[] cleValeur = paire.split("=", 2);
            String cle = URLDecoder.decode(cleValeur[0], StandardCharsets.UTF_8);
            String valeur = cleValeur.length > 1 ? URLDecoder.decode(cleValeur[1], StandardCharsets.UTF_8) : "";
            if (!valeur.isBlank()) {
                params.put(cle, valeur.trim());
            }
        }
        return params;
    }

    private Integer entierParam(Map<String, String> params, String cle) {
        String valeur = params.get(cle);
        return valeur == null ? null : entier(valeur);
    }

    private <E extends Enum<E>> E enumParam(Map<String, String> params, String cle, Class<E> type) {
        String valeur = params.get(cle);
        if (valeur == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, valeur.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valeur invalide pour '" + cle + "' : " + valeur);
        }
    }

    // Extrait le jeton de l'en-tete "Authorization: Bearer <token>"
    private String token(HttpExchange exchange) {
        String entete = exchange.getRequestHeaders().getFirst("Authorization");
        if (entete == null || !entete.startsWith("Bearer ")) {
            return null;
        }
        return entete.substring("Bearer ".length()).trim();
    }

    private IllegalArgumentException methodeNonSupportee(String methode, String route) {
        return new IllegalArgumentException("Methode non supportee : " + methode + " " + route);
    }

    private ResourceNotFoundException routeInconnue(String[] segments) {
        return new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    // Renvoie un JSON minimal {"erreur": "..."}
    private String messageErreur(String message) {
        Map<String, String> erreur = new HashMap<>();
        erreur.put("erreur", message);
        return gson.toJson(erreur);
    }

    // En-tetes CORS : autorisent le frontend React (autre port) a appeler l'API
    private void ajouterEntetesCors(Headers headers) {
        headers.set("Access-Control-Allow-Origin", CORS_ORIGIN);
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.set("Access-Control-Max-Age", "3600");
    }

    // Envoie la reponse HTTP avec statut et corps JSON
    private void envoyer(HttpExchange exchange, int statut, String corps) throws IOException {
        byte[] bytes = corps == null ? new byte[0] : corps.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statut, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            // Reponse sans corps (ex. 204 No Content)
            exchange.sendResponseHeaders(statut, -1);
            exchange.close();
        }
    }
}