package ie.ucd.comp41670.meitheal.unit;

import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;
import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import ie.ucd.comp41670.meitheal.engagement.EngagementServiceImpl;
import ie.ucd.comp41670.meitheal.engagement.Offer;
import ie.ucd.comp41670.meitheal.engagement.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EngagementServiceTest {

    private EngagementServiceImpl engagementService;
    private SkillCategory tutoring;
    private SkillCategory gardening;
    private FulfilmentWindow window;

    @BeforeEach
    void setUp() {
        tutoring = new SkillCategory("Tutoring");
        gardening = new SkillCategory("Gardening");
        engagementService = new EngagementServiceImpl(Set.of(tutoring, gardening));
        window = new FulfilmentWindow(
                Instant.parse("2026-11-10T09:00:00Z"),
                Instant.parse("2026-11-10T17:00:00Z")
        );
    }

    @Test
    @DisplayName("R5 / INV-5: Member cannot fulfil their own request")
    void shouldRejectSelfFulfilment() {
        Request request = new Request("REQ-1", "MEM-001", tutoring, "Java help", window);

        assertThrows(IllegalArgumentException.class,
                () -> engagementService.validateSelfFulfilment(request, "MEM-001"));
        assertDoesNotThrow(() -> engagementService.validateSelfFulfilment(request, "MEM-002"));
    }

    @Test
    @DisplayName("R9 / INV-9: Fulfilment instant must fall inclusively within the request window")
    void shouldValidateFulfilmentWindowBoundaries() {
        Instant atEarliest = Instant.parse("2026-11-10T09:00:00Z");
        Instant inside = Instant.parse("2026-11-10T12:30:00Z");
        Instant atLatest = Instant.parse("2026-11-10T17:00:00Z");
        Instant beforeEarliest = Instant.parse("2026-11-10T08:59:59Z");
        Instant afterLatest = Instant.parse("2026-11-10T17:00:01Z");

        assertDoesNotThrow(() -> engagementService.validateFulfilmentTime(window, atEarliest));
        assertDoesNotThrow(() -> engagementService.validateFulfilmentTime(window, inside));
        assertDoesNotThrow(() -> engagementService.validateFulfilmentTime(window, atLatest));

        assertThrows(IllegalArgumentException.class,
                () -> engagementService.validateFulfilmentTime(window, beforeEarliest));
        assertThrows(IllegalArgumentException.class,
                () -> engagementService.validateFulfilmentTime(window, afterLatest));
    }

    @Test
    @DisplayName("R13: Only configured skill categories are accepted")
    void shouldValidateSkillCategoriesAgainstConfiguredSet() {
        SkillCategory unknown = new SkillCategory("Plumbing");
        Offer validOffer = new Offer("OFF-1", "MEM-002", gardening, "Lawn mowing");

        assertEquals("OFF-1", validOffer.getId());
        assertEquals("MEM-002", validOffer.getProviderId());
        assertEquals(gardening, validOffer.getCategory());
        assertEquals("Lawn mowing", validOffer.getDescription());

        assertDoesNotThrow(() -> engagementService.validateSkillCategory(tutoring));
        assertThrows(IllegalArgumentException.class,
                () -> engagementService.validateSkillCategory(unknown));
        assertThrows(NullPointerException.class,
                () -> engagementService.validateSkillCategory(null));
    }
}