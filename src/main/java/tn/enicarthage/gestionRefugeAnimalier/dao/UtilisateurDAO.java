package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Role;
import tn.enicarthage.gestionRefugeAnimalier.model.Utilisateur;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table utilisateur. */
public class UtilisateurDAO {

    public Utilisateur create(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, utilisateur.getNom());
            statement.setString(2, utilisateur.getPrenom());
            statement.setString(3, utilisateur.getEmail());
            statement.setString(4, utilisateur.getMotDePasse());
            statement.setString(5, utilisateur.getTelephone());
            JdbcUtil.setEnum(statement, 6, utilisateur.getRole());
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    utilisateur.setId(cles.getInt(1));
                }
            }
            return utilisateur;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation de l'utilisateur", e);
        }
    }

    public List<Utilisateur> findAll() {
        String sql = "SELECT * FROM utilisateur ORDER BY nom, prenom";
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                utilisateurs.add(mapper(result));
            }
            return utilisateurs;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des utilisateurs", e);
        }
    }

    public Utilisateur findById(int id) {
        return findUnique("SELECT * FROM utilisateur WHERE id = ?", id);
    }

    public Utilisateur findByEmail(String email) {
        return findUnique("SELECT * FROM utilisateur WHERE email = ?", email);
    }

    public void update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, "
                + "telephone = ?, role = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, utilisateur.getNom());
            statement.setString(2, utilisateur.getPrenom());
            statement.setString(3, utilisateur.getEmail());
            statement.setString(4, utilisateur.getMotDePasse());
            statement.setString(5, utilisateur.getTelephone());
            JdbcUtil.setEnum(statement, 6, utilisateur.getRole());
            statement.setInt(7, utilisateur.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification de l'utilisateur", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Suppression de l'utilisateur", e);
        }
    }

    public int compterTous() {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Comptage des utilisateurs", e);
        }
    }

    private Utilisateur findUnique(String sql, Object valeur) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valeur);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture de l'utilisateur", e);
        }
    }

    private Utilisateur mapper(ResultSet result) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(result.getInt("id"));
        utilisateur.setNom(result.getString("nom"));
        utilisateur.setPrenom(result.getString("prenom"));
        utilisateur.setEmail(result.getString("email"));
        utilisateur.setMotDePasse(result.getString("mot_de_passe"));
        utilisateur.setTelephone(result.getString("telephone"));
        utilisateur.setRole(JdbcUtil.getEnum(result, "role", Role.class));
        utilisateur.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        utilisateur.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return utilisateur;
    }
}
