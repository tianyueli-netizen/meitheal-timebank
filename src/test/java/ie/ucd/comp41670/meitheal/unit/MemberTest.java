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

    @Test
    @DisplayName("R6: Suspended member cannot perform active operations but CAN receive credits")
    void suspendedMemberCannotActButCanReceiveCredits() {
        Member member = new Member(new MemberId("MEM-002"), "Bob", "bob@ucd.ie");
        member.suspend();

        // 冻结成员主动发起操作必须被拦截 (INV-6)
        assertThrows(IllegalStateException.class, member::assertActive);

        // R6 关键分支：接收报酬不能被拦截！
        assertDoesNotThrow(() -> member.credit(60));
        assertEquals(180, member.getBalance(), "Suspended member must successfully receive earned credits per R6");
    }
}