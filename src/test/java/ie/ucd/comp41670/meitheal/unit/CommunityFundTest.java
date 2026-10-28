package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.member.CommunityFundServiceImpl;
import ie.ucd.comp41670.meitheal.member.InMemoryMemberRepository;
import ie.ucd.comp41670.meitheal.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CommunityFundTest {

    private MemberRepository memberRepository;
    private CommunityFundServiceImpl fundService;
    private Member deficitMember;

    @BeforeEach
    void setUp() {
        memberRepository = new InMemoryMemberRepository();
        fundService = new CommunityFundServiceImpl(memberRepository);
        deficitMember = new Member(new MemberId("MEM-DEFICIT"), "Dave", "dave@ucd.ie");
        // 将其扣至负资产状态 (120 - 180 = -60)
        deficitMember.debit(180);
        memberRepository.save(deficitMember);
        // 充值基金储备池 100 积分
        fundService.depositLevy(100);
    }

    @Test
    @DisplayName("INV-12: Must permit grant in consecutive calendar months (e.g. Oct 31 then Nov 1) and reject in same month")
    void shouldEnforceCalendarMonthBoundaryCorrectly() {
        LocalDate oct31 = LocalDate.of(2026, 10, 31);
        LocalDate nov1 = LocalDate.of(2026, 11, 1);
        LocalDate nov15 = LocalDate.of(2026, 11, 15);

        // 第一次：10月31日领取 30 分
        fundService.grantRelief(deficitMember.getId(), 30, oct31);

        // 第二次：11月1日（仅隔1天，但是新自然月），依法必须允许！AI 的 30 天滑动窗口会在这里抛异常导致红灯
        assertDoesNotThrow(() -> fundService.grantRelief(deficitMember.getId(), 30, nov1),
                "INV-12 Violation: Consecutive calendar months must be permitted even if only 1 day apart!");

        // 第三次：同在11月的15日再次领取，必须被拦截抛出异常！
        assertThrows(IllegalStateException.class,
                () -> fundService.grantRelief(deficitMember.getId(), 20, nov15),
                "INV-12 Violation: Second grant in the same calendar month must be strictly rejected!");
    }

    @Test
    @DisplayName("INV-11: Fund cannot grant credits it does not hold (fundBalance >= 0)")
    void fundCannotOverdraftBeyondBalance() {
        // 当前基金只有 100 积分，尝试申请 150 积分救济，必须被拒绝且基金余额不可变为负数
        assertThrows(IllegalStateException.class,
                () -> fundService.grantRelief(deficitMember.getId(), 150, LocalDate.of(2026, 10, 29)));
        assertEquals(100, fundService.getFundBalance(), "INV-11: Fund balance must remain intact and non-negative");
    }

    @Test
    @DisplayName("R11: Member with balance >= 0 is strictly ineligible for relief")
    void memberWithZeroOrPositiveBalanceCannotReceiveGrant() {
        Member positiveMember = new Member(new MemberId("MEM-POS"), "Eve", "eve@ucd.ie"); // 余额 120
        memberRepository.save(positiveMember);

        assertThrows(IllegalStateException.class,
                () -> fundService.grantRelief(positiveMember.getId(), 20, LocalDate.of(2026, 10, 29)),
                "R11 Violation: Relief can only be granted to members with balance strictly below zero");
    }
}