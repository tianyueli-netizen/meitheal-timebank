package ie.ucd.comp41670.meitheal.policy;

import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import ie.ucd.comp41670.meitheal.domain.TimeCredit;
import ie.ucd.comp41670.meitheal.policy.exceptions.OverdraftFloorException;

import java.util.Objects;

public class TensionArbiter {
    public SettlementResult calculateSettlement(int requesterBalance, TimeCredit laborCost) {
        Objects.requireNonNull(laborCost, "laborCost must not be null");

        if (laborCost.credits() <= 0) {
            throw new IllegalArgumentException("Labor cost must be positive.");
        }

        if (requesterBalance < StandardOverdraftPolicy.MINIMUM_BALANCE) {
            throw new OverdraftFloorException("Initial balance cannot be below -180.");
        }

        long balanceAfterLabor = (long) requesterBalance - laborCost.credits();
        if (balanceAfterLabor < StandardOverdraftPolicy.MINIMUM_BALANCE) {
            return new SettlementResult(0, 0, false);
        }

        long nominalLevy = calculateNominalLevy(laborCost.credits());
        long levy = StandardOverdraftPolicy.capLevy(nominalLevy, balanceAfterLabor);
        return new SettlementResult(laborCost.credits(), (int) levy, true);
    }

    private long calculateNominalLevy(int laborCredits) {
        return (long) laborCredits / 20L;
    }
}
