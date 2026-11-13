package ie.ucd.comp41670.meitheal.policy.exceptions;

/**
 * Signals that a member attempted to fulfil their own request.
 */
public class SelfFulfilmentException extends IllegalArgumentException {
    public SelfFulfilmentException(String message) {
        super(message);
    }
}
