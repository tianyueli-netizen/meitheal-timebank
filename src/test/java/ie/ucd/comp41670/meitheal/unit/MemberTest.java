package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.MemberStatus;
import ie.ucd.comp41670.meitheal.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import ie.ucd.comp41670.meitheal.member.InMemoryMemberRepository;

import java.util.List;

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

    @Test
    @DisplayName("Characterisation: the current implementation only blocks active operations for suspended members and still allows credits/debits")
    void suspendedMembersStillAcceptCreditAndDebitDespiteAssertActive() {
        Member member = new Member(new MemberId("MEM-100"), "Dana", "dana@ucd.ie");
        member.suspend();

        assertThrows(IllegalStateException.class, member::assertActive,
                "Current behaviour: suspended status is the only state that trips assertActive");

        assertDoesNotThrow(() -> member.credit(25),
                "Current behaviour: credit() does not validate member status before increasing balance");
        assertEquals(145, member.getBalance(),
                "Current behaviour: a suspended member can still receive credits");

        assertDoesNotThrow(() -> member.debit(30),
                "Current behaviour: debit() also bypasses status checks");
        assertEquals(115, member.getBalance(),
                "Current behaviour: a suspended member can still be debited without an active-state guard");
    }

    @Test
    @DisplayName("Adversarial inversion candidate: if a helper were called from credit/debit, suspended members would begin throwing before the balance change")
    void adversarialInversionCandidateWouldDifferForSuspendedMembers() {
        Member member = new Member(new MemberId("MEM-101"), "Eli", "eli@ucd.ie");
        member.suspend();

        assertDoesNotThrow(() -> member.credit(10),
                "Pinned behaviour: credit does not invoke the active-state guard");
        assertDoesNotThrow(() -> member.debit(5),
                "Pinned behaviour: debit does not invoke the active-state guard");
        assertEquals(125, member.getBalance(),
                "Pinned behaviour: balance mutations remain accepted for a suspended member");
    }

    @Test
    @DisplayName("R12: findAll() must return unmodifiable view preventing internal collection tampering")
    void repositoryFindAllMustBeUnmodifiablePerR12() {
        InMemoryMemberRepository repo = new InMemoryMemberRepository();
        Member m1 = new Member(new MemberId("MEM-010"), "Charlie", "charlie@ucd.ie");
        repo.save(m1);

        List<Member> members = repo.findAll();
        assertEquals(1, members.size());

        // 故意尝试修改返回的列表，必须抛出 UnsupportedOperationException (R12 防逃逸)
        assertThrows(UnsupportedOperationException.class, () -> members.clear(),
                "R12 Violation: Caller was able to modify returned repository collection!");
    }
}