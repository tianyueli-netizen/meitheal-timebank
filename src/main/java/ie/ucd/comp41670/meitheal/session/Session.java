package ie.ucd.comp41670.meitheal.session;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public class Session {
    private final EngagementId engagementId;
    private final MemberId providerId;
    private final MemberId requesterId;
    private final SessionDuration duration;
    private boolean providerConfirmed;
    private boolean requesterConfirmed;
    private boolean cancelled;
    private boolean creditsTransferred;
    private LocalDateTime agreedStart;

    public Session(EngagementId engagementId, MemberId providerId, MemberId requesterId, SessionDuration duration) {
        this.engagementId = Objects.requireNonNull(engagementId, "Engagement ID cannot be null");
        this.providerId = Objects.requireNonNull(providerId, "Provider ID cannot be null");
        this.requesterId = Objects.requireNonNull(requesterId, "Requester ID cannot be null");
        this.duration = Objects.requireNonNull(duration, "Session duration cannot be null");
        if (providerId.equals(requesterId)) {
            throw new IllegalArgumentException("Provider and requester must be distinct members");
        }
    }

    public Session(MemberId providerId, MemberId requesterId, SessionDuration duration) {
        this(new EngagementId(UUID.randomUUID().toString()), providerId, requesterId, duration);
    }

    public Session(EngagementId engagementId, MemberId providerId, MemberId requesterId, int durationMinutes) {
        this(engagementId, providerId, requesterId, new SessionDuration(durationMinutes));
    }

    public Session(EngagementId engagementId, MemberId providerId, MemberId requesterId, double durationHours) {
        this(engagementId, providerId, requesterId, new SessionDuration(durationHours));
    }

    public Session(MemberId providerId, MemberId requesterId, int durationMinutes) {
        this(new EngagementId(UUID.randomUUID().toString()), providerId, requesterId, durationMinutes);
    }

    public Session(MemberId providerId, MemberId requesterId, double durationHours) {
        this(new EngagementId(UUID.randomUUID().toString()), providerId, requesterId, durationHours);
    }

    public EngagementId getEngagementId() {
        return engagementId;
    }

    public MemberId getProviderId() {
        return providerId;
    }

    public MemberId getRequesterId() {
        return requesterId;
    }

    public SessionDuration getDuration() {
        return duration;
    }

    public int getDurationMinutes() {
        return duration.toMinutes();
    }

    public int getCredits() {
        return duration.toCredits();
    }

    public boolean isProviderConfirmed() {
        return providerConfirmed;
    }

    public boolean isRequesterConfirmed() {
        return requesterConfirmed;
    }

    public void confirmProvider() {
        providerConfirmed = true;
    }

    public void confirmRequester() {
        requesterConfirmed = true;
    }

    public void confirm(MemberId memberId) {
        Objects.requireNonNull(memberId, "Member ID cannot be null");
        if (memberId.equals(providerId)) {
            confirmProvider();
            return;
        }
        if (memberId.equals(requesterId)) {
            confirmRequester();
            return;
        }
        throw new IllegalArgumentException("Member is not part of this session");
    }

    public boolean isConfirmed() {
        return providerConfirmed && requesterConfirmed;
    }

    public void setAgreedStart(LocalDateTime agreedStart) {
        this.agreedStart = agreedStart;
    }

    public void setStart(LocalDateTime agreedStart) {
        this.agreedStart = agreedStart;
    }

    public LocalDateTime getAgreedStart() {
        return agreedStart;
    }

    public LocalDateTime getStart() {
        return agreedStart;
    }

    public void setStart(Instant agreedStart) {
        this.agreedStart = LocalDateTime.ofInstant(agreedStart, ZoneId.systemDefault());
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean canTransferCredits() {
        return isConfirmed() && !cancelled && !creditsTransferred;
    }

    public int transferCredits() {
        if (!canTransferCredits()) {
            throw new IllegalStateException("Session credits cannot be transferred until both parties confirm the session and it remains active.");
        }
        creditsTransferred = true;
        return duration.toCredits();
    }

    public boolean isCreditsTransferred() {
        return creditsTransferred;
    }
}
