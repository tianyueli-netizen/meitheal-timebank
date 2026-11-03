package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettlementResultTest {

    @Test
    void approvedSettlementAllowsPositiveLaborAndWithinLevyLimit() {
        assertDoesNotThrow(() -> new SettlementResult(100, 5, true));
        assertDoesNotThrow(() -> new SettlementResult(60, 2, true));
        assertDoesNotThrow(() -> new SettlementResult(1, 0, true));
    }

    @Test
    void approvedSettlementAllowsWaivedLevy() {
        assertDoesNotThrow(() -> new SettlementResult(19, 0, true));
        assertDoesNotThrow(() -> new SettlementResult(20, 0, true));
    }

    @Test
    void approvedSettlementUsesFloorFivePercentAtRoundingBoundaries() {
        assertDoesNotThrow(() -> new SettlementResult(19, 0, true));
        assertDoesNotThrow(() -> new SettlementResult(20, 1, true));
    }

    @Test
    void rejectedSettlementMustBeZeroValued() {
        assertDoesNotThrow(() -> new SettlementResult(0, 0, false));
    }

    @Test
    void rejectedSettlementRejectsNonZeroValues() {
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(1, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(0, 1, false));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(12, 3, false));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(-1, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(0, -1, false));
    }

    @Test
    void approvedSettlementRejectsZeroOrNegativeLabor() {
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(0, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(-1, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(-100, 5, true));
    }

    @Test
    void approvedSettlementRejectsNegativeOrExcessiveLevy() {
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(100, -1, true));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(100, 6, true));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(19, 1, true));
    }

    @Test
    void integerMaxValueUsesBusinessRuleForMaximumLevy() {
        int labor = Integer.MAX_VALUE;
        int maxLevy = labor / 20;

        assertDoesNotThrow(() -> new SettlementResult(labor, maxLevy, true));
        assertThrows(IllegalArgumentException.class, () -> new SettlementResult(labor, maxLevy + 1, true));
    }
}
