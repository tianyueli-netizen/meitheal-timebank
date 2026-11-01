
package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;

import java.util.Objects;

public class Offer {

    private final String id;
    private final String providerId;
    private final SkillCategory category;
    private final String description;

    public Offer(
            String id,
            String providerId,
            SkillCategory category,
            String description) {

        this.id = Objects.requireNonNull(id);
        this.providerId = Objects.requireNonNull(providerId);
        this.category = Objects.requireNonNull(category);
        this.description = Objects.requireNonNull(description);
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
