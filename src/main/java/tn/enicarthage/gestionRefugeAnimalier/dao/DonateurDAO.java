package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Donateur;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDonateur;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table donateur. */
public class DonateurDAO {

    public Donateur create(Donateur donateur) {
        String sql = "INSERT INTO donateur (nom, prenom, email, adresse, telephone, type) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(statement, donateur);
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    donateur.setId(cles.getInt(1));
                }
            }
            return donateur;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation du donateur", e);
        }
    }

    public List<Donateur> findAll() {
        return rechercher(null, null);
    }

    /** Recherche par mot-cle (nom, prenom, email, telephone) et/ou par type. */
    public List<Donateur> rechercher(String motCle, TypeDonateur type) {
        StringBuilder sql = new StringBuilder("SELECT * FROM donateur WHERE 1 = 1");
        List<Object> parametres = new ArrayList<>();
        if (motCle != null) {
            sql.append(" AND (nom LIKE ? OR prenom LIKE ? OR email LIKE ? OR telephone LIKE ?)");
            for (int i = 0; i < 4; i++) {
                parametres.add("%" + motCle + "%");
            }
        }
        if (type != null) {
            sql.append(" AND type = ?");
            parametres.add(type.name());
        }
        sql.append(" ORDER BY nom");

        List<Donateur> donateurs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    donateurs.add(mapper(result));
                }
            }
            return donateurs;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des donateurs", e);
        }
    }

    public Donateur findById(int id) {
        return findUnique("SELECT * FROM donateur WHERE id = ?", id);
    }

    public Donateur findByEmail(String email) {
        return findUnique("SELECT * FROM donateur WHERE email = ?", email);
    }

    public void update(Donateur donateur) {
        String sql = "UPDATE donateur SET nom = ?, prenom = ?, email = ?, adresse = ?, telephone = ?, "
                + "type = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            remplir(statement, donateur);
            statement.setInt(7, donateur.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification du donateur", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM donateur WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Suppression du donateur", e);
        }
    }

    public int compterTous() {
        String sql = "SELECT COUNT(*) FROM donateur";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Comptage des donateurs", e);
        }
    }

    private Donateur findUnique(String sql, Object valeur) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valeur);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture du donateur", e);
        }
    }

    private void remplir(PreparedStatement statement, Donateur donateur) throws SQLException {
        statement.setString(1, donateur.getNom());
        statement.setString(2, donateur.getPrenom());
        statement.setString(3, donateur.getEmail());
        statement.setString(4, donateur.getAdresse());
        statement.setString(5, donateur.getTelephone());
        JdbcUtil.setEnum(statement, 6, donateur.getType());
    }

    private Donateur mapper(ResultSet result) throws SQLException {
        Donateur donateur = new Donateur();
        donateur.setId(result.getInt("id"));
        donateur.setNom(result.getString("nom"));
        donateur.setPrenom(result.getString("prenom"));
        donateur.setEmail(result.getString("email"));
        donateur.setAdresse(result.getString("adresse"));
        donateur.setTelephone(result.getString("telephone"));
        donateur.setType(JdbcUtil.getEnum(result, "type", TypeDonateur.class));
        donateur.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        donateur.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return donateur;
    }
}
