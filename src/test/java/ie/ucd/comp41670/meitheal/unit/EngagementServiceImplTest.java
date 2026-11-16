package ie.ucd.comp41670.meitheal.engagement;

import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;
import ie.ucd.comp41670.meitheal.domain.SkillCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EngagementServiceImplTest {

    private EngagementServiceImpl service;
    private SkillCategory gardening;
    private FulfilmentWindow window;

    @BeforeEach
    void setUp() {
        gardening = new SkillCategory("GARDENING");

        service = new EngagementServiceImpl(
                Set.of(gardening, new SkillCategory("COOKING"))
        );

        window = new FulfilmentWindow(
                Instant.parse("2026-12-01T10:00:00Z"),
                Instant.parse("2026-12-01T12:00:00Z")
        );
    }

    private Request createRequest() {
        return new Request(
                "REQ001",
                "member1",
                gardening,
                "Help with gardening",
                window
        );
    }

    private Offer createOffer() {
        return new Offer(
                "OFF001",
                "member2",
                gardening,
                "Gardening service"
        );
    }

    @Test
    @DisplayName("Should create and retrieve a request")
    void shouldCreateAndRetrieveRequest() {
        Request request = createRequest();

        service.createRequest(request);

        assertEquals(1, service.getRequests().size());
        assertEquals(request, service.findRequestById("REQ001"));
    }

    @Test
    @DisplayName("Should reject duplicate request IDs")
    void shouldRejectDuplicateRequestId() {
        service.createRequest(createRequest());

        Request duplicate = createRequest();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createRequest(duplicate)
        );
    }

    @Test
    @DisplayName("Should create and retrieve an offer")
    void shouldCreateAndRetrieveOffer() {
        Offer offer = createOffer();

        service.createOffer(offer);

        assertEquals(1, service.getOffers().size());
        assertEquals(offer, service.findOfferById("OFF001"));
    }

    @Test
    @DisplayName("Should reject duplicate offer IDs")
    void shouldRejectDuplicateOfferId() {
        service.createOffer(createOffer());

        Offer duplicate = createOffer();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createOffer(duplicate)
        );
    }

    @Test
    @DisplayName("Should create an engagement successfully")
    void shouldCreateEngagement() {
        Engagement engagement = service.createEngagement(
                "ENG001",
                createRequest(),
                createOffer()
        );

        assertNotNull(engagement);
        assertEquals(1, service.getEngagements().size());
        assertEquals(
                engagement,
                service.findEngagementById("ENG001")
        );
    }

    @Test
    @DisplayName("R5: Should reject self-fulfilment")
    void shouldRejectSelfFulfilment() {
        Request request = createRequest();

        Offer offer = new Offer(
                "OFF002",
                "member1",
                gardening,
                "My own offer"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createEngagement("ENG002", request, offer)
        );
    }

    @Test
    @DisplayName("R9: Should accept fulfilment time within window")
    void shouldAcceptTimeWithinWindow() {
        assertDoesNotThrow(() ->
                service.validateFulfilmentTime(
                        window,
                        Instant.parse("2026-12-01T11:00:00Z")
                )
        );
    }

    @Test
    @DisplayName("R9: Should reject fulfilment time outside window")
    void shouldRejectTimeOutsideWindow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateFulfilmentTime(
                        window,
                        Instant.parse("2026-12-01T13:00:00Z")
                )
        );
    }

    @Test
    @DisplayName("R13: Should accept valid skill category")
    void shouldAcceptValidCategory() {
        assertDoesNotThrow(() ->
                service.validateSkillCategory(gardening)
        );
    }

    @Test
    @DisplayName("R13: Should reject invalid skill category")
    void shouldRejectInvalidCategory() {
        SkillCategory invalid = new SkillCategory("SWIMMING");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateSkillCategory(invalid)
        );
    }

    @Test
    @DisplayName("Should reject mismatched request and offer categories")
    void shouldRejectMismatchedCategories() {
        Offer cookingOffer = new Offer(
                "OFF003",
                "member2",
                new SkillCategory("COOKING"),
                "Cooking service"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createEngagement(
                        "ENG003",
                        createRequest(),
                        cookingOffer
                )
        );
    }
}
