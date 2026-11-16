package ie.ucd.comp41670.meitheal.invariants;

import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import ie.ucd.comp41670.meitheal.domain.TimeCredit;
import ie.ucd.comp41670.meitheal.domain.TransactionType;
import ie.ucd.comp41670.meitheal.ledger.AppendOnlyLedger;
import ie.ucd.comp41670.meitheal.member.CommunityFundServiceImpl;
import ie.ucd.comp41670.meitheal.member.InMemoryMemberRepository;
import ie.ucd.comp41670.meitheal.member.Member;
import ie.ucd.comp41670.meitheal.policy.InvariantAuditor;
import ie.ucd.comp41670.meitheal.policy.TensionArbiter;
import ie.ucd.comp41670.meitheal.session.Session;
import ie.ucd.comp41670.meitheal.session.SessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class InvariantConservationAndFloorTest {

    private InMemoryMemberRepository memberRepository;
    private CommunityFundServiceImpl fundService;
    private SessionServiceImpl sessionService;
    private TensionArbiter arbiter;
    private AppendOnlyLedger ledger;
    private InvariantAuditor auditor;

    @BeforeEach
    void setUp() {
        memberRepository = new InMemoryMemberRepository();
        fundService = new CommunityFundServiceImpl(memberRepository);
        sessionService = new SessionServiceImpl();
        arbiter = new TensionArbiter();
        ledger = new AppendOnlyLedger();
        auditor = new InvariantAuditor();
    }

    private void executeEndToEndSettlement(Member provider, Member requester, int durationMinutes) {
        Session session = sessionService.createSession(provider.getId(), requester.getId(), durationMinutes);
        sessionService.confirm(session, provider.getId());
        sessionService.confirm(session, requester.getId());

        SettlementResult settlement = arbiter.calculateSettlement(
                requester.getBalance(),
                TimeCredit.fromMinutes(durationMinutes)
        );
        if (!settlement.isApproved()) {
            throw new IllegalStateException("Settlement rejected: would breach -180 overdraft floor.");
        }

        int laborCredits = session.transferCredits();
        int levyCredits = settlement.actualLevyCredits();

        requester.debit(laborCredits + levyCredits);
        provider.credit(laborCredits);
        if (levyCredits > 0) {
            fundService.depositLevy(levyCredits);
            ledger.append(new LedgerEntry(
                    "LEVY-" + session.getEngagementId().value(),
                    requester.getId(),
                    null,
                    levyCredits,
                    TransactionType.COMMUNITY_LEVY,
                    Instant.now()
            ));
        }
        ledger.append(new LedgerEntry(
                "TX-" + session.getEngagementId().value(),
                requester.getId(),
                provider.getId(),
                laborCredits,
                TransactionType.TRANSFER,
                Instant.now()
        ));
    }

    private void assertSystemInvariantsHold() {
        Map<MemberId, Integer> balances = memberRepository.findAll().stream()
                .collect(Collectors.toMap(Member::getId, Member::getBalance));
        int authorisedIssuedCredits = memberRepository.count() * 120;
        var violations = auditor.audit(balances, fundService.getFundBalance(), authorisedIssuedCredits);
        assertTrue(violations.isEmpty(), () -> "Invariant violations detected: " + violations);
    }

    @Test
    @DisplayName("INV-1 & INV-2: Multi-party transfers, levies, and community grants conserve total credits")
    void totalCreditsRemainConservedAcrossTransfersLeviesAndGrants() {
        Member alice = memberRepository.registerMember("Alice", "alice@ucd.ie");
        Member bob = memberRepository.registerMember("Bob", "bob@ucd.ie");
        Member charlie = memberRepository.registerMember("Charlie", "charlie@ucd.ie");
        assertSystemInvariantsHold(); // 3 * 120 = 360

        // Alice (120) -> Bob (120) for 120 mins: labor=120, levy=6. Alice=-6, Bob=240, Fund=6
        executeEndToEndSettlement(bob, alice, 120);
        assertEquals(-6, alice.getBalance());
        assertEquals(240, bob.getBalance());
        assertEquals(6, fundService.getFundBalance());
        assertSystemInvariantsHold();

        // Bob (240) -> Charlie (120) for 60 mins: labor=60, levy=3. Bob=177, Charlie=180, Fund=9
        executeEndToEndSettlement(charlie, bob, 60);
        assertEquals(177, bob.getBalance());
        assertEquals(180, charlie.getBalance());
        assertEquals(9, fundService.getFundBalance());
        assertSystemInvariantsHold();

        // Community Fund grants 5 credits relief to deficit member Alice (-6 -> -1), Fund: 9 -> 4
        fundService.grantRelief(alice.getId(), 5, LocalDate.of(2026, 11, 10));
        assertEquals(-1, alice.getBalance());
        assertEquals(4, fundService.getFundBalance());
        assertSystemInvariantsHold();
    }

    @Test
    @DisplayName("INV-1 & INV-10: Overdraft floor -180 waives levy at exact boundary and rejects breach")
    void overdraftFloorProtectionWaivesLevyAtBoundaryAndRejectsBreach() {
        Member provider = memberRepository.registerMember("Provider", "prov@ucd.ie");
        Member requester = memberRepository.registerMember("Requester", "req@ucd.ie");

        // Move requester from 120 to -120 via 240-min session (labor=240, levy=0 because 120-240=-120; wait: 120-240=-120 leaves 60 buffer, so levy=12 -> -132)
        // Adjust requester directly to exact -120 for boundary test
        requester.debit(240);
        provider.credit(240);
        assertEquals(-120, requester.getBalance());
        assertSystemInvariantsHold();

        // Edge case: Requester at -120 confirms 60-min session -> labor=60 brings balance to -180, 3-credit levy waived to 0
        executeEndToEndSettlement(provider, requester, 60);
        assertEquals(-180, requester.getBalance());
        assertEquals(420, provider.getBalance());
        assertEquals(0, fundService.getFundBalance());
        assertSystemInvariantsHold();

        // Next session of even 1 minute must be rejected, leaving all balances unchanged at -180
        assertThrows(IllegalStateException.class, () -> executeEndToEndSettlement(provider, requester, 1));
        assertEquals(-180, requester.getBalance());
        assertEquals(420, provider.getBalance());
        assertSystemInvariantsHold();
    }
}