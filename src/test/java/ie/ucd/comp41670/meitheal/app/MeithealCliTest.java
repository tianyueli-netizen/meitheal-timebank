
package ie.ucd.comp41670.meitheal.app;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import ie.ucd.comp41670.meitheal.engagement.EngagementServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MeithealCliTest {

    private InputStream originalIn;
    private PrintStream originalOut;

    @BeforeEach
    void saveSystemStreams() {
        originalIn = System.in;
        originalOut = System.out;
    }

    @AfterEach
    void restoreSystemStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private String runCli(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8)));

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        Set<SkillCategory> categories = Set.of(
                new SkillCategory("GARDENING"),
                new SkillCategory("TRANSPORT"),
                new SkillCategory("COOKING")
        );

        EngagementServiceImpl service =
                new EngagementServiceImpl(categories);

        MeithealCli cli = new MeithealCli(service);
        cli.start();

        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Input 0: Exit the CLI")
    void shouldExitWhenInputIsZero() {
        String output = runCli("0\n");

        assertTrue(output.contains("Welcome to Meitheal Timebank"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    @DisplayName("Invalid input: Display error message")
    void shouldDisplayErrorForInvalidOption() {
        String output = runCli("9\n0\n");

        assertTrue(output.contains("Invalid option. Please try again."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    @DisplayName("Input 1: Create Request")
    void shouldCreateRequestSuccessfully() {
        String input =
                "1\n" +
                        "REQ001\n" +
                        "member1\n" +
                        "GARDENING\n" +
                        "Help with gardening\n" +
                        "2026-12-01T10:00:00Z\n" +
                        "2026-12-01T12:00:00Z\n" +
                        "0\n";

        String output = runCli(input);

        assertTrue(output.contains("Create Request"));
        assertTrue(output.contains("Request created successfully!"));
    }

    @Test
    @DisplayName("Input 2: View Requests")
    void shouldDisplayViewRequestsMessage() {
        String output = runCli("2\n0\n");

        assertTrue(output.contains("Available Requests"));
        assertTrue(output.contains("No requests available."));
    }

    @Test
    @DisplayName("Input 3: View Offers")
    void shouldDisplayViewOffersMessage() {
        String output = runCli("3\n0\n");

        assertTrue(output.contains("Available Offers"));
        assertTrue(output.contains("No offers available."));
    }

    @Test
    @DisplayName("Input 4: Accept Offer when no requests exist")
    void shouldHandleAcceptOfferWhenRecordsDoNotExist() {
        String output = runCli("4\n0\n");

        assertTrue(output.contains("Accept an Offer"));
        assertTrue(output.contains("No requests available."));
    }
}
