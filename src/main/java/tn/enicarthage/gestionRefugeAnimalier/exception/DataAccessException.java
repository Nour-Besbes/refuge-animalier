package tn.enicarthage.gestionRefugeAnimalier.exception;

/**
 * Enveloppe une SQLException levee dans un DAO -> HTTP 500.
 * Evite le e.printStackTrace() silencieux : l'erreur remonte jusqu'au Router.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
