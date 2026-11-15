
package ie.ucd.comp41670.meitheal.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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

        System.setOut(new PrintStream(output));

        MeithealCli cli = new MeithealCli();
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
        String output = runCli("5\n0\n");

        assertTrue(output.contains("Invalid option. Please try again."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    @DisplayName("Input 1: Create Request")
    void shouldDisplayCreateRequestMessage() {
        String output = runCli("1\n0\n");

        assertTrue(output.contains("Create Request selected."));
    }

    @Test
    @DisplayName("Input 2: View Requests")
    void shouldDisplayViewRequestsMessage() {
        String output = runCli("2\n0\n");

        assertTrue(output.contains("View Requests selected."));
    }

    @Test
    @DisplayName("Input 3: View Offers")
    void shouldDisplayViewOffersMessage() {
        String output = runCli("3\n0\n");

        assertTrue(output.contains("View Offers selected."));
    }

    @Test
    @DisplayName("Input 4: Accept Offer")
    void shouldDisplayAcceptOfferMessage() {
        String output = runCli("4\n0\n");

        assertTrue(output.contains("Accept Offer selected."));
    }
}

