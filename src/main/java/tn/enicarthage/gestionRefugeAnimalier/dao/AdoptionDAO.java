package tn.enicarthage.gestionRefugeAnimalier.dao;

import tn.enicarthage.gestionRefugeAnimalier.config.database.DatabaseConnection;
import tn.enicarthage.gestionRefugeAnimalier.model.Adoption;
import tn.enicarthage.gestionRefugeAnimalier.model.StatutAdoption;
import tn.enicarthage.gestionRefugeAnimalier.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acces a la table adoption. */
public class AdoptionDAO {

    /** Version transactionnelle : l'adoption et le statut de l'animal changent ensemble. */
    public Adoption create(Connection connection, Adoption adoption) {
        String sql = "INSERT INTO adoption (date_adoption, statut, commentaire, adoptant_id, animal_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(statement, adoption);
            statement.executeUpdate();
            try (ResultSet cles = statement.getGeneratedKeys()) {
                if (cles.next()) {
                    adoption.setId(cles.getInt(1));
                }
            }
            return adoption;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Creation de l'adoption", e);
        }
    }

    public List<Adoption> findAll() {
        return rechercher(null, null, null, null);
    }

    /**
     * Recherche d'adoptions. motCle porte sur le nom/prenom de l'adoptant
     * ou le nom de l'animal (jointures). Chaque critere null est ignore.
     */
    public List<Adoption> rechercher(String motCle, StatutAdoption statut, Integer adoptantId, Integer animalId) {
        StringBuilder sql = new StringBuilder(
                "SELECT ad.* FROM adoption ad "
                        + "JOIN adoptant ap ON ad.adoptant_id = ap.id "
                        + "JOIN animal an ON ad.animal_id = an.id WHERE 1 = 1");
        List<Object> parametres = new ArrayList<>();
        if (motCle != null) {
            sql.append(" AND (ap.nom LIKE ? OR ap.prenom LIKE ? OR an.nom LIKE ?)");
            for (int i = 0; i < 3; i++) {
                parametres.add("%" + motCle + "%");
            }
        }
        if (statut != null) {
            sql.append(" AND ad.statut = ?");
            parametres.add(statut.name());
        }
        if (adoptantId != null) {
            sql.append(" AND ad.adoptant_id = ?");
            parametres.add(adoptantId);
        }
        if (animalId != null) {
            sql.append(" AND ad.animal_id = ?");
            parametres.add(animalId);
        }
        sql.append(" ORDER BY ad.date_adoption DESC, ad.id DESC");

        List<Adoption> adoptions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            JdbcUtil.setParametres(statement, parametres);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    adoptions.add(mapper(result));
                }
            }
            return adoptions;
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture des adoptions", e);
        }
    }

    public Adoption findById(int id) {
        return findUnique("SELECT * FROM adoption WHERE id = ?", id);
    }

    /** Adoption non annulee (EN_COURS ou CONFIRME) d'un animal, ou null. */
    public Adoption findActiveByAnimalId(int animalId) {
        return findUnique("SELECT * FROM adoption WHERE animal_id = ? AND statut <> 'ANNULE'", animalId);
    }

    /** Version transactionnelle. */
    public void update(Connection connection, Adoption adoption) {
        String sql = "UPDATE adoption SET date_adoption = ?, statut = ?, commentaire = ?, "
                + "adoptant_id = ?, animal_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            remplir(statement, adoption);
            statement.setInt(6, adoption.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Modification de l'adoption", e);
        }
    }

    public int compterParStatut(StatutAdoption statut) {
        String sql = "SELECT COUNT(*) FROM adoption WHERE statut = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            JdbcUtil.setEnum(statement, 1, statut);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Comptage des adoptions", e);
        }
    }

    private Adoption findUnique(String sql, int valeur) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, valeur);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapper(result) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduire("Lecture de l'adoption", e);
        }
    }

    private void remplir(PreparedStatement statement, Adoption adoption) throws SQLException {
        JdbcUtil.setDate(statement, 1, adoption.getDateAdoption());
        JdbcUtil.setEnum(statement, 2, adoption.getStatut());
        statement.setString(3, adoption.getCommentaire());
        statement.setInt(4, adoption.getAdoptantId());
        statement.setInt(5, adoption.getAnimalId());
    }

    private Adoption mapper(ResultSet result) throws SQLException {
        Adoption adoption = new Adoption();
        adoption.setId(result.getInt("id"));
        adoption.setDateAdoption(JdbcUtil.getDate(result, "date_adoption"));
        adoption.setStatut(JdbcUtil.getEnum(result, "statut", StatutAdoption.class));
        adoption.setCommentaire(result.getString("commentaire"));
        adoption.setAdoptantId(result.getInt("adoptant_id"));
        adoption.setAnimalId(result.getInt("animal_id"));
        adoption.setCreatedAt(JdbcUtil.getDateTime(result, "created_at"));
        adoption.setUpdatedAt(JdbcUtil.getDateTime(result, "updated_at"));
        return adoption;
    }
}
