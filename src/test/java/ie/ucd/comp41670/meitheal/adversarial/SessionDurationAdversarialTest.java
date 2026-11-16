package ie.ucd.comp41670.meitheal.adversarial;

import ie.ucd.comp41670.meitheal.session.SessionDuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionDurationAdversarialTest {

    @Test
    void negativeMinutesMinusOneShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> SessionDuration.ofMinutes(-1));
    }

    @Test
    void zeroMinutesShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> SessionDuration.ofMinutes(0));
    }

    @Test
    void exactUpperBoundary480MinutesShouldBeAcceptedAndMapTo480Credits() {
        SessionDuration duration = SessionDuration.ofMinutes(480);
        assertEquals(480, duration.toCredits());
    }

    @Test
    void aboveUpperBoundary481MinutesShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> SessionDuration.ofMinutes(481));
    }

    @Test
    void fractionalHourBoundaryEightPointZeroZeroOneHoursShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> SessionDuration.ofHours(8.001));
    }
}
