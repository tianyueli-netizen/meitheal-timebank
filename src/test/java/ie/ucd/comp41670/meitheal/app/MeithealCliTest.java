package ie.ucd.comp41670.meitheal.app;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import ie.ucd.comp41670.meitheal.engagement.EngagementServiceImpl;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MeithealCliTest {
    private static final String EARLIEST = "2026-11-16T09:00:00Z";
    private static final String LATEST = "2026-11-20T17:00:00Z";
    private record Members(String requester, String provider) { }

    @Test
    void shouldExitWhenInputIsZero() throws Exception {
        try (Cli cli = new Cli()) {
            assertTrue(cli.startup.contains("Welcome to Meitheal Timebank"));
            assertTrue(cli.exit().contains("Goodbye!"));
        }
    }

    @Test
    void shouldDisplayErrorForInvalidOption() throws Exception {
        try (Cli cli = new Cli()) {
            assertTrue(cli.command("99").contains("Invalid option. Please try again."));
        }
    }

    @Test
    void shouldCreateRequestSuccessfully() throws Exception {
        try (Cli cli = new Cli()) {
            String member = cli.register("Alice", "alice@example.test");
            String response = cli.command("1", "REQ001", member, "GARDENING",
                    "Help with gardening", EARLIEST, LATEST);
            assertTrue(response.contains("Request created successfully!"));
            String records = cli.command("2");
            assertTrue(records.contains("Request ID: REQ001"));
            assertTrue(records.contains("Requester ID: " + member));
        }
    }

    @Test
    void shouldDisplayViewRequestsMessage() throws Exception {
        try (Cli cli = new Cli()) {
            assertTrue(cli.command("2").contains("No requests available."));
        }
    }

    @Test
    void shouldDisplayViewOffersMessage() throws Exception {
        try (Cli cli = new Cli()) {
            assertTrue(cli.command("3").contains("No offers available."));
        }
    }

    @Test
    void shouldHandleAcceptOfferWhenRecordsDoNotExist() throws Exception {
        try (Cli cli = new Cli()) {
            assertTrue(cli.command("4").contains("No requests available."));
        }
    }

    @Test
    void completedSessionSettlesRealBalancesAndLedgerExactlyOnce() throws Exception {
        try (Cli cli = new Cli()) {
            Members m = members(cli);
            assertEquals(120, cli.balance(m.requester()));
            assertEquals(120, cli.balance(m.provider()));
            assertTrue(cli.command("13", "ALL").contains("Total transactions: 2"));
            assertTrue(cli.command("8", "Duplicate", "ALICE@example.test")
                    .contains("already registered"));

            String engagement = job(cli, "NORMAL", m, 60, "2026-11-16T10:00:00Z");
            assertTrue(cli.command("6", engagement, m.requester()).contains("option 15"));
            accept(cli, engagement, m);
            assertEquals(120, cli.balance(m.requester()));
            assertTrue(cli.command("6", engagement, m.requester()).contains("waiting for the other party"));
            assertEquals(120, cli.balance(m.requester()));
            assertTrue(cli.command("14").contains("Global ledger entries: 2"));

            assertTrue(cli.command("6", engagement, m.provider()).contains("Actual levy: 3"));
            assertEquals(57, cli.balance(m.requester()));
            assertEquals(180, cli.balance(m.provider()));
            String report = cli.command("14");
            assertTrue(report.contains("Community fund balance: 3"));
            assertTrue(report.contains("Total credits in circulation: 240"));
            assertTrue(report.contains("Authorised issued credits: 240"));
            assertTrue(report.contains("Invariant audit: OK"));

            String ledger = cli.command("13", "ALL");
            assertTrue(ledger.contains("Total transactions: 4"));
            assertEquals(1, occurrences(ledger, "TRANSFER |"));
            assertEquals(1, occurrences(ledger, "COMMUNITY_LEVY |"));
            assertTrue(cli.command("6", engagement, m.requester()).contains("Already settled"));
            assertTrue(cli.command("6", engagement, m.provider()).contains("Already settled"));
            assertEquals(ledger, cli.command("13", "ALL"));
            assertEquals(57, cli.balance(m.requester()));
            assertTrue(cli.command("13", m.requester()).contains("Total transactions: 3"));
            assertTrue(cli.command("7", m.requester()).contains(engagement));
            String outsider = cli.register("Other", "other@example.test");
            assertFalse(cli.command("7", outsider).contains(engagement));
            assertTrue(cli.command("14").contains("Total credits in circulation: 360"));
        }
    }

    @Test
    void suspensionBlocksNewWorkButAllowsPaymentForAnExistingAgreement() throws Exception {
        try (Cli cli = new Cli()) {
            Members m = members(cli);
            String engagement = job(cli, "EARNED", m, 60, "2026-11-16T10:00:00Z");
            accept(cli, engagement, m);
            assertTrue(cli.command("9", m.provider(), "SUSPEND").contains("SUSPENDED"));
            assertTrue(cli.command("5", "OFF-BLOCKED", m.provider(), "GARDENING", "Help")
                    .contains("Suspended member"));
            assertTrue(cli.command("1", "REQ-BLOCKED", m.provider(), "GARDENING", "Help",
                    EARLIEST, LATEST).contains("Suspended member"));
            assertTrue(cli.command("4", "REQ-EARNED", "OFF-EARNED",
                    "2026-11-16T10:00:00Z", "60").contains("Suspended member"));
            cli.command("6", engagement, m.requester());
            assertTrue(cli.command("6", engagement, m.provider()).contains("Settlement approved."));
            assertEquals(180, cli.balance(m.provider()));
            assertTrue(cli.command("9", m.provider(), "REINSTATE").contains("ACTIVE"));
            assertTrue(cli.command("5", "OFF-RESTORED", m.provider(), "GARDENING", "Help")
                    .contains("Offer created successfully!"));
        }
    }

    @Test
    void floorWaiverRejectionAndReliefPreserveBalancesAndLedger() throws Exception {
        try (Cli cli = new Cli()) {
            Members m = members(cli);
            String first = job(cli, "PREP", m, 229, "2026-11-16T10:00:00Z");
            settle(cli, first, m);
            assertEquals(-120, cli.balance(m.requester()));
            String floor = job(cli, "FLOOR", m, 60, "2026-11-16T10:00:00Z");
            assertTrue(settle(cli, floor, m).contains("Actual levy: 0"));
            assertEquals(-180, cli.balance(m.requester()));
            assertEquals(409, cli.balance(m.provider()));
            String before = cli.command("13", "ALL");
            assertTrue(before.contains("Total transactions: 5"));
            assertEquals(1, occurrences(before, "COMMUNITY_LEVY |"));

            String rejected = job(cli, "REJECT", m, 1, "2026-11-16T10:00:00Z");
            assertTrue(settle(cli, rejected, m).contains("Settlement rejected"));
            assertEquals(-180, cli.balance(m.requester()));
            assertEquals(409, cli.balance(m.provider()));
            assertEquals(before, cli.command("13", "ALL"));

            assertTrue(cli.command("11", m.requester(), "2").contains("Community grant approved"));
            assertEquals(-178, cli.balance(m.requester()));
            String afterGrant = cli.command("13", "ALL");
            assertEquals(1, occurrences(afterGrant, "FUND_GRANT |"));
            assertTrue(cli.command("11", m.requester(), "1").contains("already received"));
            assertEquals(afterGrant, cli.command("13", "ALL"));
            assertTrue(cli.command("11", m.requester(), "20").contains("Insufficient fund"));
            assertTrue(cli.command("11", m.provider(), "1").contains("not below zero"));
            String report = cli.command("14");
            assertTrue(report.contains("Community fund balance: 9"));
            assertTrue(report.contains("Total credits in circulation: 240"));
            assertTrue(report.contains("Authorised issued credits: 240"));
            assertTrue(report.contains("Invariant audit: OK"));
        }
    }

    @Test
    void cancellationHonoursParticipantsIdempotencyAndExact24HourBoundary() throws Exception {
        try (Cli cli = new Cli()) {
            Members m = members(cli);
            String outsider = cli.register("Other", "other@example.test");
            String late = job(cli, "LATE", m, 60, "2026-11-16T13:00:00Z");
            accept(cli, late, m);
            assertTrue(cli.command("10", late, outsider).contains("not a participant"));
            String cancelled = cli.command("10", late, m.requester());
            assertTrue(cancelled.contains("Late cancellation: true"));
            assertTrue(cancelled.contains("Late cancellation count: 1"));
            assertTrue(cli.command("10", late, m.requester()).contains("already cancelled"));
            assertTrue(cli.command("6", late, m.provider()).contains("cancelled"));

            String exact = job(cli, "EXACT", m, 60, "2026-11-17T12:00:00Z");
            accept(cli, exact, m);
            String exactResult = cli.command("10", exact, m.provider());
            assertTrue(exactResult.contains("Late cancellation: false"));
            assertTrue(exactResult.contains("Late cancellation count: 0"));
            String inside = job(cli, "INSIDE", m, 60, "2026-11-17T11:59:59Z");
            accept(cli, inside, m);
            assertTrue(cli.command("10", inside, m.provider()).contains("Late cancellation: true"));

            String proposal = job(cli, "PROPOSAL", m, 60, "2026-11-16T13:00:00Z");
            assertTrue(cli.command("10", proposal, m.provider()).contains("Late cancellation: false"));
            assertTrue(cli.command("6", proposal, m.requester()).contains("cancelled"));
            assertEquals(120, cli.balance(m.requester()));
            assertEquals(120, cli.balance(m.provider()));
            assertTrue(cli.command("13", "ALL").contains("Total transactions: 3"));
            assertTrue(cli.command("14").contains("Total credits in circulation: 360"));

            String paid = job(cli, "PAID", m, 60, "2026-11-16T10:00:00Z");
            settle(cli, paid, m);
            String ledger = cli.command("13", "ALL");
            assertTrue(cli.command("10", paid, m.requester()).contains("cannot be cancelled"));
            assertEquals(ledger, cli.command("13", "ALL"));
        }
    }

    @Test
    void invalidMembersCategoriesDatesAndDurationsCreateNoEngagement() throws Exception {
        try (Cli cli = new Cli()) {
            Members m = members(cli);
            assertTrue(cli.command("1", "UNKNOWN", "MISSING", "GARDENING", "Help", EARLIEST, LATEST)
                    .contains("Member not found"));
            assertTrue(cli.command("5", "BAD", m.provider(), "DANCING", "Help")
                    .contains("Invalid skill category"));
            cli.command("1", "REQ", m.requester(), "GARDENING", "Help", EARLIEST, LATEST);
            cli.command("5", "OFF", m.provider(), "GARDENING", "Help");
            assertTrue(cli.command("4", "REQ", "OFF", "2026-11-21T10:00:00Z", "60")
                    .contains("within the fulfilment window"));
            assertTrue(cli.command("4", "REQ", "OFF", "2026-11-16T10:00:00Z", "0")
                    .contains("Session duration"));
            assertTrue(cli.command("4", "REQ", "OFF", "2026-11-16T10:00:00Z", "481")
                    .contains("Session duration"));
            cli.command("5", "SELF", m.requester(), "GARDENING", "Help");
            assertTrue(cli.command("4", "REQ", "SELF", "2026-11-16T10:00:00Z", "60")
                    .contains("distinct members"));
            cli.command("5", "COOK", m.provider(), "COOKING", "Help");
            assertTrue(cli.command("4", "REQ", "COOK", "2026-11-16T10:00:00Z", "60")
                    .contains("categories must match"));
            assertTrue(cli.command("7", m.requester()).contains("No engagements available."));
            assertTrue(cli.command("14").contains("Global ledger entries: 2"));
        }
    }

    private static Members members(Cli cli) throws Exception {
        return new Members(cli.register("Alice", "alice@example.test"),
                cli.register("Bob", "bob@example.test"));
    }

    private static String job(Cli cli, String id, Members m, int minutes, String start) throws Exception {
        assertTrue(cli.command("1", "REQ-" + id, m.requester(), "GARDENING", "Help", EARLIEST, LATEST)
                .contains("Request created successfully!"));
        assertTrue(cli.command("5", "OFF-" + id, m.provider(), "GARDENING", "Help")
                .contains("Offer created successfully!"));
        String output = cli.command("4", "REQ-" + id, "OFF-" + id, start, Integer.toString(minutes));
        var match = Pattern.compile("Engagement ID: ([a-fA-F0-9-]{36})").matcher(output);
        assertTrue(match.find(), output);
        return match.group(1);
    }

    private static void accept(Cli cli, String id, Members m) throws Exception {
        cli.command("15", id, m.requester());
        assertTrue(cli.command("15", id, m.provider()).contains("Agreement confirmed by both parties."));
    }

    private static String settle(Cli cli, String id, Members m) throws Exception {
        accept(cli, id, m);
        cli.command("6", id, m.requester());
        return cli.command("6", id, m.provider());
    }

    private static int occurrences(String value, String token) {
        return (value.length() - value.replace(token, "").length()) / token.length();
    }

    /** Drives the actual console through its public constructor, without reflection or mocks. */
    private static final class Cli implements AutoCloseable {
        private final BlockingQueue<Integer> output = new LinkedBlockingQueue<>();
        private final PipedInputStream input = new PipedInputStream(65536);
        private final PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new PipedOutputStream(input), StandardCharsets.UTF_8), true);
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Future<?> task;
        private final String startup;

        private Cli() throws Exception {
            var service = new EngagementServiceImpl(Set.of(new SkillCategory("GARDENING"),
                    new SkillCategory("TRANSPORT"), new SkillCategory("COOKING")));
            var stream = new PrintStream(new OutputStream() {
                @Override public void write(int value) { output.add(value & 255); }
            }, true, StandardCharsets.UTF_8);
            var clock = Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
            var cli = new MeithealCli(service, new Scanner(input, StandardCharsets.UTF_8), stream, clock);
            task = executor.submit(cli::start);
            try {
                startup = readUntil("Your choice: ");
            } catch (Throwable failure) {
                writer.close();
                executor.shutdownNow();
                throw failure;
            }
        }

        private String command(String choice, String... fields) throws Exception {
            writer.println(choice);
            for (String field : fields) writer.println(field);
            assertFalse(writer.checkError(), "CLI input pipe failed");
            return readUntil("Your choice: ");
        }

        private String register(String name, String email) throws Exception {
            String response = command("8", name, email);
            var match = Pattern.compile("Member registered: (MEM-[a-fA-F0-9-]+)").matcher(response);
            assertTrue(match.find(), response);
            return match.group(1);
        }

        private int balance(String member) throws Exception {
            String response = command("12", member);
            var match = Pattern.compile("Member ID: " + Pattern.quote(member)
                    + "[^\\n]*?\\| Balance: (-?\\d+)").matcher(response);
            assertTrue(match.find(), response);
            return Integer.parseInt(match.group(1));
        }

        private String exit() throws Exception {
            writer.println("0");
            String response = readUntil("Goodbye!");
            task.get(5, TimeUnit.SECONDS);
            return response;
        }

        private String readUntil(String token) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StringBuilder ending = new StringBuilder();
            while (true) {
                long remaining = deadline - System.nanoTime();
                assertTrue(remaining > 0, "Timed out waiting for " + token + ": " + bytes);
                Integer value = output.poll(remaining, TimeUnit.NANOSECONDS);
                assertNotNull(value, "CLI stopped before " + token + ": " + bytes);
                bytes.write(value);
                ending.append((char) value.intValue());
                if (ending.length() > token.length()) ending.deleteCharAt(0);
                if (ending.toString().equals(token)) return bytes.toString(StandardCharsets.UTF_8);
            }
        }

        @Override public void close() throws Exception {
            writer.close();
            try {
                task.get(5, TimeUnit.SECONDS);
            } finally {
                input.close();
                executor.shutdownNow();
            }
        }
    }
}
