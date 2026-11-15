
package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.TransactionType;
import ie.ucd.comp41670.meitheal.ledger.AppendOnlyLedger;
import ie.ucd.comp41670.meitheal.ledger.LedgerReport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerReportTest {

    private AppendOnlyLedger ledger;
    private LedgerReport report;

    private MemberId memberA;
    private MemberId memberB;
    private MemberId memberC;

    @BeforeEach
    void setUp() {
        ledger = new AppendOnlyLedger();
        report = new LedgerReport(ledger);

        memberA = new MemberId("MEM-A");
        memberB = new MemberId("MEM-B");
        memberC = new MemberId("MEM-C");
    }

    @Test
    void emptyLedgerReport() {
        String result = report.generateFullReport();

        assertTrue(result.contains("No transactions found."));
        assertTrue(result.contains("Total transactions: 0"));
    }

    @Test
    void fullReportContainsAllEntries() {
        ledger.append(new LedgerEntry(
                "TX-001", null, memberA, 120,
                TransactionType.WELCOME_GRANT,
                Instant.EPOCH
        ));

        ledger.append(new LedgerEntry(
                "TX-002", memberA, memberB, 60,
                TransactionType.TRANSFER,
                Instant.EPOCH
        ));

        String result = report.generateFullReport();

        assertTrue(result.contains("TX-001"));
        assertTrue(result.contains("TX-002"));
        assertTrue(result.contains("WELCOME_GRANT"));
        assertTrue(result.contains("TRANSFER"));
        assertTrue(result.contains("Amount: 120"));
        assertTrue(result.contains("Amount: 60"));
        assertTrue(result.contains("System/Fund"));
        assertTrue(result.contains("Total transactions: 2"));
    }

    @Test
    void memberReportContainsOnlyRelevantEntries() {
        ledger.append(new LedgerEntry(
                "TX-001", memberA, memberB, 60,
                TransactionType.TRANSFER,
                Instant.EPOCH
        ));

        ledger.append(new LedgerEntry(
                "TX-002", memberB, memberA, 30,
                TransactionType.TRANSFER,
                Instant.EPOCH
        ));

        ledger.append(new LedgerEntry(
                "TX-003", memberB, memberC, 20,
                TransactionType.TRANSFER,
                Instant.EPOCH
        ));

        String result = report.generateMemberReport(memberA);

        assertTrue(result.contains("TX-001"));
        assertTrue(result.contains("TX-002"));
        assertFalse(result.contains("TX-003"));
        assertTrue(result.contains("Total transactions: 2"));
    }

    @Test
    void summaryCountsTransactionTypes() {
        ledger.append(new LedgerEntry(
                "TX-001", null, memberA, 120,
                TransactionType.WELCOME_GRANT,
                Instant.EPOCH
        ));

        ledger.append(new LedgerEntry(
                "TX-002", null, memberB, 120,
                TransactionType.WELCOME_GRANT,
                Instant.EPOCH
        ));

        ledger.append(new LedgerEntry(
                "TX-003", memberA, memberB, 60,
                TransactionType.TRANSFER,
                Instant.EPOCH
        ));

        String result = report.generateSummary();

        assertTrue(result.contains("Total transactions: 3"));
        assertTrue(result.contains("WELCOME_GRANT: 2"));
        assertTrue(result.contains("TRANSFER: 1"));
        assertTrue(result.contains("COMMUNITY_LEVY: 0"));
        assertTrue(result.contains("FUND_GRANT: 0"));
    }

    @Test
    void reportDoesNotModifyLedger() {
        LedgerEntry entry = new LedgerEntry(
                "TX-001", memberA, memberB, 60,
                TransactionType.TRANSFER,
                Instant.EPOCH
        );

        ledger.append(entry);

        List<LedgerEntry> before = ledger.getAllEntries();

        report.generateFullReport();
        report.generateMemberReport(memberA);
        report.generateSummary();

        assertEquals(before, ledger.getAllEntries());
        assertEquals(1, ledger.getEntryCount());
    }

    @Test
    void reportReflectsNewEntries() {
        assertTrue(report.generateFullReport()
                .contains("Total transactions: 0"));

        ledger.append(new LedgerEntry(
                "TX-001", null, memberA, 120,
                TransactionType.WELCOME_GRANT,
                Instant.EPOCH
        ));

        String result = report.generateFullReport();

        assertTrue(result.contains("TX-001"));
        assertTrue(result.contains("Total transactions: 1"));
    }
}
