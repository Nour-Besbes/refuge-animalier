# Gestion d'un refuge animalier — Backend Java pur

Backend **Java 17 sans framework** (ni Spring, ni Hibernate, ni JPA) :
serveur HTTP intégré au JDK + JDBC + MySQL (WAMP), conforme au *Guide de référence* et au *Guide de développement — Étape 2*.

```
React ──HTTP/JSON──> ApiServer → Router → Controller → Service → DAO → DatabaseConnection → MySQL
                                   │
                         DTO (Request/Response) <-> Gson (JSON)
```

Sommaire : 1. Analyse · 2. Arborescence · 3. Base de données · 4. Configuration · 5. Couches · 6. Point d'entrée · 7. Environnement · 8. Tests · 9. Intégration React · 10. Exécution sous Windows

---

## Étape 1 — Analyse des documents

### Acteurs et droits (diagramme de cas d'utilisation)

| Acteur | Fonctionnalités | Valeur `Role` |
|---|---|---|
| Administrateur | Hérite de l'employé + donateurs, dons, utilisateurs, dashboard | `ADMIN` |
| Employé (bénévole) | Animaux, adoptants, demandes d'adoption, adoptions | `EMPLOYE` |
| Vétérinaire | Liste des animaux, soins, historique médical | `VETERINAIRE` |
| Tous | S'authentifier | — |

### Entités (diagramme de classes)

| Entité | Attributs principaux | Relations |
|---|---|---|
| Utilisateur | nom, prenom, email, motDePasse, telephone, role | 1 — 0..* Soin (réalise) |
| Animal | nom, espece, race, dateNaissance, sexe, description, etatSante, statut, dateArrivee, photo | 1 — 0..* Soin, 1 — 0..* Demande, 1 — 0..1 Adoption |
| Adoptant | nom, prenom, email, adresse, telephone, dateInscription | 1 — 0..* Demande, 1 — 0..* Adoption |
| DemandeAdoption | dateDemande, motif, statut | → Adoptant, → Animal |
| Adoption | dateAdoption, statut, commentaire | → Adoptant, → Animal |
| Soin | dateSoin, type, motif, diagnostic, traitement, prochaineVisite, remarque | → Animal, → Utilisateur |
| Donateur | nom, prenom, email, adresse, telephone, type | 1 — 0..* Don |
| Don | dateDon, typeDon, montant, description | → Donateur |

Énumérations : `Role`, `Sexe`, `Espece`, `EtatSante`, `StatutAnimal`, `StatutDemande`, `StatutAdoption`, `TypeSoin`, `TypeDon`, `TypeDonateur`.

### Contradictions relevées et solution retenue

| Point | Ce que disent les documents | Choix retenu |
|---|---|---|
| Version Java | Guide : 22 (et 21 dans l'étape 2) ; consigne : JDK 17 | `pom.xml` en **17**. Le code n'utilise rien d'ultérieur (records, `HttpServer`, `Files.readString` existent en 17). |
| Emplacement de `DatabaseConnection` | Guide : `config/` ; consigne : `config/database/` | `config.database.DatabaseConnection` (reste dans `config`, donc compatible). |
| Emplacement de `Main` / `ApiServer` | Guide de référence : package racine ; guide étape 2 : package `main` | Package racine, comme le guide de référence (« à reprendre tels quels ») et son `pom.xml`. |
| `database.sql` | Étape 2 : `database/database.sql` ; guide de référence : `src/main/resources/database.sql` | `src/main/resources/database.sql`, car `DatabaseConnection.main()` le lit à cet endroit. |
| Identifiants | UML : `idAnimal`, `idUser`… ; guide : colonne `id` obligatoire | Colonne et attribut `id` dans toutes les tables + `created_at`, `updated_at`. |
| Statuts de demande | Texte : `ACCEPTEE`/`REFUSEE` ; UML : `ACCEPTE`/`REFUSE` | `EN_ATTENTE`, `ACCEPTEE`, `REFUSEE` (texte des fonctionnalités). |
| Type de soin | Texte : « vaccin » ; UML : `VACCINATION` | `CONSULTATION`, `VACCINATION` (UML). |
| Montant d'un don | UML : `double` ; guide : `DECIMAL` ↔ `BigDecimal` | `BigDecimal` (évite les erreurs d'arrondi). |
| Coquilles UML | `desciption`, `dateArrivé` | `description`, `dateArrivee`. |

