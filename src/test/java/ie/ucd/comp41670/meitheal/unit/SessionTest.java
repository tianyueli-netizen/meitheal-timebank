package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import ie.ucd.comp41670.meitheal.domain.TimeCredit;
import ie.ucd.comp41670.meitheal.policy.TensionArbiter;
import ie.ucd.comp41670.meitheal.session.Session;
import ie.ucd.comp41670.meitheal.session.SessionServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    private final SessionServiceImpl sessionService = new SessionServiceImpl();
    private final TensionArbiter arbiter = new TensionArbiter();

    @Test
    void sessionTransfersOnlyAfterBothPartiesConfirm() {
        MemberId provider = new MemberId("P-001");
        MemberId requester = new MemberId("R-001");
        Session session = sessionService.createSession(provider, requester, 60);

        assertFalse(session.isProviderConfirmed());
        assertFalse(session.isRequesterConfirmed());
        assertFalse(session.canTransferCredits());
        assertThrows(IllegalStateException.class, session::transferCredits);

        sessionService.confirm(session, provider);
        assertTrue(session.isProviderConfirmed());
        assertFalse(session.isRequesterConfirmed());
        assertFalse(session.canTransferCredits());

        sessionService.confirm(session, requester);
        assertTrue(session.isConfirmed());
        assertTrue(session.canTransferCredits());
        assertEquals(60, session.transferCredits());
        assertTrue(session.isCreditsTransferred());

        assertThrows(IllegalStateException.class, session::transferCredits);
    }

    @Test
    void levyIsCappedSoRequesterNeverFallsBelowTheOverdraftFloor() {
        SettlementResult legal = arbiter.calculateSettlement(-117, new TimeCredit(60));
        assertTrue(legal.isApproved());
        assertEquals(60, legal.laborCredits());
        assertEquals(3, legal.actualLevyCredits());

        SettlementResult atFloor = arbiter.calculateSettlement(-120, new TimeCredit(60));
        assertTrue(atFloor.isApproved());
        assertEquals(60, atFloor.laborCredits());
        assertEquals(0, atFloor.actualLevyCredits());

        SettlementResult belowFloor = arbiter.calculateSettlement(-121, new TimeCredit(60));
        assertFalse(belowFloor.isApproved());
        assertEquals(0, belowFloor.laborCredits());
        assertEquals(0, belowFloor.actualLevyCredits());

        SettlementResult rounded = arbiter.calculateSettlement(500, new TimeCredit(39));
        assertTrue(rounded.isApproved());
        assertEquals(39, rounded.laborCredits());
        assertEquals(1, rounded.actualLevyCredits());
    }
}
