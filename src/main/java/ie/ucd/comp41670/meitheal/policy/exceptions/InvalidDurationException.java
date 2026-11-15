package ie.ucd.comp41670.meitheal.policy.exceptions;

/**
 * Signals that a requested session duration is invalid for the business rules.
 */
public class InvalidDurationException extends IllegalArgumentException {
    public InvalidDurationException(String message) {
        super(message);
    }
}
