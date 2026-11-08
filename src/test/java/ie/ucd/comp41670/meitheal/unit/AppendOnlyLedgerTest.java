package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.TransactionType;
import ie.ucd.comp41670.meitheal.ledger.AppendOnlyLedger;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppendOnlyLedgerTest {

    private final MemberId member1 = new MemberId("member-001");
    private final MemberId member2 = new MemberId("member-002");
    private final MemberId member3 = new MemberId("member-003");

    private LedgerEntry createTransfer(
            String entryId,
            MemberId from,
            MemberId to,
            int amount
    ) {
        return new LedgerEntry(
                entryId,
                from,
                to,
                amount,
                TransactionType.TRANSFER,
                Instant.parse("2026-11-08T10:00:00Z")
        );
    }

    @Test
    void appendShouldIncreaseEntryCount() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        ledger.append(createTransfer("entry-001", member1, member2, 10));

        assertEquals(1, ledger.size());
    }

    @Test
    void appendShouldPreserveExistingEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        LedgerEntry first = createTransfer(
                "entry-001", member1, member2, 10
        );
        LedgerEntry second = createTransfer(
                "entry-002", member2, member3, 5
        );

        ledger.append(first);
        ledger.append(second);

        assertEquals(List.of(first, second), ledger.getAllEntries());
        assertEquals(2, ledger.size());
    }

    @Test
    void getAllEntriesShouldReturnAllEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        LedgerEntry first = createTransfer(
                "entry-001", member1, member2, 10
        );
        LedgerEntry second = createTransfer(
                "entry-002", member2, member3, 5
        );

        ledger.append(first);
        ledger.append(second);

        assertEquals(List.of(first, second), ledger.getAllEntries());
    }

    @Test
    void getAllEntriesShouldBeUnmodifiable() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();
        LedgerEntry entry = createTransfer(
                "entry-001", member1, member2, 10
        );
        ledger.append(entry);

        List<LedgerEntry> exportedEntries = ledger.getAllEntries();

        assertThrows(
                UnsupportedOperationException.class,
                exportedEntries::clear
        );

        // Attempting to modify the exported list must not affect the ledger.
        assertEquals(1, ledger.size());
        assertEquals(List.of(entry), ledger.getAllEntries());
    }

    @Test
    void getEntriesByMemberShouldFindSenderEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();
        LedgerEntry entry = createTransfer(
                "entry-001", member1, member2, 10
        );
        ledger.append(entry);

        assertEquals(
                List.of(entry),
                ledger.getEntriesByMember(member1)
        );
    }

    @Test
    void getEntriesByMemberShouldFindReceiverEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();
        LedgerEntry entry = createTransfer(
                "entry-001", member1, member2, 10
        );
        ledger.append(entry);

        assertEquals(
                List.of(entry),
                ledger.getEntriesByMember(member2)
        );
    }

    @Test
    void getEntriesByMemberShouldNotReturnUnrelatedEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();
        LedgerEntry entry = createTransfer(
                "entry-001", member1, member2, 10
        );
        ledger.append(entry);

        assertTrue(ledger.getEntriesByMember(member3).isEmpty());
    }

    @Test
    void appendShouldRejectNullEntry() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        assertThrows(
                NullPointerException.class,
                () -> ledger.append(null)
        );

        assertEquals(0, ledger.size());
    }

    @Test
    void getEntriesByMemberShouldRejectNullId() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        assertThrows(
                NullPointerException.class,
                () -> ledger.getEntriesByMember(null)
        );
    }
}
