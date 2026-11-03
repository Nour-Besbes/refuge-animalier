package tn.enicarthage.gestionRefugeAnimalier.exception;

/**
 * Regle metier violee par rapport a l'etat actuel des donnees -> HTTP 409 Conflict.
 * Exemples : accepter une demande deja refusee, supprimer un animal deja adopte,
 * email deja utilise.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
