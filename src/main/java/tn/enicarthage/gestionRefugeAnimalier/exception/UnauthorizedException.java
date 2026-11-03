package tn.enicarthage.gestionRefugeAnimalier.exception;

/** Utilisateur non authentifie ou identifiants incorrects -> HTTP 401 Unauthorized. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
