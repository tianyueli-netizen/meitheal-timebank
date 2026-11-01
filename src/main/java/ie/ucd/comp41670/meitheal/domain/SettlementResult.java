package ie.ucd.comp41670.meitheal.domain;

public record SettlementResult(
        int laborCredits,
        int actualLevyCredits,
        boolean isApproved
) {}