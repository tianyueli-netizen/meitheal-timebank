
package ie.ucd.comp41670.meitheal.domain;

import java.util.Objects;

public record SkillCategory(String name) {

    public SkillCategory {
        Objects.requireNonNull(name, "name must not be null");

        name = name.trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "name must not be empty"
            );
        }
    }
}

