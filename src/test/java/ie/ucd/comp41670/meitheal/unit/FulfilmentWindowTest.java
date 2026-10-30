
package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FulfilmentWindowTest {

    private final Instant earliest =
            Instant.parse("2026-11-01T10:00:00Z");

    private final Instant latest =
            Instant.parse("2026-11-01T12:00:00Z");

    @Test
    void shouldCreateWindowWhenEarliestIsBeforeLatest() {
        assertDoesNotThrow(
                () -> new FulfilmentWindow(earliest, latest)
        );
    }

    @Test
    void shouldAllowEarliestAndLatestToBeEqual() {
        assertDoesNotThrow(
                () -> new FulfilmentWindow(earliest, earliest)
        );
    }

    @Test
    void shouldRejectWindowWhenEarliestIsAfterLatest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FulfilmentWindow(latest, earliest)
        );
    }

    @Test
    void shouldRejectNullEarliest() {
        assertThrows(
                NullPointerException.class,
                () -> new FulfilmentWindow(null, latest)
        );
    }

    @Test
    void shouldRejectNullLatest() {
        assertThrows(
                NullPointerException.class,
                () -> new FulfilmentWindow(earliest, null)
        );
    }
}
