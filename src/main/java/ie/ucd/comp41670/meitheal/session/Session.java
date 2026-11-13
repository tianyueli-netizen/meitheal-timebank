package ie.ucd.comp41670.meitheal.session;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.util.Objects;

public class Session {
    private final EngagementId engagementId;
    private final MemberId requesterId;
    private final MemberId providerId;
    private final SessionDuration duration;
    private boolean requesterConfirmed;
    private boolean providerConfirmed;
    private boolean cancelled;

    public Session(EngagementId engagementId, MemberId requesterId, MemberId providerId, SessionDuration duration) {
        this.engagementId = requireEngagementId(engagementId);
        this.requesterId = requireMemberId(requesterId, "requester");
        this.providerId = requireMemberId(providerId, "provider");
        if (Objects.equals(this.requesterId.value(), this.providerId.value())) {
            throw new IllegalArgumentException("Requester and provider must be different members");
        }
        this.duration = requireDuration(duration);
    }

    public Session(String engagementId, String requesterId, String providerId, SessionDuration duration) {
        this(new EngagementId(engagementId), new MemberId(requesterId), new MemberId(providerId), duration);
    }

    public Session(String engagementId, String requesterId, String providerId, int minutes) {
        this(new EngagementId(engagementId), new MemberId(requesterId), new MemberId(providerId), new SessionDuration(minutes));
    }

    public Session(EngagementId engagementId, String requesterId, String providerId, int minutes) {
        this(engagementId, new MemberId(requesterId), new MemberId(providerId), new SessionDuration(minutes));
    }

    public Session(EngagementId engagementId, MemberId requesterId, String providerId, SessionDuration duration) {
        this(engagementId, requesterId, new MemberId(providerId), duration);
    }

    private static EngagementId requireEngagementId(EngagementId engagementId) {
        return Objects.requireNonNull(engagementId, "Engagement ID cannot be null");
    }

    private static MemberId requireMemberId(MemberId memberId, String label) {
        if (memberId == null) {
            throw new IllegalArgumentException(label + " member ID cannot be null");
        }
        return memberId;
    }

    private static SessionDuration requireDuration(SessionDuration duration) {
        return Objects.requireNonNull(duration, "Session duration cannot be null");
    }

    public EngagementId getEngagementId() {
        return engagementId;
    }

    public MemberId getRequesterId() {
        return requesterId;
    }

    public String getRequesterIdValue() {
        return requesterId.value();
    }

    public MemberId getProviderId() {
        return providerId;
    }

    public String getProviderIdValue() {
        return providerId.value();
    }

    public SessionDuration getDuration() {
        return duration;
    }

    public int getMinutes() {
        return duration.minutes();
    }

    public int getCredits() {
        return duration.minutes();
    }

    public int getTimeCredits() {
        return getCredits();
    }

    public boolean isRequesterConfirmed() {
        return requesterConfirmed;
    }

    public boolean isProviderConfirmed() {
        return providerConfirmed;
    }

    public boolean isConfirmedBy(MemberId memberId) {
        if (memberId == null) {
            return false;
        }
        return isConfirmedBy(memberId.value());
    }

    public boolean isConfirmedBy(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return false;
        }
        if (requesterId.value().equals(memberId.trim())) {
            return requesterConfirmed;
        }
        if (providerId.value().equals(memberId.trim())) {
            return providerConfirmed;
        }
        return false;
    }

    public void confirm(String memberId) {
        String value = memberId == null ? "" : memberId.trim();
        if (requesterId.value().equals(value)) {
            requesterConfirmed = true;
            return;
        }
        if (providerId.value().equals(value)) {
            providerConfirmed = true;
            return;
        }
        throw new IllegalArgumentException("Member is not a participant in this session");
    }

    public void confirm(MemberId memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        confirm(memberId.value());
    }

    public boolean hasBothConfirmations() {
        return requesterConfirmed && providerConfirmed;
    }

    public boolean canTransferCredits() {
        return !cancelled && hasBothConfirmations();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void markCancelled() {
        this.cancelled = true;
    }
}
