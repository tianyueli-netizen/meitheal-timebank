package ie.ucd.comp41670.meitheal.invariants;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.session.Session;
import ie.ucd.comp41670.meitheal.session.SessionServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvariantDoubleConfirmTest {

    private final SessionServiceImpl sessionService = new SessionServiceImpl();

    @Test
    void r4RequiresBothPartiesToConfirmBeforeTransfer() {
        MemberId provider = new MemberId("P-100");
        MemberId requester = new MemberId("R-100");
        Session session = sessionService.createSession(provider, requester, 60);

        assertFalse(session.isProviderConfirmed());
        assertFalse(session.isRequesterConfirmed());
        assertFalse(session.isConfirmed());
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);

        sessionService.confirm(session, provider);

        assertTrue(session.isProviderConfirmed());
        assertFalse(session.isRequesterConfirmed());
        assertFalse(session.isConfirmed());
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);

        sessionService.confirm(session, requester);

        assertTrue(session.isProviderConfirmed());
        assertTrue(session.isRequesterConfirmed());
        assertTrue(session.isConfirmed());
        assertTrue(session.canTransferCredits());
        assertEquals(60, session.transferCredits());
        assertTrue(session.isCreditsTransferred());

        assertThrows(IllegalStateException.class, session::transferCredits);
    }
}
