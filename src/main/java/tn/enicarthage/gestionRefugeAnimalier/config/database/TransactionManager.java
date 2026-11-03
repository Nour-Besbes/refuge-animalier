package tn.enicarthage.gestionRefugeAnimalier.config.database;

import tn.enicarthage.gestionRefugeAnimalier.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Execute plusieurs operations SQL dans UNE seule transaction :
 * soit tout reussit (commit), soit tout est annule (rollback).
 * Utilise par les services quand une action metier modifie plusieurs tables
 * (ex. : enregistrer une adoption ET changer le statut de l'animal).
 */
public final class TransactionManager {

    @FunctionalInterface
    public interface Travail<T> {
        T executer(Connection connection);
    }

    private TransactionManager() {
    }

    public static <T> T executer(Travail<T> travail) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T resultat = travail.executer(connection);
                connection.commit();
                return resultat;
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la transaction : " + e.getMessage(), e);
        }
    }
}
