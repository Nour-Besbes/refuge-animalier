package tn.enicarthage.gestionRefugeAnimalier.util;

import tn.enicarthage.gestionRefugeAnimalier.exception.ConflictException;
import tn.enicarthage.gestionRefugeAnimalier.exception.DataAccessException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Petites methodes reutilisees par tous les DAO :
 * conversions LocalDate <-> java.sql.Date (valeurs null comprises),
 * enum <-> texte, et traduction des SQLException.
 */
public final class JdbcUtil {

    private JdbcUtil() {
    }

    public static void setDate(PreparedStatement statement, int index, LocalDate date) throws SQLException {
        if (date == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    public static LocalDate getDate(ResultSet result, String colonne) throws SQLException {
        Date date = result.getDate(colonne);
        return date == null ? null : date.toLocalDate();
    }

    public static LocalDateTime getDateTime(ResultSet result, String colonne) throws SQLException {
        Timestamp timestamp = result.getTimestamp(colonne);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public static void setEnum(PreparedStatement statement, int index, Enum<?> valeur) throws SQLException {
        if (valeur == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, valeur.name());
        }
    }

    public static <E extends Enum<E>> E getEnum(ResultSet result, String colonne, Class<E> type)
            throws SQLException {
        String valeur = result.getString(colonne);
        return valeur == null ? null : Enum.valueOf(type, valeur);
    }

    /** Affecte une liste de parametres (requetes de recherche construites dynamiquement). */
    public static void setParametres(PreparedStatement statement, List<Object> parametres) throws SQLException {
        for (int i = 0; i < parametres.size(); i++) {
            statement.setObject(i + 1, parametres.get(i));
        }
    }

    /**
     * Traduit une SQLException :
     * - violation de contrainte (cle etrangere, UNIQUE) -> ConflictException (409)
     * - autre erreur SQL -> DataAccessException (500)
     */
    public static RuntimeException traduire(String operation, SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            String detail = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (detail.contains("foreign key")) {
                return new ConflictException(operation
                        + " impossible : cet element est lie a d'autres donnees (cle etrangere).");
            }
            if (detail.contains("duplicate")) {
                return new ConflictException(operation + " impossible : une valeur unique est deja utilisee.");
            }
            return new ConflictException(operation + " impossible : contrainte d'integrite violee.");
        }
        return new DataAccessException(operation + " : erreur SQL (" + e.getMessage() + ")", e);
    }
}
