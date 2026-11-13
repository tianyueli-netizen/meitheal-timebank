
package ie.ucd.comp41670.meitheal.domain;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an immutable entry in the transaction ledger.
 *
 * R3: Records valid credit movements.
 * R7: Ledger entries are append-only and cannot be modified.
 * R12: Ledger entries can be inspected without modifying them.
 */
public record LedgerEntry(
        String entryId,
        MemberId fromMemberId,
        MemberId toMemberId,
        int amount,
        TransactionType type,
        Instant timestamp
) {

    public LedgerEntry {
        if (entryId == null || entryId.isBlank()) {
            throw new IllegalArgumentException(
                    "Entry ID cannot be null or blank"
            );
        }

        Objects.requireNonNull(type, "Transaction type cannot be null");

        Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Ledger amount cannot be negative"
            );
        }
    }
}
