
package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.member.CommunityFundServiceImpl;
import ie.ucd.comp41670.meitheal.member.InMemoryMemberRepository;
import ie.ucd.comp41670.meitheal.member.Member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CommunityFundCharacterisationTest {

    private MemberRepository repository;
    private CommunityFundServiceImpl fund;
    private Member recipient;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMemberRepository();
        fund = new CommunityFundServiceImpl(repository);

        recipient = new Member(
                new MemberId("CHAR-001"),
                "Test Member",
                "test@example.com"
        );

        recipient.debit(180);
        repository.save(recipient);
        fund.depositLevy(100);
    }

    @Test
    void nullRecipientIsCheckedBeforeInvalidAmount() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> fund.grantRelief(
                        null, 0, LocalDate.of(2026, 10, 29))
        );

        assertEquals("Recipient ID required", ex.getMessage());
    }

    @Test
    void nullDateIsCheckedBeforeInvalidAmount() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> fund.grantRelief(
                        recipient.getId(), 0, null)
        );

        assertEquals("Grant date required", ex.getMessage());
    }

    @Test
    void insufficientFundIsCheckedBeforeMemberLookup() {
        MemberId unknownId = new MemberId("UNKNOWN");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> fund.grantRelief(
                        unknownId, 150, LocalDate.of(2026, 10, 29))
        );

        assertEquals(
                "INV-11 Violation: Insufficient fund balance (100) for grant (150)",
                ex.getMessage()
        );
    }

    @Test
    void grantEqualToFundBalanceSucceeds() {
        int originalBalance = recipient.getBalance();

        fund.grantRelief(
                recipient.getId(),
                100,
                LocalDate.of(2026, 10, 29)
        );

        assertEquals(0, fund.getFundBalance());
        assertEquals(originalBalance + 100, recipient.getBalance());
        assertEquals(2, fund.getFundLedgerEntries().size());
    }

    @Test
    void repeatedGrantInSameMonthPreservesState() {
        LocalDate date = LocalDate.of(2026, 10, 29);

        fund.grantRelief(recipient.getId(), 20, date);

        int fundBefore = fund.getFundBalance();
        int memberBefore = recipient.getBalance();
        int entriesBefore = fund.getFundLedgerEntries().size();

        assertThrows(
                IllegalStateException.class,
                () -> fund.grantRelief(
                        recipient.getId(), 10, date.plusDays(1))
        );

        assertEquals(fundBefore, fund.getFundBalance());
        assertEquals(memberBefore, recipient.getBalance());
        assertEquals(entriesBefore, fund.getFundLedgerEntries().size());
    }

    @Test
    void exportedLedgerIsAnImmutableSnapshot() {
        var snapshot = fund.getFundLedgerEntries();

        fund.depositLevy(0);

        assertEquals(1, snapshot.size());
        assertEquals(2, fund.getFundLedgerEntries().size());

        assertThrows(
                UnsupportedOperationException.class,
                snapshot::clear
        );
    }
}
