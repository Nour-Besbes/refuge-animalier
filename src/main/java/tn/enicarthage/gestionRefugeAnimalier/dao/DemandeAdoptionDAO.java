package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.DemandeAdoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutDemande;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table demande_adoption. */
public class DemandeAdoptionDAO {

    /** Version transactionnelle : la demande et le statut de l'animal changent ensemble. */
    public DemandeAdoption create(Connection connection, DemandeAdoption demande) {
        String sql = "INSERT INTO demande_adoption (date_demande, motif, statut, adoptant_id, animal_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            JdbcUtil.setDate(statement, 1, demande.getDateDemande());
            statement.setString(2, demande.getMotif());
            JdbcUtil.setEnum(statement, 3, demande.getStatut());
            statement.setInt(4, demande.getAdoptantId());
            statement.setInt(5, demande.getAnimalId());
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    demande.setId(cles.getInt(1));
                }
            }
            return demande;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation de la demande d'adoption", e);
        }
    }

    public List<DemandeAdoption> findAll() {
        return rechercher(null, null, null);
    }

    /** Filtre par statut, adoptant et/ou animal (null = critere ignore). */
    public List<DemandeAdoption> rechercher(StatutDemande statut, Integer adoptantId, Integer animalId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM demande_adoption WHERE 1 = 1");
        List<Object> parametres = new ArrayList<>();
        if (statut != null) {
            sql.append(" AND statut = ?");
            parametres.add(statut.name());
        }
        if (adoptantId != null) {
            sql.append(" AND adoptant_id = ?");
            parametres.add(adoptantId);
        }
        if (animalId != null) {
            sql.append(" AND animal_id = ?");
            parametres.add(animalId);
        }
        sql.append(" ORDER BY date_demande DESC, id DESC");

        List<DemandeAdoption> demandes = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    demandes.add(mapper(result));
                }
            }
            return demandes;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des demandes d'adoption", e);
        }
    }

    public DemandeAdoption findById(int id) {
        String sql = "SELECT * FROM demande_adoption WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture de la demande d'adoption", e);
        }
    }

    public void updateStatut(Connection connection, int id, StatutDemande statut) {
        String sql = "UPDATE demande_adoption SET statut = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            JdbcUtil.setEnum(statement, 1, statut);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Changement de statut de la demande", e);
        }
    }

    /** Refuse toutes les autres demandes EN_ATTENTE du meme animal (apres une acceptation). */
    public int refuserAutresDemandesEnAttente(Connection connection, int animalId, int demandeAccepteeId) {
        String sql = "UPDATE demande_adoption SET statut = 'REFUSEE' "
                + "WHERE animal_id = ? AND id <> ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, animalId);
            statement.setInt(2, demandeAccepteeId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Refus des autres demandes", e);
        }
    }

    /** Nombre de demandes d'un animal ayant un statut donne (lu dans la transaction en cours). */
    public int compterParAnimalEtStatut(Connection connection, int animalId, StatutDemande statut) {
        String sql = "SELECT COUNT(*) FROM demande_adoption WHERE animal_id = ? AND statut = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, animalId);
            JdbcUtil.setEnum(statement, 2, statut);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Comptage des demandes", e);
        }
    }

    public boolean existe(int adoptantId, int animalId, StatutDemande statut) {
        String sql = "SELECT COUNT(*) FROM demande_adoption WHERE adoptant_id = ? AND animal_id = ? AND statut = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, adoptantId);
            statement.setInt(2, animalId);
            JdbcUtil.setEnum(statement, 3, statut);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Verification des demandes", e);
        }
    }

    public int compterParStatut(StatutDemande statut) {
        String sql = "SELECT COUNT(*) FROM demande_adoption WHERE statut = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            JdbcUtil.setEnum(statement, 1, statut);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Comptage des demandes", e);
        }
    }

    private DemandeAdoption mapper(ResultSet result) throws SQLException {
        DemandeAdoption demande = new DemandeAdoption();
        demande.setId(result.getInt("id"));
        demande.setDateDemande(JdbcUtil.getDate(result, "date_demande"));
        demande.setMotif(result.getString("motif"));
        demande.setStatut(JdbcUtil.getEnum(result, "statut", StatutDemande.class));
        demande.setAdoptantId(result.getInt("adoptant_id"));
        demande.setAnimalId(result.getInt("animal_id"));
        demande.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        demande.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return demande;
    }
}
