package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Soin;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeSoin;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table soin. */
public class SoinDAO {

    public Soin create(Soin soin) {
        String sql = "INSERT INTO soin (date_soin, type, motif, diagnostic, traitement, prochaine_visite, "
                + "remarque, animal_id, utilisateur_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(statement, soin);
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    soin.setId(cles.getInt(1));
                }
            }
            return soin;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation du soin", e);
        }
    }

    public List<Soin> findAll() {
        return rechercher(null, null);
    }

    /** Historique medical d'un animal : tous ses soins, du plus recent au plus ancien. */
    public List<Soin> findByAnimalId(int animalId) {
        return rechercher(null, animalId);
    }

    public List<Soin> rechercher(TypeSoin type, Integer animalId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM soin WHERE 1 = 1");
        List<Object> parametres = new ArrayList<>();
        if (type != null) {
            sql.append(" AND type = ?");
            parametres.add(type.name());
        }
        if (animalId != null) {
            sql.append(" AND animal_id = ?");
            parametres.add(animalId);
        }
        sql.append(" ORDER BY date_soin DESC, id DESC");

        List<Soin> soins = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    soins.add(mapper(result));
                }
            }
            return soins;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des soins", e);
        }
    }

    public Soin findById(int id) {
        String sql = "SELECT * FROM soin WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture du soin", e);
        }
    }

    public void update(Soin soin) {
        String sql = "UPDATE soin SET date_soin = ?, type = ?, motif = ?, diagnostic = ?, traitement = ?, "
                + "prochaine_visite = ?, remarque = ?, animal_id = ?, utilisateur_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            remplir(statement, soin);
            statement.setInt(10, soin.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification du soin", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM soin WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Suppression du soin", e);
        }
    }

    private void remplir(PreparedStatement statement, Soin soin) throws SQLException {
        JdbcUtil.setDate(statement, 1, soin.getDateSoin());
        JdbcUtil.setEnum(statement, 2, soin.getType());
        statement.setString(3, soin.getMotif());
        statement.setString(4, soin.getDiagnostic());
        statement.setString(5, soin.getTraitement());
        JdbcUtil.setDate(statement, 6, soin.getProchaineVisite());
        statement.setString(7, soin.getRemarque());
        statement.setInt(8, soin.getAnimalId());
        statement.setInt(9, soin.getUtilisateurId());
    }

    private Soin mapper(ResultSet result) throws SQLException {
        Soin soin = new Soin();
        soin.setId(result.getInt("id"));
        soin.setDateSoin(JdbcUtil.getDate(result, "date_soin"));
        soin.setType(JdbcUtil.getEnum(result, "type", TypeSoin.class));
        soin.setMotif(result.getString("motif"));
        soin.setDiagnostic(result.getString("diagnostic"));
        soin.setTraitement(result.getString("traitement"));
        soin.setProchaineVisite(JdbcUtil.getDate(result, "prochaine_visite"));
        soin.setRemarque(result.getString("remarque"));
        soin.setAnimalId(result.getInt("animal_id"));
        soin.setUtilisateurId(result.getInt("utilisateur_id"));
        soin.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        soin.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return soin;
    }
}
