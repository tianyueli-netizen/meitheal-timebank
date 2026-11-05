package ie.ucd.comp41670.meitheal.domain;

/**
 * Represents a proposed settlement outcome.
 * Approved settlements must have positive labor credits and a levy between zero and
 * floor(5% of the labor credits). Rejected settlements are zero-valued.
 */
public record SettlementResult(
        int laborCredits,
        int actualLevyCredits,
        boolean isApproved
) {
    public SettlementResult {
        if (isApproved) {
            validateApprovedSettlement(laborCredits, actualLevyCredits);
        } else {
            if (laborCredits != 0 || actualLevyCredits != 0) {
                throw new IllegalArgumentException(
                        "Rejected settlements must have zero labor credits and zero levy."
                );
            }
        }
    }

    private static void validateApprovedSettlement(int laborCredits, int actualLevyCredits) {
        if (laborCredits <= 0) {
            throw new IllegalArgumentException("Approved settlements must have positive labor credits.");
        }
        int maxLevy = Math.floorDiv(laborCredits, 20);
        if (actualLevyCredits < 0 || actualLevyCredits > maxLevy) {
            throw new IllegalArgumentException(
                    "Approved settlements require a levy between 0 and floor(5% of labor credits)."
            );
        }
    }
}