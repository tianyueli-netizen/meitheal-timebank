package ie.ucd.comp41670.meitheal.session;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CancellationManager {
    private final Map<MemberId, Integer> cancellationsByMember = new HashMap<>();

    public void recordCancellation(MemberId memberId) {
        Objects.requireNonNull(memberId, "Member ID cannot be null");
        cancellationsByMember.merge(memberId, 1, Integer::sum);
    }

    public int getCancellationCount(MemberId memberId) {
        Objects.requireNonNull(memberId, "Member ID cannot be null");
        return cancellationsByMember.getOrDefault(memberId, 0);
    }

    public boolean isLateCancellation(Session session, Instant cancellationInstant) {
        Objects.requireNonNull(session, "Session cannot be null");
        Objects.requireNonNull(cancellationInstant, "Cancellation instant cannot be null");
        return isLateCancellation(session, LocalDateTime.ofInstant(cancellationInstant, java.time.ZoneId.systemDefault()));
    }

    public boolean isLateCancellation(Session session, LocalDateTime cancellationTime) {
        Objects.requireNonNull(session, "Session cannot be null");
        Objects.requireNonNull(cancellationTime, "Cancellation time cannot be null");
        if (!session.isConfirmed()) {
            return false;
        }
        LocalDateTime agreedStart = session.getAgreedStart();
        if (agreedStart == null) {
            return false;
        }
        Duration timeUntilStart = Duration.between(cancellationTime, agreedStart);
        return !timeUntilStart.isNegative() && timeUntilStart.toHours() < 24;
    }

    public boolean cancelSession(Session session, MemberId cancellingMember, Instant cancellationInstant) {
        Objects.requireNonNull(session, "Session cannot be null");
        Objects.requireNonNull(cancellingMember, "Cancelling member cannot be null");
        Objects.requireNonNull(cancellationInstant, "Cancellation instant cannot be null");
        return cancelSession(session, cancellingMember, LocalDateTime.ofInstant(cancellationInstant, java.time.ZoneId.systemDefault()));
    }

    public boolean cancelSession(Session session, MemberId cancellingMember, LocalDateTime cancellationTime) {
        Objects.requireNonNull(session, "Session cannot be null");
        Objects.requireNonNull(cancellingMember, "Cancelling member cannot be null");
        Objects.requireNonNull(cancellationTime, "Cancellation time cannot be null");

        boolean lateCancellation = isLateCancellation(session, cancellationTime);
        if (lateCancellation) {
            session.cancel();
            recordCancellation(cancellingMember);
            return false;
        }

        if (session.isConfirmed() && !session.isCancelled()) {
            session.cancel();
        }
        return true;
    }

    public int getCancellationsFor(MemberId memberId) {
        return getCancellationCount(memberId);
    }

    public boolean cancelConfirmedEngagement(Session session, MemberId cancellingMember, Instant cancellationInstant) {
        return cancelSession(session, cancellingMember, cancellationInstant);
    }

    public boolean cancelConfirmedEngagement(Session session, MemberId cancellingMember, LocalDateTime cancellationTime) {
        return cancelSession(session, cancellingMember, cancellationTime);
    }
}
