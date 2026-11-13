package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public class EngagementServiceImpl {

    private final Set<SkillCategory> validCategories;

    public EngagementServiceImpl(Set<SkillCategory> validCategories) {
        this.validCategories = Set.copyOf(
                Objects.requireNonNull(validCategories)
        );
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
}
