-- =====================================================================
--  Requetes de verification (demandees par le guide : SELECT, WHERE,
--  INSERT, UPDATE, DELETE, JOIN). A executer dans phpMyAdmin apres database.sql.
-- =====================================================================
USE gestion_refuge_animalier;

-- SELECT : tous les animaux
SELECT * FROM animal;

-- WHERE : animaux disponibles a l'adoption
SELECT id, nom, espece, race FROM animal WHERE statut = 'DISPONIBLE';

-- WHERE : demandes d'adoption en attente
SELECT * FROM demande_adoption WHERE statut = 'EN_ATTENTE';

-- JOIN : demandes avec le nom de l'adoptant et de l'animal
SELECT d.id, d.date_demande, d.statut,
       CONCAT(ap.prenom, ' ', ap.nom) AS adoptant,
       an.nom AS animal
FROM demande_adoption d
JOIN adoptant ap ON d.adoptant_id = ap.id
JOIN animal an   ON d.animal_id = an.id
ORDER BY d.date_demande DESC;

-- JOIN : historique medical d'un animal (id = 3) avec le veterinaire
SELECT s.date_soin, s.type, s.motif, s.diagnostic, s.traitement,
       CONCAT(u.prenom, ' ', u.nom) AS veterinaire
FROM soin s
JOIN utilisateur u ON s.utilisateur_id = u.id
WHERE s.animal_id = 3
ORDER BY s.date_soin DESC;

-- JOIN + agregation : total des dons en argent par donateur
SELECT dn.nom, dn.type, SUM(d.montant) AS total_argent
FROM don d
JOIN donateur dn ON d.donateur_id = dn.id
WHERE d.type_don = 'ARGENT'
GROUP BY dn.id, dn.nom, dn.type;

-- Indicateurs du tableau de bord
SELECT
    (SELECT COUNT(*) FROM animal)                                  AS total_animaux,
    (SELECT COUNT(*) FROM animal WHERE statut = 'DISPONIBLE')      AS disponibles,
    (SELECT COUNT(*) FROM animal WHERE statut = 'ADOPTE')          AS adoptes,
    (SELECT COUNT(*) FROM animal WHERE statut = 'EN_SOIN')         AS en_soins,
    (SELECT COUNT(*) FROM demande_adoption WHERE statut = 'EN_ATTENTE') AS demandes_en_attente,
    (SELECT COUNT(*) FROM adoption WHERE statut = 'CONFIRME')      AS adoptions_realisees,
    (SELECT COUNT(*) FROM donateur)                                AS donateurs,
    (SELECT COUNT(*) FROM utilisateur)                             AS utilisateurs;

-- INSERT / UPDATE / DELETE (exemple sur un adoptant de test)
INSERT INTO adoptant (nom, prenom, email, adresse, telephone, date_inscription)
VALUES ('Test', 'Requete', 'test.requete@mail.tn', 'Nabeul', '50000000', CURRENT_DATE);

UPDATE adoptant SET adresse = 'Hammamet' WHERE email = 'test.requete@mail.tn';

DELETE FROM adoptant WHERE email = 'test.requete@mail.tn';
