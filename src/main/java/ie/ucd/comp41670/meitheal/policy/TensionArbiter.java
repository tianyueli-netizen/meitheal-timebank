package ie.ucd.comp41670.meitheal.policy;

import ie.ucd.comp41670.meitheal.domain.SettlementResult;
import ie.ucd.comp41670.meitheal.domain.TimeCredit;

import java.util.Objects;

public class TensionArbiter {
    private static final int MINIMUM_BALANCE = -180;

    public SettlementResult calculateSettlement(int requesterBalance, TimeCredit laborCost) {
        Objects.requireNonNull(laborCost, "laborCost must not be null");

        if (laborCost.credits() <= 0) {
            throw new IllegalArgumentException("Labor cost must be positive.");
        }

        if (requesterBalance < MINIMUM_BALANCE) {
            throw new IllegalArgumentException("Initial balance cannot be below -180.");
        }

        long balanceAfterLabor = (long) requesterBalance - laborCost.credits();
        if (balanceAfterLabor < MINIMUM_BALANCE) {
            return new SettlementResult(0, 0, false);
        }

        long levy = calculateLevy(laborCost.credits(), balanceAfterLabor);
        return new SettlementResult(laborCost.credits(), (int) levy, true);
    }

    private long calculateLevy(int laborCredits, long balanceAfterLabor) {
        long laborLevyCap = (long) laborCredits / 20L;
        long balanceBasedLevyCap = Math.max(0L, balanceAfterLabor - MINIMUM_BALANCE);
        return Math.min(laborLevyCap, balanceBasedLevyCap);
    }
}
