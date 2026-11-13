package ie.ucd.comp41670.meitheal.session;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.time.LocalDateTime;
import java.util.Objects;

public class SessionServiceImpl {
    private final CancellationManager cancellationManager;

    public SessionServiceImpl() {
        this(new CancellationManager());
    }

    public SessionServiceImpl(CancellationManager cancellationManager) {
        this.cancellationManager = Objects.requireNonNull(cancellationManager, "CancellationManager cannot be null");
    }

    public Session createSession(EngagementId engagementId, MemberId requesterId, MemberId providerId, SessionDuration duration) {
        return new Session(engagementId, requesterId, providerId, duration);
    }

    public Session createSession(String engagementId, String requesterId, String providerId, SessionDuration duration) {
        return new Session(engagementId, requesterId, providerId, duration);
    }

    public Session createSession(EngagementId engagementId, String requesterId, String providerId, int minutes) {
        return new Session(engagementId, requesterId, providerId, minutes);
    }

    public Session createSession(String engagementId, String requesterId, String providerId, int minutes) {
        return new Session(engagementId, requesterId, providerId, minutes);
    }

    public void confirm(Session session, MemberId memberId) {
        requireSession(session);
        session.confirm(memberId);
    }

    public void confirm(Session session, String memberId) {
        requireSession(session);
        session.confirm(memberId);
    }

    public boolean canTransferCredits(Session session) {
        requireSession(session);
        return session.canTransferCredits();
    }

    public void requireConfirmed(Session session) {
        requireSession(session);
        if (!session.hasBothConfirmations()) {
            throw new IllegalStateException("R2 Violation: Both provider and requester must confirm before credits transfer");
        }
    }

    public boolean cancel(Session session, String cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        return cancellationManager.cancel(session, cancellingMemberId, agreedStart, cancellationTime);
    }

    public boolean cancel(Session session, MemberId cancellingMemberId, LocalDateTime agreedStart, LocalDateTime cancellationTime) {
        return cancellationManager.cancel(session, cancellingMemberId == null ? null : cancellingMemberId.value(), agreedStart, cancellationTime);
    }

    private static void requireSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
    }
}
