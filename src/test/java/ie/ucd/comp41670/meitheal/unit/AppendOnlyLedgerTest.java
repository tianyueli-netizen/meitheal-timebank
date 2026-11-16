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

        assertEquals(1, ledger.getEntryCount());
    }

    @Test
    void appendShouldPreserveExistingEntriesInInsertionOrder() {
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
        assertEquals(2, ledger.getEntryCount());
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
        assertEquals(1, ledger.getEntryCount());
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

        assertEquals(0, ledger.getEntryCount());
    }

    @Test
    void getEntriesByMemberShouldRejectNullId() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        assertThrows(
                NullPointerException.class,
                () -> ledger.getEntriesByMember(null)
        );
    }

    @Test /*No record of transaction when create entry */
    void emptyLedgerShouldHaveNoEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        assertEquals(0, ledger.getEntryCount());
        assertTrue(ledger.getAllEntries().isEmpty());
        assertTrue(ledger.getEntriesByMember(member1).isEmpty());
    }

    @Test
    void getEntriesByMemberShouldBeUnmodifiable() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        LedgerEntry entry = createTransfer(
                "entry-001", member1, member2, 10
        );
        ledger.append(entry);

        List<LedgerEntry> entries =
                ledger.getEntriesByMember(member1);

        assertThrows(
                UnsupportedOperationException.class,
                entries::clear
        );

        assertEquals(1, ledger.getEntryCount());
        assertEquals(List.of(entry), ledger.getAllEntries());
    }

    @Test
    void ledgerSnapshotShouldNotChangeAfterAppend() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        LedgerEntry first = createTransfer(
                "entry-001", member1, member2, 10
        );
        LedgerEntry second = createTransfer(
                "entry-002", member2, member3, 5
        );

        ledger.append(first);

        List<LedgerEntry> snapshot = ledger.getAllEntries();

        ledger.append(second);

        assertEquals(List.of(first), snapshot);
        assertEquals(List.of(first, second),
                ledger.getAllEntries());
        assertEquals(2, ledger.getEntryCount());
    }

    @Test
    void getEntriesByMemberShouldHandleCommunityFundEntries() {
        AppendOnlyLedger ledger = new AppendOnlyLedger();

        LedgerEntry levy = new LedgerEntry(
                "levy-001",
                member1,
                null,
                5,
                TransactionType.COMMUNITY_LEVY,
                Instant.parse("2026-11-08T10:00:00Z")
        );

        LedgerEntry grant = new LedgerEntry(
                "grant-001",
                null,
                member2,
                10,
                TransactionType.FUND_GRANT,
                Instant.parse("2026-11-08T11:00:00Z")
        );

        ledger.append(levy);
        ledger.append(grant);

        assertEquals(List.of(levy),
                ledger.getEntriesByMember(member1));

        assertEquals(List.of(grant),
                ledger.getEntriesByMember(member2));

        assertTrue(
                ledger.getEntriesByMember(member3).isEmpty()
        );

        assertEquals(2, ledger.getEntryCount());
    }

}
