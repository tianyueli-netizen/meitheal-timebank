
package ie.ucd.comp41670.meitheal.contract;

import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.util.List;

/**
 * Defines the operations available for the system ledger.
 *
 * R7: Ledger entries are append-only.
 * R12: Ledger exports must be read-only.
 */
public interface Ledger {

    /**
     * Appends a new transaction entry.
     */
    void append(LedgerEntry entry);

    /**
     * Returns all ledger entries as a read-only list.
     */
    List<LedgerEntry> getAllEntries();

    /**
     * Returns all entries involving a particular member.
     */
    List<LedgerEntry> getEntriesByMember(MemberId memberId);

    /**
     * Returns the number of ledger entries currently stored.
     */
    int getEntryCount();
}
