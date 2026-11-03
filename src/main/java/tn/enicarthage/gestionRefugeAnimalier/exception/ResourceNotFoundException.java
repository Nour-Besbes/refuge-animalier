package tn.enicarthage.gestionRefugeAnimalier.exception;

/** Ressource introuvable -> HTTP 404 Not Found. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
