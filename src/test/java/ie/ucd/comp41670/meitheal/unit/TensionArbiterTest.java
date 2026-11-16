package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.TimeCredit;
import ie.ucd.comp41670.meitheal.policy.TensionArbiter;
import ie.ucd.comp41670.meitheal.policy.exceptions.OverdraftFloorException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TensionArbiterTest {

    private final TensionArbiter arbiter = new TensionArbiter();

    @Test
    void invalidInitialBalanceThrowsOverdraftFloorException() {
        assertThrows(
                OverdraftFloorException.class,
                () -> arbiter.calculateSettlement(-181, new TimeCredit(1))
        );
    }
}
