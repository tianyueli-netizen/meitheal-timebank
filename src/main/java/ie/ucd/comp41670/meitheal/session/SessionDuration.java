package ie.ucd.comp41670.meitheal.session;

public record SessionDuration(int minutes) {
    public static final int MINUTES_PER_HOUR = 60;
    public static final int MAX_MINUTES = 8 * MINUTES_PER_HOUR;
    public static final int CREDITS_PER_HOUR = MINUTES_PER_HOUR;

    public SessionDuration {
        // R8: must be > 0 and <= 8 hours (480 minutes)
        if (minutes <= 0 || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException("R8 Violation: Duration must be > 0 and <= 480 minutes (8 hours)");
        }
    }

    public static SessionDuration ofHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be positive");
        }
        return new SessionDuration(hours * MINUTES_PER_HOUR);
    }

    public int toHoursRoundedDown() {
        return minutes / MINUTES_PER_HOUR;
    }
}

