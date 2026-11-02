package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CommunityFundServiceCharacterisationTest {

    private MemberRepository memberRepository;
    private CommunityFundServiceImpl fundService;
    private Member deficitMember;
    private Member solventMember;

    @BeforeEach
    void setUp() {
        memberRepository = new InMemoryMemberRepository();
        fundService = new CommunityFundServiceImpl(memberRepository);

        // 构造负资产成员 (120 初始积分 - 150 = -30 积分)
        deficitMember = new Member(new MemberId("MEM-DEF"), "Alice", "alice@ucd.ie");
        deficitMember.debit(150);
        memberRepository.save(deficitMember);

        // 构造正资产成员 (120 初始积分)
        solventMember = new Member(new MemberId("MEM-SOL"), "Bob", "bob@ucd.ie");
        memberRepository.save(solventMember);

        // 为基金池注资 200 积分
        fundService.depositLevy(200);
    }



    @Test
    @DisplayName("Pin: Normal grant succeeds, updates balances and records monthly grant")
    void pin_successfulGrantRelief() {
        LocalDate date = LocalDate.of(2026, 11, 2);

        fundService.grantRelief(deficitMember.getId(), 50, date);

        assertEquals(150, fundService.getFundBalance(), "Fund balance must be deducted by grant amount");
        assertEquals(20, deficitMember.getBalance(), "Recipient balance must increase by grant amount (-30 + 50 = 20)");
    }

    @Test
    @DisplayName("Pin: Null arguments throw NullPointerException")
    void pin_nullArgumentsThrowNpe() {
        LocalDate date = LocalDate.of(2026, 11, 2);

        assertThrows(NullPointerException.class, () ->
                fundService.grantRelief(null, 50, date));

        assertThrows(NullPointerException.class, () ->
                fundService.grantRelief(deficitMember.getId(), 50, null));
    }

    @Test
    @DisplayName("Pin: Non-positive amount throws IllegalArgumentException")
    void pin_nonPositiveAmountThrowsIllegalArgumentException() {
        LocalDate date = LocalDate.of(2026, 11, 2);

        assertThrows(IllegalArgumentException.class, () ->
                fundService.grantRelief(deficitMember.getId(), 0, date));

        assertThrows(IllegalArgumentException.class, () ->
                fundService.grantRelief(deficitMember.getId(), -10, date));
    }

    @Test
    @DisplayName("Pin: INV-11 fund solvency violation throws IllegalStateException")
    void pin_fundInsolventThrowsIllegalStateException() {
        LocalDate date = LocalDate.of(2026, 11, 2);

        // 基金池当前仅 200 积分，索取 201 触发 INV-11 异常
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fundService.grantRelief(deficitMember.getId(), 201, date));

        assertTrue(ex.getMessage().contains("INV-11 Violation"));
        assertEquals(200, fundService.getFundBalance(), "Fund balance must not mutate on failure");
    }

    @Test
    @DisplayName("Pin: Non-existent recipient throws IllegalArgumentException")
    void pin_unknownRecipientThrowsIllegalArgumentException() {
        LocalDate date = LocalDate.of(2026, 11, 2);
        MemberId missingId = new MemberId("MEM-GHOST");

        assertThrows(IllegalArgumentException.class, () ->
                fundService.grantRelief(missingId, 50, date));
    }

    @Test
    @DisplayName("Pin: R11 recipient balance >= 0 throws IllegalStateException")
    void pin_nonDeficitRecipientThrowsIllegalStateException() {
        LocalDate date = LocalDate.of(2026, 11, 2);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fundService.grantRelief(solventMember.getId(), 50, date));

        assertTrue(ex.getMessage().contains("R11 Violation"));
        assertEquals(200, fundService.getFundBalance(), "Fund balance must not mutate on failure");
    }

    @Test
    @DisplayName("Pin: INV-12 same calendar month repeat grant throws IllegalStateException")
    void pin_sameCalendarMonthRepeatGrantThrowsIllegalStateException() {
        LocalDate date1 = LocalDate.of(2026, 11, 2);
        LocalDate date2 = LocalDate.of(2026, 11, 28);

        // 第一次领取：-30 + 30 = 0 积分
        fundService.grantRelief(deficitMember.getId(), 30, date1);

        // 核心修正：由于第一次救济后余额变为 0，必须再次透支为负数，才能穿透 R11 守卫触发 INV-12
        deficitMember.debit(40);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fundService.grantRelief(deficitMember.getId(), 30, date2));

        assertTrue(ex.getMessage().contains("INV-12 Violation"));
    }

    @Test
    @DisplayName("Pin: INV-12 grants across different calendar months are both permitted")
    void pin_differentCalendarMonthsPermitted() {
        LocalDate oct31 = LocalDate.of(2026, 10, 31);
        LocalDate nov01 = LocalDate.of(2026, 11, 1);

        // 10月31日领取一次
        fundService.grantRelief(deficitMember.getId(), 30, oct31);

        // 重新将其透支至负数以满足 R11 前置条件
        deficitMember.debit(50);

        // 11月1日属于新的自然月，必须顺利通过
        assertDoesNotThrow(() -> fundService.grantRelief(deficitMember.getId(), 30, nov01));
        assertEquals(140, fundService.getFundBalance());
    }

    @Test
    @DisplayName("Adversarial Pin: Same calendar month across different years is permitted")
    void pin_adversarial_sameMonthDifferentYearsPermitted() {
        LocalDate lastYearNov = LocalDate.of(2025, 11, 15);
        LocalDate thisYearNov = LocalDate.of(2026, 11, 15);

        // 2025 年 11 月领取救济
        fundService.grantRelief(deficitMember.getId(), 20, lastYearNov);

        // 重新扣减使其余额保持为负数
        deficitMember.debit(30);

        // 2026 年 11 月（相隔一年）必须允许正常领取，绝不能仅比对 Month 枚举
        assertDoesNotThrow(() -> fundService.grantRelief(deficitMember.getId(), 20, thisYearNov));
        assertEquals(160, fundService.getFundBalance());
    }
}