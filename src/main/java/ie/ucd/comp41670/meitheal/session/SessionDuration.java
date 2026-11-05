package ie.ucd.comp41670.meitheal.session;

public record SessionDuration(int minutes) {
    public SessionDuration {
        // R8: must be > 0 and <= 8 hours (480 minutes)
        if (minutes <= 0 || minutes > 480) {
            throw new IllegalArgumentException("R8 Violation: Duration must be > 0 and <= 480 minutes (8 hours)");
        }
    }

    public static SessionDuration ofHours(int hours) {
        return new SessionDuration(hours * 60);
    }

    public int toHoursRoundedDown() {
        return minutes / 60;
    }
}

