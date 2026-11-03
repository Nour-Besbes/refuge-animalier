package tn.enicarthage.gestionRefugeAnimalier.exception;

/** Utilisateur authentifie mais dont le role n'autorise pas l'action -> HTTP 403 Forbidden. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
