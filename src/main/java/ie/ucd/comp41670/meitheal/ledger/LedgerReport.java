
package ie.ucd.comp41670.meitheal.ledger;

import ie.ucd.comp41670.meitheal.contract.Ledger;
import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.TransactionType;

import java.util.List;
import java.util.Objects;

/**
 * Generates read-only reports from the transaction ledger.
 * Supports R7 and R12.
 */
public class LedgerReport {

    private final Ledger ledger;

    public LedgerReport(Ledger ledger) {
        this.ledger = Objects.requireNonNull(
                ledger, "Ledger cannot be null");
    }

    public String generateFullReport() {
        return formatReport(
                "Transaction Ledger",
                ledger.getAllEntries()
        );
    }

    public String generateMemberReport(MemberId memberId) {
        Objects.requireNonNull(
                memberId, "Member ID cannot be null");

        return formatReport(
                "Transaction History: " + memberId,
                ledger.getEntriesByMember(memberId)
        );
    }

    public String generateSummary() {
        List<LedgerEntry> entries = ledger.getAllEntries();

        StringBuilder report = new StringBuilder();

        report.append("Ledger Summary\n");
        report.append("Total transactions: ")
                .append(entries.size())
                .append("\n");

        for (TransactionType type : TransactionType.values()) {
            long count = entries.stream()
                    .filter(entry -> entry.type() == type)
                    .count();

            report.append(type)
                    .append(": ")
                    .append(count)
                    .append("\n");
        }

        return report.toString();
    }

    private String formatReport(
            String title, List<LedgerEntry> entries) {

        StringBuilder report = new StringBuilder();

        report.append(title).append("\n");
        report.append("--------------------------------\n");

        if (entries.isEmpty()) {
            report.append("No transactions found.\n");
        } else {
            for (LedgerEntry entry : entries) {
                report.append("ID: ")
                        .append(entry.entryId())
                        .append(" | Type: ")
                        .append(entry.type())
                        .append(" | From: ")
                        .append(formatMember(entry.fromMemberId()))
                        .append(" | To: ")
                        .append(formatMember(entry.toMemberId()))
                        .append(" | Amount: ")
                        .append(entry.amount())
                        .append(" | Time: ")
                        .append(entry.timestamp())
                        .append("\n");
            }
        }

        report.append("Total transactions: ")
                .append(entries.size())
                .append("\n");

        return report.toString();
    }

    private String formatMember(MemberId memberId) {
        return memberId == null
                ? "System/Fund"
                : memberId.toString();
    }
}
