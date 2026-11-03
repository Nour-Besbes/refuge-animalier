# Gestion d'un Refuge Animalier — Backend

Backend de l'application **Gestion d'un Refuge Animalier**, développé en Java 17. Il permet de gérer les animaux du refuge, les adoptions, les soins vétérinaires ainsi que les dons et les utilisateurs.

Le backend est développé en Java pur, sans framework comme Spring Boot. Il repose sur une architecture en couches et communique avec le frontend React à travers une API REST.

## Architecture du projet

L'application suit une architecture organisée en plusieurs couches afin de séparer les responsabilités et de faciliter la maintenance du code.

```text
                  Frontend React
                        |
                    HTTP / JSON
                        |
                    ApiServer
                        |
                      Router
                        |
                    Controller
                        |
                     Service
                        |
                       DAO
                        |
                DatabaseConnection
                        |
                     MySQL
```

Les échanges de données entre le frontend et le backend sont réalisés au format JSON, avec Gson pour la sérialisation et la désérialisation.

## Fonctionnalités

### Authentification et gestion des utilisateurs
- Authentification des utilisateurs.
- Gestion des rôles et des autorisations.
- Gestion des comptes employés et vétérinaires.
- Accès aux fonctionnalités selon le rôle connecté.

### Gestion des animaux
- Ajouter, modifier, supprimer et consulter les animaux.
- Rechercher et filtrer les animaux.
- Gérer les informations et les photos des animaux.
- Suivre leur état de santé et leur statut.

### Gestion des adoptions
- Gérer les adoptants.
- Enregistrer les demandes d'adoption.
- Accepter ou refuser les demandes.
- Gérer les adoptions et leur évolution.
- Contrôler la disponibilité des animaux.

### Gestion des soins vétérinaires
- Enregistrer les consultations et les vaccinations.
- Ajouter des traitements et des observations.
- Consulter l'historique médical des animaux.
- Assurer le suivi des prochaines visites.

### Gestion des dons
- Gérer les donateurs.
- Enregistrer les dons financiers et matériels.
- Consulter et modifier les informations relatives aux dons.

### Tableau de bord
- Consulter les statistiques du refuge.
- Suivre les principales activités liées aux animaux, aux adoptions et aux dons.

## Technologies utilisées

| Technologie | Utilisation |
|---|---|
| Java 17 | Langage de programmation |
| JDK HttpServer | Serveur HTTP |
| JDBC | Accès à la base de données |
| MySQL | Système de gestion de base de données |
| Maven | Gestion des dépendances et compilation |
| Gson | Gestion des données JSON |
| Postman | Test des endpoints API |
| Git / GitHub | Gestion du code source |

## Organisation du backend

```text
backend/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tn/enicarthage/gestionRefugeAnimalier/
│   │   │       ├── config/
│   │   │       │   └── database/
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── router/
│   │   │       ├── service/
│   │   │       ├── util/
│   │   │       ├── ApiServer.java
│   │   │       └── Main.java
│   │   │
│   │   └── resources/
│   │       ├── database.sql
│   │       └── requetes.sql
│   │
│   └── test/
│
├── uploads/
├── pom.xml
├── postman_collection.json
└── README.md
```

### Rôle des différentes couches

- **Model :** représente les entités de l'application.
- **DAO :** assure les opérations d'accès et de manipulation des données dans MySQL.
- **Service :** contient les traitements et les règles métier.
- **Controller :** gère les opérations liées aux différentes ressources.
- **Router :** associe les requêtes HTTP aux contrôleurs.
- **DTO :** structure les données échangées avec le frontend.
- **Config :** centralise la configuration et la connexion à la base de données.
- **Exception :** regroupe la gestion des exceptions de l'application.
- **Util :** contient les classes utilitaires.

## Installation et configuration

### Prérequis

Avant de lancer le backend, il faut disposer de :

- JDK 17
- Apache Maven
- MySQL (via WAMP)
- IntelliJ IDEA ou un autre IDE Java

### 1. Récupérer le projet

```bash
git clone https://github.com/Nour-Besbes/refuge-animalier.git
```

Accéder au dossier du backend :

```bash
cd refuge-animalier/backend
```

### 2. Configurer la base de données

Le script de création de la base de données se trouve dans :

```text
src/main/resources/database.sql
```

Exécuter ce script depuis phpMyAdmin ou lancer la classe `DatabaseConnection` pour initialiser la base et ses tables.

### 3. Configurer la connexion MySQL

La connexion peut être personnalisée à l'aide des variables d'environnement suivantes :

| Variable | Valeur par défaut |
|---|---|
| DB_HOST | localhost |
| DB_PORT | 3306 |
| DB_NAME | gestion_refuge_animalier |
| DB_USER | root |
| DB_PASSWORD | vide |

### 4. Compiler le projet

```bash
mvn clean install
```

### 5. Démarrer le serveur

```bash
mvn exec:java
```

Le serveur démarre sur :

```text
http://localhost:8686
```

## Tester l'API

Une collection Postman est disponible à la racine du projet :

```text
postman_collection.json
```

Pour tester les fonctionnalités :

1. Importer la collection dans Postman.
2. Commencer par l'authentification.
3. Tester les différentes requêtes selon les rôles.
4. Vérifier les opérations CRUD et les règles métier.

## Codes HTTP utilisés

| Code | Signification |
|---|---|
| 200 | Requête exécutée avec succès |
| 201 | Ressource créée |
| 204 | Opération effectuée sans contenu retourné |
| 400 | Requête invalide |
| 401 | Authentification requise |
| 403 | Accès non autorisé |
| 404 | Ressource introuvable |
| 409 | Conflit ou règle métier non respectée |
| 500 | Erreur interne du serveur |

## Intégration avec le frontend

Le frontend React communique avec le backend à travers des requêtes HTTP.

- Backend : `http://localhost:8686`
- Communication : REST API / JSON
- Authentification : Bearer Token
- CORS : configuré au niveau du Router

---

**Projet académique — Génie Informatique**

Développement d'une application de gestion d'un refuge animalier avec Java, React et MySQL.
