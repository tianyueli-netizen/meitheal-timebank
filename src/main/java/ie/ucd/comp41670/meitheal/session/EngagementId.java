package ie.ucd.comp41670.meitheal.session;

public record EngagementId(String value) {
    public EngagementId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Engagement ID cannot be null or blank");
        }
    }
}