### Hypothèses (éléments non précisés par les documents)

1. **Authentification** : jeton aléatoire conservé en mémoire, mots de passe hachés (PBKDF2). Les sessions sont perdues au redémarrage et il n'y a pas de HTTPS en local : c'est une authentification **pédagogique**, pas une sécurité de production.
2. **Gestion des utilisateurs** : l'administrateur crée uniquement des `EMPLOYE` et `VETERINAIRE` ; un admin initial est fourni dans `database.sql`.
3. **Cycle de vie d'une adoption** : une demande doit être `ACCEPTEE` avant d'enregistrer l'adoption ; le statut de l'animal suit automatiquement (voir les règles).
4. **0..1 Adoption par animal** : interprété comme « au plus une adoption **non annulée** », pour qu'un animal rendu (adoption `ANNULE`) puisse être adopté de nouveau.
5. **Adoptions et demandes** : pas de suppression (absente des fonctionnalités) ; « Adoptions réalisées » du dashboard = adoptions `CONFIRME`.
6. **Soins** : réservés au vétérinaire (cas d'utilisation) ; l'auteur du soin est le vétérinaire connecté.

### Règles métier (couche Service)

- **Animal** : nom, espèce, sexe obligatoires ; dates jamais dans le futur ; naissance ≤ arrivée. Statuts manuels : `DISPONIBLE`, `EN_SOIN`, `INDISPONIBLE`. `EN_ATTENTE_ADOPTION` et `ADOPTE` sont pilotés uniquement par les demandes et les adoptions.
- **Demande** : animal `DISPONIBLE` ou `EN_ATTENTE_ADOPTION` ; pas deux demandes en attente du même adoptant pour le même animal. À l'enregistrement, l'animal passe `EN_ATTENTE_ADOPTION`. Accepter refuse automatiquement les autres demandes en attente pour cet animal. Refuser la dernière demande remet l'animal `DISPONIBLE`.
- **Adoption** : `EN_COURS → CONFIRME | ANNULE`, `CONFIRME → ANNULE`, `ANNULE` est définitif. L'animal suit : `EN_COURS → EN_ATTENTE_ADOPTION`, `CONFIRME → ADOPTE`, `ANNULE → DISPONIBLE` (dans une **transaction**).
- **Soin** : la prochaine visite doit être postérieure à la date du soin.
- **Don** : un don `ARGENT` exige un montant > 0 ; les autres types acceptent une valeur estimée facultative (≥ 0).
- **Donateur** : le prénom est obligatoire pour un `PARTICULIER` ; l'email est facultatif mais unique.
- **Emails** uniques (utilisateur, adoptant, donateur). Suppression bloquée (409) si des données liées existent (clés étrangères).

---

## Étape 2 — Arborescence

```
gestion-refuge-animalier/
└── backend/
    ├── pom.xml                         Dépendances Gson + MySQL, Java 17, exec, shade
    ├── postman_collection.json         54 requêtes prêtes à importer dans Postman
    ├── README.md
    └── src/
        ├── main/
        │   ├── java/tn/enicarthage/gestionRefugeAnimalier/
        │   │   ├── Main.java                     Teste la connexion puis démarre le serveur (8080)
        │   │   ├── ApiServer.java                Serveur HTTP du JDK, branche le Router
        │   │   ├── config/
        │   │   │   └── database/
        │   │   │       ├── DatabaseConnection.java   Connexions JDBC + initialisation de la base
        │   │   │       └── TransactionManager.java   commit / rollback de plusieurs requêtes
        │   │   ├── model/          Utilisateur, Animal, Adoptant, DemandeAdoption, Adoption,
        │   │   │                   Soin, Donateur, Don + 10 énumérations
        │   │   ├── dao/            UtilisateurDAO, AnimalDAO, AdoptantDAO, DemandeAdoptionDAO,
        │   │   │                   AdoptionDAO, SoinDAO, DonateurDAO, DonDAO  (SQL via JDBC)
        │   │   ├── service/        AuthService, UtilisateurService, AnimalService, AdoptantService,
        │   │   │                   DemandeAdoptionService, AdoptionService, SoinService,
        │   │   │                   DonateurService, DonService, DashboardService (règles métier)
        │   │   ├── controller/     Un contrôleur par ressource + AuthController, DashboardController
        │   │   ├── dto/            LoginRequest, LoginResponse, UtilisateurRequest,
        │   │   │                   UtilisateurResponse (sans mot de passe), DashboardResponse
        │   │   ├── exception/      ResourceNotFound (404), Unauthorized (401), Forbidden (403),
        │   │   │                   Conflict (409), DataAccess (500)
        │   │   ├── util/           JdbcUtil (conversions JDBC), PasswordUtil (hachage),
        │   │   │                   ValidationUtil (contrôles de saisie), ImageUtil (photos)
        │   │   └── router/
        │   │       └── Router.java              URL + méthode → contrôleur, JSON, CORS, rôles, erreurs
        │   └── resources/
        │       ├── database.sql                 Création des tables + données de test
        │       └── requetes.sql                 SELECT / WHERE / JOIN / INSERT / UPDATE / DELETE
        └── test/java/tn/enicarthage/gestionRefugeAnimalier/
            └── TestBackend.java                 Tests DAO + services sans HTTP
```

**Choix du guide respectés** : DAO en classes concrètes (le guide n'impose pas d'interfaces), `PreparedStatement` partout, connexion centralisée, packages `config, model, dao, service, controller, dto, exception, util, router`. Aucun package supplémentaire n'a été ajouté.

---

## Étape 3 — Base de données

Fichier : `src/main/resources/database.sql`. Ordre de création : `utilisateur`, `adoptant`, `donateur`, `animal` (sans dépendance), puis `demande_adoption`, `adoption`, `soin`, `don` (avec clés étrangères).

Chaque table contient `id INT AUTO_INCREMENT PRIMARY KEY`, `created_at` et `updated_at`.
Les clés étrangères sont en mode `RESTRICT` (par défaut) : on ne peut pas supprimer un animal qui a des soins, ni un donateur qui a des dons. L'historique est donc conservé et l'API renvoie **409**.

Comptes de test (mots de passe hachés dans le script, **à changer** en dehors du projet) :

| Email | Mot de passe | Rôle |
|---|---|---|
| admin@refuge.tn | Admin@123 | ADMIN |
| employe@refuge.tn | Employe@123 | EMPLOYE |
| veto@refuge.tn | Veto@123 | VETERINAIRE |

Pour créer un autre hash : lancer la classe `util.PasswordUtil` avec le mot de passe en argument.

---

## Étape 4 — Configuration Java

`config/database/DatabaseConnection.java` reprend le code du guide avec une seule différence : chaque paramètre peut être remplacé par une **variable d'environnement**, sans toucher au code et sans mettre de mot de passe dans Git.

| Variable | Valeur par défaut (WAMP) |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `gestion_refuge_animalier` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | *(vide)* |
| `CORS_ORIGIN` | `*` (ex. `http://localhost:5173`) |

> ⚠️ **WAMP et le port** : selon la version, WAMP installe **MariaDB sur 3306** et **MySQL sur 3308**. Vérifiez dans *WAMP → MySQL → Port utilisé par MySQL*. Si c'est 3308 : `set DB_PORT=3308` (cmd) ou `$env:DB_PORT="3308"` (PowerShell), ou changez la valeur par défaut dans la classe.

`DB_NAME` doit correspondre au nom de la base créée par `database.sql`. Si vous le changez, modifiez aussi le script.

---

## Étape 5 — Les couches

| Couche | Rôle | Exemple |
|---|---|---|
| **model** | Un POJO par table (constructeurs, getters/setters, `toString`) | `Animal` ↔ table `animal`, `dateArrivee` ↔ `date_arrivee` |
| **dao** | SQL uniquement : `create`, `findAll`, `findById`, `update`, `delete`, `rechercher` | `AnimalDAO.rechercher(nom, espece, statut)` |
| **service** | Validation et règles métier, coordination des DAO, transactions | `DemandeAdoptionService.accepter(id)` |
| **controller** | Reçoit les données du Router, appelle le service, renvoie le résultat | `AnimalController.creer(animal)` |
| **router** | Associe l'URL et la méthode au contrôleur, gère JSON, rôles et codes HTTP | `PUT /demandes/5/accepter` |

**Transactions** : quand une action modifie plusieurs tables (par exemple insérer une adoption *et* changer le statut de l'animal), le service passe par `TransactionManager.executer(...)`. Les DAO reçoivent alors la même `Connection` : tout est validé, ou tout est annulé.

**Erreurs SQL** : au lieu du `e.printStackTrace()` des exemples du guide, les DAO lèvent une exception qui remonte jusqu'au Router. Une violation de clé étrangère ou d'unicité devient 409 ; toute autre erreur devient 500, avec le détail affiché seulement dans la console du serveur.

---

## Étape 6 — Point d'entrée

`Main.java` (identique au guide) : `testerConnexion()` puis `new ApiServer(8080).demarrer()`.

### Routes de l'API

| Méthode | URL | Rôles | Code |
|---|---|---|---|
| POST | `/auth/login` | public | 200 / 401 |
| POST | `/auth/logout` · GET `/auth/moi` | connecté | 204 / 200 |
| GET | `/dashboard` | ADMIN | 200 |
| GET, POST | `/utilisateurs` | ADMIN | 200 / 201 |
| GET, PUT, DELETE | `/utilisateurs/{id}` | ADMIN | 200 / 204 |
| GET | `/animaux?nom=&espece=&statut=` · `/animaux/{id}` | tous | 200 |
| POST · PUT, DELETE | `/animaux` · `/animaux/{id}` | ADMIN, EMPLOYE | 201 / 200 / 204 |
| GET | `/animaux/{id}/soins` (historique médical) | VETERINAIRE | 200 |
| PUT | `/animaux/{id}/photo` (corps = fichier image brut) | ADMIN, EMPLOYE | 200 / 400 |
| DELETE | `/animaux/{id}/photo` | ADMIN, EMPLOYE | 200 |
| GET | `/images/{nomFichier}` | public | 200 / 404 |
| GET, POST | `/adoptants?recherche=` | ADMIN, EMPLOYE | 200 / 201 |
| GET, PUT, DELETE | `/adoptants/{id}` | ADMIN, EMPLOYE | 200 / 204 |
| GET, POST | `/demandes?statut=&adoptantId=&animalId=` | ADMIN, EMPLOYE | 200 / 201 |
| GET | `/demandes/{id}` | ADMIN, EMPLOYE | 200 |
| PUT | `/demandes/{id}/accepter` · `/demandes/{id}/refuser` | ADMIN, EMPLOYE | 200 / 409 |
| GET, POST | `/adoptions?recherche=&statut=&adoptantId=&animalId=` | ADMIN, EMPLOYE | 200 / 201 |
| GET, PUT | `/adoptions/{id}` | ADMIN, EMPLOYE | 200 |
| GET, POST | `/soins?type=&animalId=` | VETERINAIRE | 200 / 201 |
| GET, PUT, DELETE | `/soins/{id}` | VETERINAIRE | 200 / 204 |
| GET, POST | `/donateurs?recherche=&type=` | ADMIN | 200 / 201 |
| GET, PUT, DELETE | `/donateurs/{id}` · GET `/donateurs/{id}/dons` | ADMIN | 200 / 204 |
| GET, POST | `/dons?type=&donateurId=` | ADMIN | 200 / 201 |
| GET, PUT, DELETE | `/dons/{id}` | ADMIN | 200 / 204 |

Codes d'erreur : **400** donnée invalide ou JSON mal formé · **401** non connecté · **403** rôle insuffisant · **404** introuvable · **409** règle métier ou contrainte · **500** erreur serveur. Le corps vaut toujours `{"erreur": "message"}`.

Dates au format ISO `"2026-10-15"`, énumérations en majuscules (`"CHAT"`, `"EN_ATTENTE"`).

### Photos des animaux

- **Stockage** : les fichiers sont enregistrés dans `backend/uploads/animaux/` (modifiable avec la variable `UPLOAD_DIR`). La colonne `animal.photo` ne contient que le **nom** du fichier, par exemple `3f2a…-9c.jpg`. Ce dossier est exclu de Git.
- **Envoi** : `PUT /animaux/{id}/photo`, avec comme corps **le fichier lui-même** et l'en-tête `Content-Type: image/jpeg|png|webp`. Il n'y a pas de `multipart/form-data`, dont le décodage manuel serait long et fragile sans framework.
- **Ajout d'un animal** : on crée d'abord l'animal (`POST /animaux`, JSON), puis on envoie sa photo avec l'`id` reçu. En modification, le JSON ne touche jamais à la photo.
- **Affichage** : `<img src="http://localhost:8080/images/{photo}">`. La route est publique, car une balise `<img>` ne peut pas envoyer de token.
- **Sécurité** : le format est vérifié avec la *signature* du fichier (ses premiers octets) et non avec son nom. Seuls JPEG, PNG et WEBP de 5 Mo maximum sont acceptés. Le nom est généré par le serveur (UUID), ce qui empêche d'écrire hors du dossier.
- **Nettoyage** : l'ancienne image est supprimée quand on la remplace ou la retire, et quand l'animal est supprimé.
- **Exemple React** : voir `frontend-exemple/src/` (`api.js`, `AnimalForm.jsx` avec aperçu, `ListeAnimaux.jsx` avec vignettes).

---

## Étape 7 — Configuration de l'environnement

1. **JDK 17** : installez Temurin 17 (adoptium.net), puis vérifiez avec `java -version` et `javac -version`. Si `JAVA_HOME` n'est pas défini, ajoutez-le dans les variables d'environnement Windows.
2. **Maven** : intégré à IntelliJ IDEA et Eclipse. Pour l'utiliser en ligne de commande, installez Apache Maven et vérifiez avec `mvn -v`.
3. **Pilote JDBC** : rien à télécharger. Maven récupère `mysql-connector-j` (déclaré dans `pom.xml`) au premier `mvn compile`.
4. **WAMP** : lancez WampServer et attendez que l'icône soit **verte**. MySQL est alors démarré. Vérifiez le port (voir l'étape 4).
5. **Créer la base**, au choix :
   - lancez `DatabaseConnection.main()` (clic droit → *Run* dans l'IDE, ou la commande de l'étape 10) ;
   - ou ouvrez **phpMyAdmin** (`http://localhost/phpmyadmin`), onglet *SQL*, collez `database.sql` et exécutez.
6. **IDE** : *File → Open* sur le dossier `backend` (IntelliJ détecte `pom.xml`). Vérifiez *Project Structure → SDK = 17*.

---

## Étape 8 — Tests

1. **Connexion** : lancez `Main`. La console doit afficher `Connexion reussie a MySQL.`
2. **Sans HTTP (DAO + services)** : `TestBackend` vérifie le CRUD d'`AdoptantDAO` et les principales règles métier (13 vérifications) :
   ```
   mvn test-compile exec:java -Dexec.mainClass=tn.enicarthage.gestionRefugeAnimalier.TestBackend -Dexec.classpathScope=test
   ```
3. **Postman** : *Import* → `postman_collection.json`. Lancez d'abord **0. Auth → Login ADMIN** : le token est enregistré automatiquement dans la variable `{{token}}`. Reconnectez-vous en EMPLOYE pour la partie adoption, ou en VETERINAIRE pour les soins.
4. **Cas d'erreur à montrer** : `GET /animaux/999` → 404 ; `POST /animaux` avec `"dateNaissance":"2030-01-01"` → 400 ; EMPLOYE sur `/dashboard` → 403 ; adoption sans demande acceptée → 409 ; JSON mal formé → 400.
5. **Vérification SQL** : après chaque requête qui modifie des données, exécutez `SELECT * FROM ...` ou les requêtes de `requetes.sql`.

---

## Étape 9 — Intégration React

React (port 5173 avec Vite, ou 3000) et Java (port 8080) sont deux applications séparées. Elles communiquent en **HTTP/JSON** avec `fetch`. Comme les ports diffèrent, le navigateur exige des en-têtes **CORS**. Le Router les ajoute et répond aux requêtes `OPTIONS` envoyées avant chaque appel (*preflight*).

`src/api.js` côté React :

```js
const API_URL = "http://localhost:8080";

export async function api(chemin, { method = "GET", body } = {}) {
  const token = localStorage.getItem("token");
  const reponse = await fetch(API_URL + chemin, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: "Bearer " + token } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (reponse.status === 204) return null;
  const donnees = await reponse.json();
  if (!reponse.ok) throw new Error(donnees.erreur || "Erreur " + reponse.status);
  return donnees;
}

export async function login(email, motDePasse) {
  const { token, utilisateur } = await api("/auth/login", {
    method: "POST", body: { email, motDePasse },
  });
  localStorage.setItem("token", token);
  return utilisateur; // { id, nom, prenom, email, role } -> afficher le menu selon le role
}
```

Exemple de composant :

```jsx
import { useEffect, useState } from "react";
import { api } from "./api";

export default function ListeAnimaux() {
  const [animaux, setAnimaux] = useState([]);
  const [erreur, setErreur] = useState("");

  useEffect(() => {
    api("/animaux?statut=DISPONIBLE").then(setAnimaux).catch(e => setErreur(e.message));
  }, []);

  if (erreur) return <p>{erreur}</p>;
  return (
    <ul>
      {animaux.map(a => <li key={a.id}>{a.nom} ({a.espece}) - {a.statut}</li>)}
    </ul>
  );
}
```

En production, limitez CORS à l'adresse du frontend : `set CORS_ORIGIN=http://localhost:5173`.

---

## Étape 10 — Exécution complète sous Windows

```bat
:: 1. WAMP lance (icone verte), puis dans le dossier backend :
cd chemin\vers\gestion-refuge-animalier\backend

:: 2. (si MySQL est sur 3308 ou a un mot de passe)
set DB_PORT=3308
set DB_PASSWORD=votre_mot_de_passe

:: 3. Compiler (telecharge Gson + mysql-connector-j la premiere fois)
mvn compile

:: 4. Creer la base + tables + donnees de test
mvn exec:java -Dexec.mainClass=tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection

:: 5. Demarrer l'API (laisser la fenetre ouverte)
mvn exec:java

:: 6. Dans un autre terminal : verifier
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@refuge.tn\",\"motDePasse\":\"Admin@123\"}"
```

JAR autonome : `mvn package`, puis `java -jar target\gestionRefugeAnimalier-1.0-SNAPSHOT.jar`.

**Problèmes fréquents**
- `Communications link failure` : MySQL n'est pas démarré, ou le port est incorrect (3306/3308).
- `Unknown database` : `database.sql` n'a pas été exécuté.
- `Access denied for user 'root'` : renseignez `DB_PASSWORD`.
- `Address already in use: bind` : le port 8080 est déjà utilisé. Arrêtez l'ancienne instance.
- `NoSuchFileException database.sql` : lancez la commande depuis le dossier `backend`.
