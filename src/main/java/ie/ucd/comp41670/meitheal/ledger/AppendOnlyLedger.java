
package ie.ucd.comp41670.meitheal.ledger;

import ie.ucd.comp41670.meitheal.contract.Ledger;
import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * In-memory append-only ledger implementation.
 *
 * R7: Existing ledger entries cannot be modified or deleted.
 * R12: External callers receive read-only copies.
 */
public final class AppendOnlyLedger implements Ledger {

    private final List<LedgerEntry> entries = new ArrayList<>();

    /**
     * Adds a new entry to the ledger.
     */
    @Override
    public synchronized void append(LedgerEntry entry) {

        Objects.requireNonNull(
                entry,
                "Ledger entry cannot be null"
        );

        entries.add(entry);
    }
    @Override
    public synchronized int getEntryCount() {
        return entries.size();
    }

    /**
     * Returns an immutable snapshot of all entries.
     */
    @Override
    public synchronized List<LedgerEntry> getAllEntries() {

        return List.copyOf(entries);
    }

    /**
     * Returns entries where the member is either
     * the sender or the receiver.
     */
    @Override
    public synchronized List<LedgerEntry> getEntriesByMember(
            MemberId memberId) {

        Objects.requireNonNull(
                memberId,
                "Member ID cannot be null"
        );

        return entries.stream()
                .filter(entry ->
                        memberId.equals(entry.fromMemberId())
                                || memberId.equals(entry.toMemberId()))
                .toList();
    }
}
