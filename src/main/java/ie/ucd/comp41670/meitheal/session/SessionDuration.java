package ie.ucd.comp41670.meitheal.session;

public record SessionDuration(int minutes) {
    private static final int MAX_MINUTES = 8 * 60;

    public SessionDuration {
        if (minutes <= 0 || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException(
                    "Session duration must be greater than zero and no longer than 8 hours."
            );
        }
    }

    public SessionDuration(double hours) {
        this((int) Math.round(hours * 60));
    }

    public int toMinutes() {
        return minutes;
    }

    public double toHours() {
        return minutes / 60.0;
    }

    public int toCredits() {
        return minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    public static SessionDuration ofMinutes(int minutes) {
        return new SessionDuration(minutes);
    }

    public static SessionDuration ofHours(double hours) {
        return new SessionDuration(hours);
    }
}

