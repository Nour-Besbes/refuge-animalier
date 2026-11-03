-- =====================================================================
--  Gestion d'un refuge animalier - script de creation de la base
--  Convention du guide : chaque table possede id, created_at, updated_at
--  Execution : DatabaseConnection.main()  ou  copier-coller dans phpMyAdmin
-- =====================================================================

DROP DATABASE IF EXISTS gestion_refuge_animalier;
CREATE DATABASE gestion_refuge_animalier
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE gestion_refuge_animalier;

-- ---------------------------------------------------------------------
-- 1. Tables sans dependance
-- ---------------------------------------------------------------------

CREATE TABLE utilisateur (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(191) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL,
    telephone     VARCHAR(30),
    role          ENUM('ADMIN', 'EMPLOYE', 'VETERINAIRE') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE adoptant (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    prenom            VARCHAR(100) NOT NULL,
    email             VARCHAR(191) NOT NULL UNIQUE,
    adresse           VARCHAR(255),
    telephone         VARCHAR(30) NOT NULL,
    date_inscription  DATE NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE donateur (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    prenom      VARCHAR(100),
    email       VARCHAR(191) UNIQUE,
    adresse     VARCHAR(255),
    telephone   VARCHAR(30),
    type        ENUM('PARTICULIER', 'ENTREPRISE', 'ASSOCIATION') NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE animal (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    espece          ENUM('CHAT', 'CHIEN', 'AUTRE') NOT NULL,
    race            VARCHAR(100),
    date_naissance  DATE,
    sexe            ENUM('MALE', 'FEMELLE') NOT NULL,
    description     TEXT,
    etat_sante      ENUM('BON', 'MOYEN', 'MALADE', 'EN_CONVALESCENCE') NOT NULL DEFAULT 'BON',
    statut          ENUM('DISPONIBLE', 'ADOPTE', 'EN_ATTENTE_ADOPTION', 'EN_SOIN', 'INDISPONIBLE')
                    NOT NULL DEFAULT 'DISPONIBLE',
    date_arrivee    DATE NOT NULL,
    photo           VARCHAR(255),          -- nom du fichier dans uploads/animaux (UUID.jpg|png|webp)
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 2. Tables avec cles etrangeres
-- ---------------------------------------------------------------------

-- Adoptant 1 --- 0..* Demande_Adoption (effectue)
-- Animal   1 --- 0..* Demande_Adoption (concerne)
CREATE TABLE demande_adoption (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    date_demande  DATE NOT NULL,
    motif         TEXT,
    statut        ENUM('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',
    adoptant_id   INT NOT NULL,
    animal_id     INT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_demande_adoptant FOREIGN KEY (adoptant_id) REFERENCES adoptant(id),
    CONSTRAINT fk_demande_animal   FOREIGN KEY (animal_id)   REFERENCES animal(id)
);

-- Adoptant 1 --- 0..* Adoption (realise)
-- Animal   1 --- 0..1 Adoption (est associe a)
--   => au plus UNE adoption non annulee par animal (regle verifiee dans AdoptionService,
--      afin qu'un animal rendu apres une adoption ANNULE puisse etre adopte de nouveau)
CREATE TABLE adoption (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    date_adoption  DATE NOT NULL,
    statut         ENUM('EN_COURS', 'CONFIRME', 'ANNULE') NOT NULL DEFAULT 'EN_COURS',
    commentaire    TEXT,
    adoptant_id    INT NOT NULL,
    animal_id      INT NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_adoption_adoptant FOREIGN KEY (adoptant_id) REFERENCES adoptant(id),
    CONSTRAINT fk_adoption_animal   FOREIGN KEY (animal_id)   REFERENCES animal(id)
);

-- Animal      1 --- 0..* Soin (recoit)
-- Utilisateur 1 --- 0..* Soin (realise : le veterinaire)
CREATE TABLE soin (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    date_soin         DATE NOT NULL,
    type              ENUM('CONSULTATION', 'VACCINATION') NOT NULL,
    motif             VARCHAR(255),
    diagnostic        TEXT,
    traitement        TEXT,
    prochaine_visite  DATE,
    remarque          TEXT,
    animal_id         INT NOT NULL,
    utilisateur_id    INT NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_soin_animal      FOREIGN KEY (animal_id)      REFERENCES animal(id),
    CONSTRAINT fk_soin_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

-- Donateur 1 --- 0..* Don (faire)
CREATE TABLE don (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    date_don     DATE NOT NULL,
    type_don     ENUM('ARGENT', 'NOURRITURE', 'MEDICAMENT', 'MATERIEL', 'AUTRE') NOT NULL,
    montant      DECIMAL(10,2),
    description  TEXT,
    donateur_id  INT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_don_montant CHECK (montant IS NULL OR montant >= 0),
    CONSTRAINT fk_don_donateur FOREIGN KEY (donateur_id) REFERENCES donateur(id)
);

-- ---------------------------------------------------------------------
-- 3. Donnees de test
-- Mots de passe de TEST (haches PBKDF2) :
--   admin@refuge.tn   / Admin@123
--   employe@refuge.tn / Employe@123
--   veto@refuge.tn    / Veto@123
-- A changer avant toute utilisation reelle.
-- ---------------------------------------------------------------------

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role) VALUES
('Ben Salah', 'Amira', 'admin@refuge.tn',
 'pbkdf2$65536$lnaRkv+kwgriA+w6Aqbe5A==$Gu3OObiyQzQJ3Jk2MANKAWwpciMxTpWQiQ1D07ZDo58=', '71000001', 'ADMIN'),
('Trabelsi', 'Youssef', 'employe@refuge.tn',
 'pbkdf2$65536$oFmswnpcaSoMrA5/LDA+CA==$6P2L4bZzmHBavVqir/67UGI7YznZZ4BC0A+rUv3DW/E=', '71000002', 'EMPLOYE'),
('Gharbi', 'Sana', 'veto@refuge.tn',
 'pbkdf2$65536$c9Mpsrwyl4d7KUOl+M3Pcg==$KM++QZcAV2+PIjNVpLeduM1gQ/kv4DiPtWTU7Uon9Dk=', '71000003', 'VETERINAIRE');

INSERT INTO animal (nom, espece, race, date_naissance, sexe, description, etat_sante, statut, date_arrivee, photo) VALUES
('Rex',   'CHIEN', 'Berger allemand', '2021-03-10', 'MALE',    'Chien calme et joueur',     'BON',              'DISPONIBLE',          '2026-01-15', NULL),
('Mimi',  'CHAT',  'Europeen',        '2023-06-01', 'FEMELLE', 'Chatte tres affectueuse',   'BON',              'EN_ATTENTE_ADOPTION', '2026-02-20', NULL),
('Rocky', 'CHIEN', 'Croise',          '2020-11-05', 'MALE',    'Blessure a la patte',       'MALADE',           'EN_SOIN',             '2026-03-02', NULL),
('Luna',  'CHAT',  'Siamois',         '2022-09-12', 'FEMELLE', 'Adoptee en septembre',      'BON',              'ADOPTE',              '2025-12-01', NULL),
('Coco',  'AUTRE', 'Lapin nain',      '2024-04-18', 'MALE',    'Lapin sociable',            'EN_CONVALESCENCE', 'DISPONIBLE',          '2026-05-10', NULL);

INSERT INTO adoptant (nom, prenom, email, adresse, telephone, date_inscription) VALUES
('Mansour', 'Karim', 'karim.mansour@mail.tn', 'Tunis, Ariana',  '98000001', '2026-02-01'),
('Jebali',  'Ines',  'ines.jebali@mail.tn',   'Sousse',         '98000002', '2026-03-15'),
('Haddad',  'Omar',  'omar.haddad@mail.tn',   'Sfax',           '98000003', '2026-08-20');

INSERT INTO demande_adoption (date_demande, motif, statut, adoptant_id, animal_id) VALUES
('2026-08-25', 'Grand jardin, experience avec les chats', 'ACCEPTEE',   1, 4),
('2026-10-01', 'Compagnie pour ma fille',                 'EN_ATTENTE', 2, 2),
('2026-10-05', 'Premier animal',                          'EN_ATTENTE', 3, 2);

INSERT INTO adoption (date_adoption, statut, commentaire, adoptant_id, animal_id) VALUES
('2026-09-02', 'CONFIRME', 'Adoption finalisee, suivi a 1 mois prevu', 1, 4);

INSERT INTO soin (date_soin, type, motif, diagnostic, traitement, prochaine_visite, remarque, animal_id, utilisateur_id) VALUES
('2026-03-03', 'CONSULTATION', 'Boiterie',       'Entorse patte avant droite', 'Anti-inflammatoire 7 jours', '2026-03-10', 'Repos strict', 3, 3),
('2026-01-20', 'VACCINATION',  'Vaccin rage',    NULL,                         'Vaccin antirabique',         '2027-01-20', NULL,           1, 3),
('2026-02-25', 'VACCINATION',  'Vaccin typhus',  NULL,                         'Vaccin TCL',                 '2027-02-25', NULL,           2, 3);

INSERT INTO donateur (nom, prenom, email, adresse, telephone, type) VALUES
('Ayari',         'Mehdi', 'mehdi.ayari@mail.tn',    'La Marsa', '22000001', 'PARTICULIER'),
('PetFood Tunisie', NULL,  'contact@petfood.tn',     'Ben Arous', '71900000', 'ENTREPRISE'),
('Amis des Animaux', NULL, 'info@amisanimaux.org',   'Tunis',     '71800000', 'ASSOCIATION');

INSERT INTO don (date_don, type_don, montant, description, donateur_id) VALUES
('2026-09-10', 'ARGENT',     200.00, 'Don mensuel',                 1),
('2026-09-15', 'NOURRITURE', NULL,   '50 kg de croquettes',         2),
('2026-10-01', 'MEDICAMENT', 150.00, 'Antiparasitaires (valeur)',   3);
