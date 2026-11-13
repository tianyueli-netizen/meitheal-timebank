package ie.ucd.comp41670.meitheal.session;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CancellationManager {
    private final Map<String, Integer> cancellationsByMember = new HashMap<>();

    public CancellationManager() {
    }

    public boolean isLateCancellation(LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        if (agreedStart == null || cancellationTime == null) {
            throw new IllegalArgumentException("Agreed start and cancellation time cannot be null");
        }
        LocalDateTime threshold = agreedStart.minusHours(24);
        return cancellationTime.isBefore(agreedStart)
                && cancellationTime.isAfter(threshold);
    }

    public boolean isLateCancellation(Instant agreedStart, Instant cancellationTime) {
        if (agreedStart == null || cancellationTime == null) {
            throw new IllegalArgumentException("Agreed start and cancellation time cannot be null");
        }
        Instant threshold = agreedStart.minus(Duration.ofHours(24));
        return cancellationTime.isBefore(agreedStart)
                && cancellationTime.isAfter(threshold);
    }

    public boolean shouldTransferCredits(Session session, String cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        if (!session.hasBothConfirmations()) {
            return false;
        }
        return !isLateCancellation(agreedStart, cancellationTime);
    }

    public boolean shouldTransferCredits(Session session, MemberId cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        return shouldTransferCredits(session, cancellingMemberId == null ? null : cancellingMemberId.value(), agreedStart, cancellationTime);
    }

    public boolean cancel(Session session, String cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        if (cancellingMemberId == null || cancellingMemberId.isBlank()) {
            throw new IllegalArgumentException("Cancelling member ID cannot be null or blank");
        }
        boolean late = isLateCancellation(agreedStart, cancellationTime);
        if (late) {
            recordCancellation(cancellingMemberId);
            session.markCancelled();
            return false;
        }
        session.markCancelled();
        return true;
    }

    public boolean cancel(Session session, MemberId cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        return cancel(session, cancellingMemberId == null ? null : cancellingMemberId.value(), agreedStart, cancellationTime);
    }

    public void recordCancellation(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        String key = memberId.trim();
        cancellationsByMember.merge(key, 1, Integer::sum);
    }

    public void recordCancellation(MemberId memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        recordCancellation(memberId.value());
    }

    public int getRecordedCancellations(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        return cancellationsByMember.getOrDefault(memberId.trim(), 0);
    }

    public boolean hasRecordedCancellation(String memberId) {
        return getRecordedCancellations(memberId) > 0;
    }

    public int getRecordedCancellations(MemberId memberId) {
        return memberId == null ? 0 : getRecordedCancellations(memberId.value());
    }

    public boolean hasRecordedCancellation(MemberId memberId) {
        return getRecordedCancellations(memberId) > 0;
    }

    public Map<String, Integer> getAllRecordedCancellations() {
        return Map.copyOf(cancellationsByMember);
    }

    public boolean transfersCredits(Session session, String cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        return shouldTransferCredits(session, cancellingMemberId, agreedStart, cancellationTime);
    }

    public boolean transfersCredits(Session session, MemberId cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        return shouldTransferCredits(session, cancellingMemberId, agreedStart, cancellationTime);
    }
}
