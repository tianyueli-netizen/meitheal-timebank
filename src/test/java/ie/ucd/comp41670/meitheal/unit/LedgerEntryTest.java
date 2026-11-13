
package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;

import ie.ucd.comp41670.meitheal.domain.TransactionType;
import org.junit.jupiter.api.Test;


import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LedgerEntryTest {

    private final MemberId member1 = new MemberId("member-001");
    private final MemberId member2 = new MemberId("member-002");

    @Test
    void shouldCreateLedgerEntryWithValidData() {

        LedgerEntry entry = new LedgerEntry(
                "entry-001",
                member1,
                member2,
                120,
                TransactionType.TRANSFER,
                Instant.now()
        );

        assertNotNull(entry);
        assertEquals("entry-001", entry.entryId());
        assertEquals(member1, entry.fromMemberId());
        assertEquals(member2, entry.toMemberId());
        assertEquals(120, entry.amount());
        assertEquals(TransactionType.TRANSFER, entry.type());
    }

    @Test
    void shouldRejectNullEntryId() {

        assertThrows(IllegalArgumentException.class, () ->
                new LedgerEntry(
                        null,
                        member1,
                        member2,
                        120,
                        TransactionType.TRANSFER,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectBlankEntryId() {

        assertThrows(IllegalArgumentException.class, () ->
                new LedgerEntry(
                        " ",
                        member1,
                        member2,
                        120,
                        TransactionType.TRANSFER,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectNullTransactionType() {

        assertThrows(NullPointerException.class, () ->
                new LedgerEntry(
                        "entry-002",
                        member1,
                        member2,
                        120,
                        null,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectNullTimestamp() {

        assertThrows(NullPointerException.class, () ->
                new LedgerEntry(
                        "entry-003",
                        member1,
                        member2,
                        120,
                        TransactionType.TRANSFER,
                        null
                )
        );
    }

    @Test
    void shouldRejectNegativeAmount() {

        assertThrows(IllegalArgumentException.class, () ->
                new LedgerEntry(
                        "entry-004",
                        member1,
                        member2,
                        -1,
                        TransactionType.TRANSFER,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldAllowZeroAmount() {

        LedgerEntry entry = new LedgerEntry(
                "entry-005",
                member1,
                member2,
                0,
                TransactionType.COMMUNITY_LEVY,
                Instant.now()
        );

        assertEquals(0, entry.amount());
    }

    @Test
    void shouldAllowPositiveAmount() {

        LedgerEntry entry = new LedgerEntry(
                "entry-006",
                null,
                member1,
                120,
                TransactionType.WELCOME_GRANT,
                Instant.now()
        );

        assertEquals(120, entry.amount());
        assertEquals(TransactionType.WELCOME_GRANT, entry.type());
    }
}
