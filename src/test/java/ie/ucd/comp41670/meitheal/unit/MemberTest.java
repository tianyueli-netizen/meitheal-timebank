package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.MemberStatus;
import ie.ucd.comp41670.meitheal.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    @DisplayName("R3: New member must immediately receive a 120 credit welcome grant")
    void newMemberMustReceive120WelcomeGrant() {
        Member member = new Member(new MemberId("MEM-001"), "Alice", "alice@ucd.ie");
        assertEquals(120, member.getBalance(), "Invariant violation: Initial balance must be 120 credits per R3");
    }

    @Test
    @DisplayName("Validation: MemberId cannot be null or blank")
    void memberIdMustNotBeNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new MemberId(null));
        assertThrows(IllegalArgumentException.class, () -> new MemberId("   "));
    }
}