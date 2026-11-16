package ie.ucd.comp41670.meitheal.domain;

public record TimeCredit(int credits) {

    public TimeCredit {
        if (credits < 0) {
            throw new IllegalArgumentException(
                    "Credits cannot be negative"
            );
        }
    }

    public static TimeCredit fromMinutes(int minutes) {
        return new TimeCredit(minutes);
    }
}