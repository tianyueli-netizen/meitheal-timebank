
package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;

import java.util.Set;

public class EngagementServiceImpl {

    private final Set<SkillCategory> validCategories;

    public EngagementServiceImpl(Set<SkillCategory> validCategories) {
        this.validCategories = Set.copyOf(validCategories);
    }

    public void validateSelfFulfilment(
            Request request,
            String providerId) {

        if (request.getRequesterId().equals(providerId)) {
            throw new IllegalArgumentException(
                    "Members cannot fulfil their own requests."
            );
        }
    }
    public void validateSkillCategory(SkillCategory category) {

        if (!validCategories.contains(category)) {
            throw new IllegalArgumentException(
                    "Invalid skill category: " + category.name()
            );
        }
    }
}
