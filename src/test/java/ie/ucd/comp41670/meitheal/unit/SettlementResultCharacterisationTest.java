package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementResultCharacterisationTest {

    @Test
    void retainsProvidedValuesForValidInputs() {
        SettlementResult approvedSixty = new SettlementResult(60, 2, true);
        assertEquals(60, approvedSixty.laborCredits());
        assertEquals(2, approvedSixty.actualLevyCredits());
        assertTrue(approvedSixty.isApproved());

        SettlementResult approvedTwenty = new SettlementResult(20, 0, true);
        assertEquals(20, approvedTwenty.laborCredits());
        assertEquals(0, approvedTwenty.actualLevyCredits());
        assertTrue(approvedTwenty.isApproved());

        SettlementResult rejectedZero = new SettlementResult(0, 0, false);
        assertEquals(0, rejectedZero.laborCredits());
        assertEquals(0, rejectedZero.actualLevyCredits());
        assertFalse(rejectedZero.isApproved());
    }

    @Test
    void validatesLaborCreditsBeforeLevyWhenApprovedButInvalid() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SettlementResult(0, -1, true)
        );

        assertEquals("Approved settlements must have positive labor credits.", exception.getMessage());
    }

    @Test
    void throwsSpecificMessageForNegativeApprovedLevy() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SettlementResult(60, -1, true)
        );

        assertEquals(
                "Approved settlements require a levy between 0 and floor(5% of labor credits).",
                exception.getMessage()
        );
    }

    @Test
    void throwsSpecificMessageForRejectedNegativeValues() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SettlementResult(-1, -1, false)
        );

        assertEquals("Rejected settlements must have zero labor credits and zero levy.", exception.getMessage());
    }
}
