package ie.ucd.comp41670.meitheal.adversarial;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.session.CancellationManager;
import ie.ucd.comp41670.meitheal.session.Session;
import ie.ucd.comp41670.meitheal.session.SessionDuration;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancellationAdversarialTest {
    private static void assertSessionStateAndCountsUnchanged(
            Session session,
            CancellationManager manager,
            MemberId provider,
            MemberId requester,
            MemberId outsider,
            boolean providerConfirmed,
            boolean requesterConfirmed,
            boolean cancelled,
            boolean creditsTransferred,
            int providerCount,
            int requesterCount,
            int outsiderCount
    ) {
        assertEquals(providerConfirmed, session.isProviderConfirmed());
        assertEquals(requesterConfirmed, session.isRequesterConfirmed());
        assertEquals(cancelled, session.isCancelled());
        assertEquals(creditsTransferred, session.isCreditsTransferred());
        assertEquals(providerCount, manager.getCancellationsFor(provider));
        assertEquals(requesterCount, manager.getCancellationsFor(requester));
        assertEquals(outsiderCount, manager.getCancellationsFor(outsider));
    }

    @Test
    void legalLateCancellationByParticipantCancelsOnceAndBlocksTransfer() {
        MemberId provider = new MemberId("PROV-LATE-1");
        MemberId requester = new MemberId("REQ-LATE-1");
        CancellationManager manager = new CancellationManager();
        Session session = new Session(provider, requester, SessionDuration.ofMinutes(60));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 12, 0);
        session.setAgreedStart(start);
        session.confirm(provider);
        session.confirm(requester);

        manager.cancelSession(session, provider, start.minusHours(1));

        assertTrue(session.isCancelled());
        assertEquals(1, manager.getCancellationsFor(provider));
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);
    }

    @Test
    void legalCancellationOutsideLateWindowCancelsWithoutLateCountAndBlocksTransfer() {
        MemberId provider = new MemberId("PROV-EARLY-1");
        MemberId requester = new MemberId("REQ-EARLY-1");
        CancellationManager manager = new CancellationManager();
        Session session = new Session(provider, requester, SessionDuration.ofMinutes(60));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 12, 0);
        session.setAgreedStart(start);
        session.confirm(provider);
        session.confirm(requester);

        manager.cancelSession(session, provider, start.minusHours(25));

        assertTrue(session.isCancelled());
        assertEquals(0, manager.getCancellationsFor(provider));
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);
    }

    @Test
    void unconfirmedSessionCancellationMustNotLeadToTransferEligibilityAfterward() {
        MemberId provider = new MemberId("PROV-UNC-1");
        MemberId requester = new MemberId("REQ-UNC-1");
        MemberId outsider = new MemberId("OUT-UNC-1");
        CancellationManager manager = new CancellationManager();
        Session session = new Session(provider, requester, SessionDuration.ofMinutes(60));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 12, 0);
        session.setAgreedStart(start);

        try {
            manager.cancelSession(session, provider, start.minusHours(1));
        } catch (IllegalArgumentException | IllegalStateException rejection) {
            assertSessionStateAndCountsUnchanged(
                    session, manager, provider, requester, outsider,
                    false, false, false, false, 0, 0, 0
            );
            assertThrows(IllegalStateException.class, session::transferCredits);
            return;
        }

        assertTrue(session.isCancelled());
        try {
            session.confirm(provider);
            session.confirm(requester);
        } catch (IllegalStateException | IllegalArgumentException ignored) {
        }
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);
    }

    @Test
    void outsiderCancellationAttemptMustNotChangeStateOrCounts() {
        MemberId provider = new MemberId("PROV-OUT-1");
        MemberId requester = new MemberId("REQ-OUT-1");
        MemberId outsider = new MemberId("OUT-1");
        CancellationManager manager = new CancellationManager();
        Session session = new Session(provider, requester, SessionDuration.ofMinutes(60));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 12, 0);
        session.setAgreedStart(start);
        session.confirm(provider);
        session.confirm(requester);

        try {
            manager.cancelSession(session, outsider, start.minusHours(1));
        } catch (IllegalArgumentException | IllegalStateException rejection) {
            assertSessionStateAndCountsUnchanged(
                    session, manager, provider, requester, outsider,
                    true, true, false, false, 0, 0, 0
            );
            return;
        }

        assertSessionStateAndCountsUnchanged(
                session, manager, provider, requester, outsider,
                true, true, false, false, 0, 0, 0
        );
    }

    @Test
    void repeatedLateCancellationMustKeepSingleCountAndRemainCancelledAndBlocked() {
        MemberId provider = new MemberId("PROV-REP-1");
        MemberId requester = new MemberId("REQ-REP-1");
        CancellationManager manager = new CancellationManager();
        Session session = new Session(provider, requester, SessionDuration.ofMinutes(60));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 12, 0);
        session.setAgreedStart(start);
        session.confirm(provider);
        session.confirm(requester);

        manager.cancelSession(session, provider, start.minusHours(1));
        assertTrue(session.isCancelled());
        assertEquals(1, manager.getCancellationsFor(provider));
        try {
            manager.cancelSession(session, provider, start.minusMinutes(30));
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }

        assertEquals(1, manager.getCancellationsFor(provider));
        assertTrue(session.isCancelled());
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);
    }
}
