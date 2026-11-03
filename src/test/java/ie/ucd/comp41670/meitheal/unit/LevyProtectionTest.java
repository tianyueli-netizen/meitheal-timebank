package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import ie.ucd.comp41670.meitheal.domain.TimeCredit;
import ie.ucd.comp41670.meitheal.policy.TensionArbiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevyProtectionTest {

    private final TensionArbiter arbiter = new TensionArbiter();

    @Test
    void laborCostSixtyUsesTensionProtectionThresholds() {
        assertSettlement(100, 60, 3);
        assertSettlement(-117, 60, 3);
        assertSettlement(-118, 60, 2);
        assertSettlement(-119, 60, 1);
        assertSettlement(-120, 60, 0);

        SettlementResult rejected = arbiter.calculateSettlement(-121, new TimeCredit(60));
        assertFalse(rejected.isApproved());
        assertEquals(0, rejected.laborCredits());
        assertEquals(0, rejected.actualLevyCredits());
    }

    @Test
    void roundingBoundariesFollowFivePercentFloorRule() {
        assertSettlement(500, 19, 0);
        assertSettlement(500, 20, 1);
        assertSettlement(500, 39, 1);
        assertSettlement(500, 40, 2);
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(NullPointerException.class, () -> arbiter.calculateSettlement(100, null));
        assertThrows(IllegalArgumentException.class, () -> arbiter.calculateSettlement(100, new TimeCredit(0)));
        assertThrows(IllegalArgumentException.class, () -> arbiter.calculateSettlement(-181, new TimeCredit(1)));
    }

    @Test
    void integerExtremesRemainSafe() {
        SettlementResult atMinimumBalance = arbiter.calculateSettlement(-180, new TimeCredit(Integer.MAX_VALUE));
        assertFalse(atMinimumBalance.isApproved());
        assertEquals(0, atMinimumBalance.laborCredits());
        assertEquals(0, atMinimumBalance.actualLevyCredits());

        SettlementResult atMaxBalance = arbiter.calculateSettlement(Integer.MAX_VALUE, new TimeCredit(20));
        assertTrue(atMaxBalance.isApproved());
        assertEquals(20, atMaxBalance.laborCredits());
        assertEquals(1, atMaxBalance.actualLevyCredits());

        int labor = Integer.MAX_VALUE;
        int balance = Integer.MAX_VALUE;
        SettlementResult result = arbiter.calculateSettlement(balance, new TimeCredit(labor));

        assertTrue(result.isApproved());
        assertEquals(labor, result.laborCredits());
        assertEquals(180, result.actualLevyCredits());
    }

    private void assertSettlement(int startingBalance, int laborCredits, int expectedLevy) {
        SettlementResult result = arbiter.calculateSettlement(startingBalance, new TimeCredit(laborCredits));

        assertTrue(result.isApproved(), "Approved settlement expected for valid balance and labor");
        assertEquals(laborCredits, result.laborCredits());
        assertEquals(expectedLevy, result.actualLevyCredits());
    }
}
