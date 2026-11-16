package ie.ucd.comp41670.meitheal.adversarial;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.member.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberOverdraftAdversarialTest {

    @Test
    void debit300FromFreshMemberMustBeAllowedAndReachMinus180Exactly() {
        Member member = new Member(new MemberId("MEM-OD-300"), "Overdraft300", "od300@ucd.ie");

        member.debit(300);

        assertEquals(-180, member.getBalance());
    }

    @Test
    void debit301FromFreshMemberMustBeRejectedOrLeaveBalanceUnchangedAt120() {
        Member member = new Member(new MemberId("MEM-OD-301"), "Overdraft301", "od301@ucd.ie");

        try {
            member.debit(301);
        } catch (IllegalArgumentException | IllegalStateException rejection) {
            assertEquals(120, member.getBalance());
            return;
        }

        assertEquals(120, member.getBalance());
    }
}
