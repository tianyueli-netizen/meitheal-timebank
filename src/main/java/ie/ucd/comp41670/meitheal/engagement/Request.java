
package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;
import ie.ucd.comp41670.meitheal.domain.SkillCategory;

import java.util.Objects;

public class Request {

    private final String id;
    private final String requesterId;
    private final SkillCategory category;
    private final String description;
    private final FulfilmentWindow fulfilmentWindow;

    public Request(
            String id,
            String requesterId,
            SkillCategory category,
            String description,
            FulfilmentWindow fulfilmentWindow) {

        this.id = Objects.requireNonNull(id);
        this.requesterId = Objects.requireNonNull(requesterId);
        this.category = Objects.requireNonNull(category);
        this.description = Objects.requireNonNull(description);
        this.fulfilmentWindow =
                Objects.requireNonNull(fulfilmentWindow);
    }

    public String getId() {
        return id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public FulfilmentWindow getFulfilmentWindow() {
        return fulfilmentWindow;
    }
}
