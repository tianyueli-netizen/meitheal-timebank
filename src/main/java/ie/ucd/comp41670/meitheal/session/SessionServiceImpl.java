package ie.ucd.comp41670.meitheal.session;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class SessionServiceImpl {
    private final CancellationManager cancellationManager;

    public SessionServiceImpl() {
        this(new CancellationManager());
    }

    public SessionServiceImpl(CancellationManager cancellationManager) {
        this.cancellationManager = Objects.requireNonNull(cancellationManager, "Cancellation manager cannot be null");
    }

    public Session createSession(EngagementId engagementId, MemberId providerId, MemberId requesterId, SessionDuration duration) {
        return new Session(engagementId, providerId, requesterId, duration);
    }

    public Session createSession(MemberId providerId, MemberId requesterId, SessionDuration duration) {
        return new Session(providerId, requesterId, duration);
    }

    public Session createSession(EngagementId engagementId, MemberId providerId, MemberId requesterId, int durationMinutes) {
        return new Session(engagementId, providerId, requesterId, durationMinutes);
    }

    public Session createSession(MemberId providerId, MemberId requesterId, int durationMinutes) {
        return new Session(providerId, requesterId, durationMinutes);
    }

    public Session createSession(EngagementId engagementId, MemberId providerId, MemberId requesterId, double durationHours) {
        return new Session(engagementId, providerId, requesterId, durationHours);
    }

    public Session createSession(MemberId providerId, MemberId requesterId, double durationHours) {
        return new Session(providerId, requesterId, durationHours);
    }

    public Session recordSession(EngagementId engagementId, MemberId providerId, MemberId requesterId, SessionDuration duration) {
        return createSession(engagementId, providerId, requesterId, duration);
    }

    public Session recordSession(MemberId providerId, MemberId requesterId, SessionDuration duration) {
        return createSession(providerId, requesterId, duration);
    }

    public Session recordSession(EngagementId engagementId, MemberId providerId, MemberId requesterId, double durationHours) {
        return createSession(engagementId, providerId, requesterId, durationHours);
    }

    public Session recordSession(MemberId providerId, MemberId requesterId, double durationHours) {
        return createSession(providerId, requesterId, durationHours);
    }

    public void confirm(Session session, MemberId memberId) {
        Objects.requireNonNull(session, "Session cannot be null");
        session.confirm(memberId);
    }

    public void recordConfirmation(Session session, MemberId memberId) {
        confirm(session, memberId);
    }

    public boolean isConfirmed(Session session) {
        return session != null && session.isConfirmed();
    }

    public int transferCredits(Session session) {
        Objects.requireNonNull(session, "Session cannot be null");
        return session.transferCredits();
    }

    public boolean cancel(Session session, MemberId cancellingMember, Instant cancellationInstant) {
        return cancellationManager.cancelSession(session, cancellingMember, cancellationInstant);
    }

    public boolean cancel(Session session, MemberId cancellingMember, LocalDateTime cancellationTime) {
        return cancellationManager.cancelSession(session, cancellingMember, cancellationTime);
    }

    public void cancelConfirmedEngagement(Session session, MemberId cancellingMember, Instant cancellationInstant) {
        cancellationManager.cancelConfirmedEngagement(session, cancellingMember, cancellationInstant);
    }

    public void cancelConfirmedEngagement(Session session, MemberId cancellingMember, LocalDateTime cancellationTime) {
        cancellationManager.cancelConfirmedEngagement(session, cancellingMember, cancellationTime);
    }

    public int getCancellationCount(MemberId memberId) {
        return cancellationManager.getCancellationCount(memberId);
    }

    public int getCancellationsFor(MemberId memberId) {
        return getCancellationCount(memberId);
    }
}
