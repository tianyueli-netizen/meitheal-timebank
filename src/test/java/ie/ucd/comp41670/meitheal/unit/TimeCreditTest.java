package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.TimeCredit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeCreditTest {

    @Test
    void convertsMinutesToCredits() {
        assertEquals(60, TimeCredit.fromMinutes(60).credits());
        assertEquals(90, TimeCredit.fromMinutes(90).credits());
    }

    @Test
    void allowsZeroCredits() {
        assertEquals(0, new TimeCredit(0).credits());
        assertEquals(0, TimeCredit.fromMinutes(0).credits());
    }

    @Test
    void rejectsNegativeValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TimeCredit(-1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> TimeCredit.fromMinutes(-1)
        );
    }
}