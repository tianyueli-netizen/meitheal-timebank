package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;
import ie.ucd.comp41670.meitheal.domain.SkillCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EngagementTest {

    private Engagement engagement;

    @BeforeEach
    void setUp() {
        SkillCategory category = new SkillCategory("GARDENING");

        Request request = new Request(
                "REQ001",
                "member1",
                category,
                "Help with gardening",
                new FulfilmentWindow(
                        Instant.parse("2026-12-01T10:00:00Z"),
                        Instant.parse("2026-12-01T12:00:00Z")
                )
        );

        Offer offer = new Offer(
                "OFF001",
                "member2",
                category,
                "Gardening service"
        );

        engagement = new Engagement("ENG001", request, offer);
    }

    @Test
    @DisplayName("New engagement should not be confirmed")
    void shouldInitiallyBeUnconfirmed() {
        assertFalse(engagement.isConfirmed());
    }

    @Test
    @DisplayName("Requester confirmation alone is insufficient")
    void shouldRemainUnconfirmedAfterRequesterConfirmation() {
        engagement.confirmByRequester();

        assertFalse(engagement.isConfirmed());
    }

    @Test
    @DisplayName("Provider confirmation alone is insufficient")
    void shouldRemainUnconfirmedAfterProviderConfirmation() {
        engagement.confirmByProvider();

        assertFalse(engagement.isConfirmed());
    }

    @Test
    @DisplayName("Both confirmations should confirm engagement")
    void shouldBeConfirmedAfterBothPartiesConfirm() {
        engagement.confirmByRequester();
        engagement.confirmByProvider();

        assertTrue(engagement.isConfirmed());
    }

    @Test
    @DisplayName("Confirmation should work in either order")
    void shouldSupportProviderConfirmationFirst() {
        engagement.confirmByProvider();
        engagement.confirmByRequester();

        assertTrue(engagement.isConfirmed());
    }
}
