
package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillCategoryTest {

    @Test
    void shouldCreateCategoryWithValidName() {
        SkillCategory category = new SkillCategory("Java");

        assertEquals("JAVA", category.name());
    }

    @Test
    void shouldRemoveLeadingAndTrailingSpaces() {
        SkillCategory category = new SkillCategory(" Java ");

        assertEquals("JAVA", category.name());
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(
                NullPointerException.class,
                () -> new SkillCategory(null)
        );
    }

    @Test
    void shouldRejectEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SkillCategory("")
        );
    }

    @Test
    void shouldRejectNameContainingOnlySpaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SkillCategory("   ")
        );
    }
}
