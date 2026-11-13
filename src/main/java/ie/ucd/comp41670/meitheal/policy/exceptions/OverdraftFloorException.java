package ie.ucd.comp41670.meitheal.policy.exceptions;

public class OverdraftFloorException extends IllegalArgumentException {
    public OverdraftFloorException(String message) {
        super(message);
    }
}
