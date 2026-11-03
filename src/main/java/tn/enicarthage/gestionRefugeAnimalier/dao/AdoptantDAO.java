package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Adoptant;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table adoptant. */
public class AdoptantDAO {

    public Adoptant create(Adoptant adoptant) {
        String sql = "INSERT INTO adoptant (nom, prenom, email, adresse, telephone, date_inscription) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(statement, adoptant);
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    adoptant.setId(cles.getInt(1));
                }
            }
            return adoptant;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation de l'adoptant", e);
        }
    }

    public List<Adoptant> findAll() {
        return rechercher(null);
    }

    /** Recherche par nom, prenom, email ou telephone (motCle null = tous). */
    public List<Adoptant> rechercher(String motCle) {
        String sql = "SELECT * FROM adoptant";
        List<Object> parametres = new ArrayList<>();
        if (motCle != null) {
            sql += " WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? OR telephone LIKE ?";
            for (int i = 0; i < 4; i++) {
                parametres.add("%" + motCle + "%");
            }
        }
        sql += " ORDER BY nom, prenom";

        List<Adoptant> adoptants = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    adoptants.add(mapper(result));
                }
            }
            return adoptants;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des adoptants", e);
        }
    }

    public Adoptant findById(int id) {
        return findUnique("SELECT * FROM adoptant WHERE id = ?", id);
    }

    public Adoptant findByEmail(String email) {
        return findUnique("SELECT * FROM adoptant WHERE email = ?", email);
    }

    public void update(Adoptant adoptant) {
        String sql = "UPDATE adoptant SET nom = ?, prenom = ?, email = ?, adresse = ?, telephone = ?, "
                + "date_inscription = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            remplir(statement, adoptant);
            statement.setInt(7, adoptant.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification de l'adoptant", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM adoptant WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Suppression de l'adoptant", e);
        }
    }

    private Adoptant findUnique(String sql, Object valeur) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valeur);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture de l'adoptant", e);
        }
    }

    private void remplir(PreparedStatement statement, Adoptant adoptant) throws SQLException {
        statement.setString(1, adoptant.getNom());
        statement.setString(2, adoptant.getPrenom());
        statement.setString(3, adoptant.getEmail());
        statement.setString(4, adoptant.getAdresse());
        statement.setString(5, adoptant.getTelephone());
        JdbcUtil.setDate(statement, 6, adoptant.getDateInscription());
    }

    private Adoptant mapper(ResultSet result) throws SQLException {
        Adoptant adoptant = new Adoptant();
        adoptant.setId(result.getInt("id"));
        adoptant.setNom(result.getString("nom"));
        adoptant.setPrenom(result.getString("prenom"));
        adoptant.setEmail(result.getString("email"));
        adoptant.setAdresse(result.getString("adresse"));
        adoptant.setTelephone(result.getString("telephone"));
        adoptant.setDateInscription(JdbcUtil.getDate(result, "date_inscription"));
        adoptant.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        adoptant.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return adoptant;
    }
}
