package ie.ucd.comp41670.meitheal.policy;

final class StandardOverdraftPolicy {
    static final int MINIMUM_BALANCE = -180;

    private StandardOverdraftPolicy() {
    }

    static long capLevy(long nominalLevy, long balanceAfterLabor) {
        long balanceBasedLevyCap = Math.max(0L, balanceAfterLabor - MINIMUM_BALANCE);
        return Math.min(nominalLevy, balanceBasedLevyCap);
    }
}
