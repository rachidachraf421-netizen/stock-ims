package ma.esith.stockims.exception;

public class ReferenceDejaUtiliseeException extends RuntimeException {
    public ReferenceDejaUtiliseeException(String reference) {
        super("La référence « " + reference + " » existe déjà.");
    }
}
