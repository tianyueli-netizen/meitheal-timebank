package ie.ucd.comp41670.meitheal.adversarial;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.TransactionType;
import ie.ucd.comp41670.meitheal.member.CommunityFundServiceImpl;
import ie.ucd.comp41670.meitheal.member.InMemoryMemberRepository;
import ie.ucd.comp41670.meitheal.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommunityFundAdversarialTest {

    @Test
    void sameMonthRepeatGrantMustBeRejectedWithoutStateChanges() {
        MemberRepository repository = new InMemoryMemberRepository();
        Member member = new Member(new MemberId("MEM-CF-SM"), "FundSameMonth", "fund-sm@ucd.ie");
        member.debit(180);
        repository.save(member);
        CommunityFundServiceImpl fund = new CommunityFundServiceImpl(repository);
        fund.depositLevy(100);

        fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 10, 1));
        int balanceBeforeReject = member.getBalance();
        int fundBeforeReject = fund.getFundBalance();
        var ledgerBeforeReject = List.copyOf(fund.getFundLedgerEntries());
        long grantsBeforeReject = fund.getFundLedgerEntries().stream()
                .filter(e -> e.type() == TransactionType.FUND_GRANT)
                .count();

        try {
            fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 10, 15));
        } catch (IllegalArgumentException | IllegalStateException rejection) {
            assertEquals(balanceBeforeReject, member.getBalance());
            assertEquals(fundBeforeReject, fund.getFundBalance());
            assertEquals(ledgerBeforeReject, fund.getFundLedgerEntries());
            long grantsAfterReject = fund.getFundLedgerEntries().stream()
                    .filter(e -> e.type() == TransactionType.FUND_GRANT)
                    .count();
            assertEquals(grantsBeforeReject, grantsAfterReject);
            return;
        }

        assertEquals(balanceBeforeReject, member.getBalance());
        assertEquals(fundBeforeReject, fund.getFundBalance());
        assertEquals(ledgerBeforeReject, fund.getFundLedgerEntries());
        long grantsAfterReject = fund.getFundLedgerEntries().stream()
                .filter(e -> e.type() == TransactionType.FUND_GRANT)
                .count();
        assertEquals(grantsBeforeReject, grantsAfterReject);
    }

    @Test
    void consecutiveCalendarMonthGrantsMustBothSucceed() {
        MemberRepository repository = new InMemoryMemberRepository();
        Member member = new Member(new MemberId("MEM-CF-CM"), "FundConsecutiveMonth", "fund-cm@ucd.ie");
        member.debit(180);
        repository.save(member);
        CommunityFundServiceImpl fund = new CommunityFundServiceImpl(repository);
        fund.depositLevy(100);

        fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 10, 31));
        fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 11, 1));

        assertEquals(-40, member.getBalance());
        assertEquals(80, fund.getFundBalance());
        long grantEntries = fund.getFundLedgerEntries().stream()
                .filter(e -> e.type() == TransactionType.FUND_GRANT)
                .count();
        assertEquals(2, grantEntries);
    }

    @Test
    void returningToAlreadyUsedMonthMustBeRejectedWithoutStateChanges() {
        MemberRepository repository = new InMemoryMemberRepository();
        Member member = new Member(new MemberId("MEM-CF-RM"), "FundReturnMonth", "fund-rm@ucd.ie");
        member.debit(180);
        repository.save(member);
        CommunityFundServiceImpl fund = new CommunityFundServiceImpl(repository);
        fund.depositLevy(100);

        fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 10, 31));
        fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 11, 1));

        assertEquals(-40, member.getBalance());
        assertEquals(80, fund.getFundBalance());
        var ledgerBeforeReject = List.copyOf(fund.getFundLedgerEntries());
        long grantsBeforeReject = fund.getFundLedgerEntries().stream()
                .filter(e -> e.type() == TransactionType.FUND_GRANT)
                .count();
        assertEquals(2, grantsBeforeReject);

        try {
            fund.grantRelief(member.getId(), 10, LocalDate.of(2026, 10, 20));
        } catch (IllegalArgumentException | IllegalStateException rejection) {
            assertEquals(-40, member.getBalance());
            assertEquals(80, fund.getFundBalance());
            assertEquals(ledgerBeforeReject, fund.getFundLedgerEntries());
            long grantsAfterReject = fund.getFundLedgerEntries().stream()
                    .filter(e -> e.type() == TransactionType.FUND_GRANT)
                    .count();
            assertEquals(grantsBeforeReject, grantsAfterReject);
            return;
        }

        assertEquals(-40, member.getBalance());
        assertEquals(80, fund.getFundBalance());
        assertEquals(ledgerBeforeReject, fund.getFundLedgerEntries());
        long grantsAfterReject = fund.getFundLedgerEntries().stream()
                .filter(e -> e.type() == TransactionType.FUND_GRANT)
                .count();
        assertEquals(grantsBeforeReject, grantsAfterReject);
    }
}
