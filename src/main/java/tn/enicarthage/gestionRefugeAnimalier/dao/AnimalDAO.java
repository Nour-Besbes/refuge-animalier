package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Animal;
import tn.enicarthage.gestionRefugeAnimalier.model.Espece;
import tn.enicarthage.gestionRefugeAnimalier.model.EtatSante;
import tn.enicarthage.gestionRefugeAnimalier.model.Sexe;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAnimal;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table animal. */
public class AnimalDAO {

    public Animal create(Animal animal) {
        String sql = "INSERT INTO animal (nom, espece, race, date_naissance, sexe, description, "
                + "etat_sante, statut, date_arrivee, photo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(statement, animal);
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    animal.setId(cles.getInt(1));
                }
            }
            return animal;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation de l'animal", e);
        }
    }

    public List<Animal> findAll() {
        return rechercher(null, null, null);
    }

    /** Recherche multicritere : chaque critere null est ignore. */
    public List<Animal> rechercher(String nom, Espece espece, StatutAnimal statut) {
        StringBuilder sql = new StringBuilder("SELECT * FROM animal WHERE 1 = 1");
        List<Object> parametres = new ArrayList<>();
        if (nom != null) {
            sql.append(" AND (nom LIKE ? OR race LIKE ?)");
            parametres.add("%" + nom + "%");
            parametres.add("%" + nom + "%");
        }
        if (espece != null) {
            sql.append(" AND espece = ?");
            parametres.add(espece.name());
        }
        if (statut != null) {
            sql.append(" AND statut = ?");
            parametres.add(statut.name());
        }
        sql.append(" ORDER BY id");

        List<Animal> animaux = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    animaux.add(mapper(result));
                }
            }
            return animaux;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des animaux", e);
        }
    }

    public Animal findById(int id) {
        String sql = "SELECT * FROM animal WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture de l'animal", e);
        }
    }

    public void update(Animal animal) {
        String sql = "UPDATE animal SET nom = ?, espece = ?, race = ?, date_naissance = ?, sexe = ?, "
                + "description = ?, etat_sante = ?, statut = ?, date_arrivee = ?, photo = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            remplir(statement, animal);
            statement.setInt(11, animal.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification de l'animal", e);
        }
    }

    /** Version transactionnelle : utilise la connexion fournie par le service. */
    public void updateStatut(Connection connection, int id, StatutAnimal statut) {
        String sql = "UPDATE animal SET statut = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            JdbcUtil.setEnum(statement, 1, statut);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Changement de statut de l'animal", e);
        }
    }

    /** Met a jour uniquement le nom du fichier photo (null = pas de photo). */
    public void updatePhoto(int id, String photo) {
        String sql = "UPDATE animal SET photo = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, photo);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification de la photo", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM animal WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Suppression de l'animal", e);
        }
    }

    public int compterTous() {
        return compter("SELECT COUNT(*) FROM animal", null);
    }

    public int compterParStatut(StatutAnimal statut) {
        return compter("SELECT COUNT(*) FROM animal WHERE statut = ?", statut.name());
    }

    private int compter(String sql, String parametre) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parametre != null) {
                statement.setString(1, parametre);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Comptage des animaux", e);
        }
    }

    private void remplir(PreparedStatement statement, Animal animal) throws SQLException {
        statement.setString(1, animal.getNom());
        JdbcUtil.setEnum(statement, 2, animal.getEspece());
        statement.setString(3, animal.getRace());
        JdbcUtil.setDate(statement, 4, animal.getDateNaissance());
        JdbcUtil.setEnum(statement, 5, animal.getSexe());
        statement.setString(6, animal.getDescription());
        JdbcUtil.setEnum(statement, 7, animal.getEtatSante());
        JdbcUtil.setEnum(statement, 8, animal.getStatut());
        JdbcUtil.setDate(statement, 9, animal.getDateArrivee());
        statement.setString(10, animal.getPhoto());
    }

    private Animal mapper(ResultSet result) throws SQLException {
        Animal animal = new Animal();
        animal.setId(result.getInt("id"));
        animal.setNom(result.getString("nom"));
        animal.setEspece(JdbcUtil.getEnum(result, "espece", Espece.class));
        animal.setRace(result.getString("race"));
        animal.setDateNaissance(JdbcUtil.getDate(result, "date_naissance"));
        animal.setSexe(JdbcUtil.getEnum(result, "sexe", Sexe.class));
        animal.setDescription(result.getString("description"));
        animal.setEtatSante(JdbcUtil.getEnum(result, "etat_sante", EtatSante.class));
        animal.setStatut(JdbcUtil.getEnum(result, "statut", StatutAnimal.class));
        animal.setDateArrivee(JdbcUtil.getDate(result, "date_arrivee"));
        animal.setPhoto(result.getString("photo"));
        animal.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        animal.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return animal;
    }
}
