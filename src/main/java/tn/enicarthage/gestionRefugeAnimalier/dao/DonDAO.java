package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Don;
import tn.enicarthage.gestionRefugeAnimalier.model.TypeDon;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table don. */
public class DonDAO {

    public Don create(Don don) {
        String sql = "INSERT INTO don (date_don, type_don, montant, description, donateur_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(statement, don);
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    don.setId(cles.getInt(1));
                }
            }
            return don;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation du don", e);
        }
    }

    public List<Don> findAll() {
        return rechercher(null, null);
    }

    public List<Don> findByDonateurId(int donateurId) {
        return rechercher(null, donateurId);
    }

    public List<Don> rechercher(TypeDon typeDon, Integer donateurId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM don WHERE 1 = 1");
        List<Object> parametres = new ArrayList<>();
        if (typeDon != null) {
            sql.append(" AND type_don = ?");
            parametres.add(typeDon.name());
        }
        if (donateurId != null) {
            sql.append(" AND donateur_id = ?");
            parametres.add(donateurId);
        }
        sql.append(" ORDER BY date_don DESC, id DESC");

        List<Don> dons = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    dons.add(mapper(result));
                }
            }
            return dons;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des dons", e);
        }
    }

    public Don findById(int id) {
        String sql = "SELECT * FROM don WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture du don", e);
        }
    }

    public void update(Don don) {
        String sql = "UPDATE don SET date_don = ?, type_don = ?, montant = ?, description = ?, "
                + "donateur_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            remplir(statement, don);
            statement.setInt(6, don.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification du don", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM don WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Suppression du don", e);
        }
    }

    private void remplir(PreparedStatement statement, Don don) throws SQLException {
        JdbcUtil.setDate(statement, 1, don.getDateDon());
        JdbcUtil.setEnum(statement, 2, don.getTypeDon());
        statement.setBigDecimal(3, don.getMontant());
        statement.setString(4, don.getDescription());
        statement.setInt(5, don.getDonateurId());
    }

    private Don mapper(ResultSet result) throws SQLException {
        Don don = new Don();
        don.setId(result.getInt("id"));
        don.setDateDon(JdbcUtil.getDate(result, "date_don"));
        don.setTypeDon(JdbcUtil.getEnum(result, "type_don", TypeDon.class));
        don.setMontant(result.getBigDecimal("montant"));
        don.setDescription(result.getString("description"));
        don.setDonateurId(result.getInt("donateur_id"));
        don.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        don.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return don;
    }
}
