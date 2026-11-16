package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EngagementServiceImpl {

    private final Set<SkillCategory> validCategories;
    private final List<Engagement> engagements = new ArrayList<>();
    private final List<Request> requests = new ArrayList<>();
    private final List<Offer> offers = new ArrayList<>();

    public EngagementServiceImpl(Set<SkillCategory> validCategories) {
        this.validCategories = Set.copyOf(
                Objects.requireNonNull(validCategories)
        );
    }
    public Engagement createEngagement(
            String engagementId,
            Request request,
            Offer offer) {

        Objects.requireNonNull(request);
        Objects.requireNonNull(offer);

        validateSelfFulfilment(
                request,
                offer.getProviderId()
        );

        validateSkillCategory(request.getCategory());
        validateSkillCategory(offer.getCategory());

        if (!request.getCategory().equals(offer.getCategory())) {
            throw new IllegalArgumentException(
                    "Request and Offer categories must match"
            );
        }

        Engagement engagement =
                new Engagement(engagementId, request, offer);

        engagements.add(engagement);

        return engagement;
    }

    // R5: Prevent self-fulfilment
    public void validateSelfFulfilment(
            Request request,
            String providerId) {

        Objects.requireNonNull(request);
        Objects.requireNonNull(providerId);

        if (request.getRequesterId().equals(providerId)) {
            throw new IllegalArgumentException(
                    "Members cannot fulfil their own requests."
            );
        }
    }

    // R9: Validate fulfilment time
    public void validateFulfilmentTime(
            FulfilmentWindow window,
            Instant instant) {

        Objects.requireNonNull(window);

        if (!window.contains(instant)) {
            throw new IllegalArgumentException(
                    "instant must be within the fulfilment window"
            );
        }
    }

    // R13: Validate skill category
    public void validateSkillCategory(SkillCategory category) {

        Objects.requireNonNull(category);

        if (!validCategories.contains(category)) {
            throw new IllegalArgumentException(
                    "Invalid skill category: " + category.name()
            );
        }
    }
    public void createRequest(Request request) {
        Objects.requireNonNull(request);

        validateSkillCategory(request.getCategory());

        if (findRequestById(request.getId()) != null) {
            throw new IllegalArgumentException("Duplicate Request ID");
        }

        requests.add(request);
    }

    public void createOffer(Offer offer) {
        Objects.requireNonNull(offer);

        validateSkillCategory(offer.getCategory());

        if (findOfferById(offer.getId()) != null) {
            throw new IllegalArgumentException("Duplicate Offer ID");
        }

        offers.add(offer);
    }

    public List<Request> getRequests() {
        return List.copyOf(requests);
    }

    public List<Offer> getOffers() {
        return List.copyOf(offers);
    }

    public Request findRequestById(String id) {
        return requests.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Offer findOfferById(String id) {
        return offers.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    // Find an engagement by ID
    public Engagement findEngagementById(String id) {
        Objects.requireNonNull(id, "id must not be null");

        return engagements.stream()
                .filter(engagement -> engagement.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Get all engagements
    public List<Engagement> getEngagements() {
        return List.copyOf(engagements);
    }
}
