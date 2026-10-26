package ie.ucd.comp41670.meitheal.domain;

public record MemberId(String value) {
    public MemberId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        value = value.trim();
    }
}