package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.policy.InvariantAuditor;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvariantAuditorTest {
    private static final int AUTH = 240;
    private static final MemberId MEMBER_A = new MemberId("MEM-001");
    private static final MemberId MEMBER_B = new MemberId("MEM-002");

    private final InvariantAuditor auditor = new InvariantAuditor();

    @Test
    void noViolationsWhenBalancesAndFundMatchTheBaseline() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, 120, MEMBER_B, 120), 0, AUTH);
        assertTrue(result.isEmpty());
    }

    @Test
    void noViolationsWhenBalancesAndFundMatchTheBaselineWithNonZeroFund() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, 57, MEMBER_B, 180), 3, AUTH);
        assertTrue(result.isEmpty());
    }

    @Test
    void reportsR2ViolationWhenAnyMemberIsBelowFloor() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, -181, MEMBER_B, 421), 0, AUTH);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().startsWith("R2:"));
    }

    @Test
    void reportsR11ViolationWhenFundIsNegative() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, 120, MEMBER_B, 121), -1, AUTH);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().startsWith("R11:"));
    }

    @Test
    void reportsR3ViolationWhenTotalDoesNotMatchIssuedCredits() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, 120, MEMBER_B, 120), 1, AUTH);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().startsWith("R3:"));
    }

    @Test
    void allowsMemberBalanceExactlyAtTheFloorWhenTotalIsConserved() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, -180, MEMBER_B, 420), 0, 240);
        assertTrue(result.isEmpty());
    }

    @Test
    void detectsConservationMismatchWithoutIntegerOverflow() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, Integer.MAX_VALUE, MEMBER_B, Integer.MAX_VALUE), 2, 0);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().startsWith("R3:"));
        assertTrue(result.getFirst().contains("4294967296"));
    }

    @Test
    void reportsAllIndependentViolationsInOneAudit() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, -181, MEMBER_B, 120), -1, 240);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(message -> message.startsWith("R2:")));
        assertTrue(result.stream().anyMatch(message -> message.startsWith("R11:")));
        assertTrue(result.stream().anyMatch(message -> message.startsWith("R3:")));
    }

    @Test
    void returnedViolationListCannotBeModified() {
        List<String> result = auditor.audit(Map.of(MEMBER_A, -181, MEMBER_B, 120), 0, 240);
        assertThrows(UnsupportedOperationException.class, result::clear);
    }

    @Test
    void rejectsNullCapturedBalances() {
        assertThrows(NullPointerException.class, () -> auditor.audit(null, 0, 240));
    }

    @Test
    void rejectsNullMemberId() {
        Map<MemberId, Integer> malformed = new HashMap<>();
        malformed.put(null, 120);
        assertThrows(NullPointerException.class, () -> auditor.audit(malformed, 0, 240));
    }

    @Test
    void rejectsNullMemberBalance() {
        Map<MemberId, Integer> malformed = new HashMap<>();
        malformed.put(MEMBER_A, null);
        assertThrows(NullPointerException.class, () -> auditor.audit(malformed, 0, 240));
    }

    @Test
    void rejectsNegativeAuthorisedIssuedCredits() {
        assertThrows(IllegalArgumentException.class, () -> auditor.audit(Map.of(MEMBER_A, 120, MEMBER_B, 120), 0, -1));
    }
}
