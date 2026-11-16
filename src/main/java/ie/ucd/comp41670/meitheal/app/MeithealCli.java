
package ie.ucd.comp41670.meitheal.app;

import ie.ucd.comp41670.meitheal.domain.FulfilmentWindow;
import ie.ucd.comp41670.meitheal.domain.SkillCategory;
import ie.ucd.comp41670.meitheal.engagement.Engagement;
import ie.ucd.comp41670.meitheal.engagement.EngagementServiceImpl;
import ie.ucd.comp41670.meitheal.engagement.Request;
import ie.ucd.comp41670.meitheal.engagement.Offer;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MeithealCli {

    private final Scanner scanner;
    private final EngagementServiceImpl engagementService;

    public MeithealCli(EngagementServiceImpl engagementService) {
        this.scanner = new Scanner(System.in);
        this.engagementService = engagementService;
    }

    public void start() {

        boolean running = true;
        printWelcomeMessage();

        while (running && scanner.hasNextLine()) {

            System.out.println("\nPlease select an option:");
            System.out.println("1. Create a Request");
            System.out.println("2. View Requests");
            System.out.println("3. View Offers");
            System.out.println("4. Accept an Offer");
            System.out.println("5. Create an Offer");
            System.out.println("6. Confirm an Engagement");
            System.out.println("7. View Engagements");
            System.out.println("0. Exit");

            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        createRequest();
                        break;

                    case "2":
                        viewRequests();
                        break;

                    case "3":
                        viewOffers();
                        break;

                    case "4":
                        acceptOffer();
                        break;

                    case "5":
                        createOffer();
                        break;

                    case "6":
                        confirmEngagement();
                        break;

                    case "7":
                        viewEngagements();
                        break;

                    case "0":
                        running = false;
                        System.out.println("Goodbye!");
                        break;

                    default:
                        System.out.println(
                                "Invalid option. Please try again."
                        );
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());

            } catch (DateTimeParseException e) {
                System.out.println(
                        "Invalid date format. Please use ISO-8601 format."
                );
            }
        }
    }

    private void printWelcomeMessage() {
        System.out.println("================================");
        System.out.println(" Welcome to Meitheal Timebank");
        System.out.println("================================");
    }

    // 1. Create Request
    private void createRequest() {

        System.out.println("\n--- Create Request ---");

        String id = readRequired("Request ID: ");
        String requesterId = readRequired("Requester ID: ");
        String categoryName = readRequired("Skill Category: ");
        String description = readRequired("Description: ");

        System.out.println(
                "Enter fulfilment window using ISO-8601 format."
        );

        Instant earliest = Instant.parse(
                readRequired("Earliest time (e.g. 2027-01-01T09:00:00Z): ")
        );

        Instant latest = Instant.parse(
                readRequired("Latest time (e.g. 2027-01-10T18:00:00Z): ")
        );

        SkillCategory category = new SkillCategory(categoryName);

        FulfilmentWindow window =
                new FulfilmentWindow(earliest, latest);

        Request request = new Request(
                id,
                requesterId,
                category,
                description,
                window
        );

        engagementService.createRequest(request);

        System.out.println("Request created successfully!");
    }

    // 2. View Requests
    private void viewRequests() {

        System.out.println("\n--- Available Requests ---");

        List<Request> requests = engagementService.getRequests();

        if (requests.isEmpty()) {
            System.out.println("No requests available.");
            return;
        }

        for (Request request : requests) {

            System.out.println("----------------------------");
            System.out.println("Request ID: " + request.getId());
            System.out.println(
                    "Requester ID: " + request.getRequesterId()
            );
            System.out.println(
                    "Category: " + request.getCategory().name()
            );
            System.out.println(
                    "Description: " + request.getDescription()
            );
            System.out.println(
                    "Earliest: " +
                            request.getFulfilmentWindow().earliest()
            );
            System.out.println(
                    "Latest: " +
                            request.getFulfilmentWindow().latest()
            );
        }
    }

    // 3. View Offers
    private void viewOffers() {

        System.out.println("\n--- Available Offers ---");

        List<Offer> offers = engagementService.getOffers();

        if (offers.isEmpty()) {
            System.out.println("No offers available.");
            return;
        }

        for (Offer offer : offers) {

            System.out.println("----------------------------");
            System.out.println("Offer ID: " + offer.getId());
            System.out.println(
                    "Provider ID: " + offer.getProviderId()
            );
            System.out.println(
                    "Category: " + offer.getCategory().name()
            );
            System.out.println(
                    "Description: " + offer.getDescription()
            );
        }
    }

    // 4. Accept an Offer
    private void acceptOffer() {

        System.out.println("\n--- Accept an Offer ---");

        List<Request> requests = engagementService.getRequests();
        List<Offer> offers = engagementService.getOffers();

        if (requests.isEmpty()) {
            System.out.println("No requests available.");
            return;
        }

        if (offers.isEmpty()) {
            System.out.println("No offers available.");
            return;
        }

        viewRequests();

        String requestId = readRequired("\nEnter Request ID: ");

        Request request =
                engagementService.findRequestById(requestId);

        if (request == null) {
            System.out.println("Request not found.");
            return;
        }

        viewOffers();

        String offerId = readRequired("\nEnter Offer ID: ");

        Offer offer =
                engagementService.findOfferById(offerId);

        if (offer == null) {
            System.out.println("Offer not found.");
            return;
        }

        String engagementId = UUID.randomUUID().toString();

        Engagement engagement =
                engagementService.createEngagement(
                        engagementId,
                        request,
                        offer
                );

        System.out.println(
                "Engagement created successfully!"
        );

        System.out.println(
                "Engagement ID: " + engagement.getId()
        );

        System.out.println(
                "Waiting for both parties to confirm."
        );
    }

    // 5. Create Offer
    private void createOffer() {

        System.out.println("\n--- Create Offer ---");

        String id = readRequired("Offer ID: ");
        String providerId = readRequired("Provider ID: ");
        String categoryName = readRequired("Skill Category: ");
        String description = readRequired("Description: ");

        SkillCategory category = new SkillCategory(categoryName);

        Offer offer = new Offer(
                id,
                providerId,
                category,
                description
        );

        engagementService.createOffer(offer);

        System.out.println("Offer created successfully!");
    }

    // 6. Confirm an Engagement
    private void confirmEngagement() {

        System.out.println("\n--- Confirm an Engagement ---");

        List<Engagement> engagements =
                engagementService.getEngagements();

        if (engagements.isEmpty()) {
            System.out.println("No engagements available.");
            return;
        }

        viewEngagements();

        String engagementId =
                readRequired("\nEnter Engagement ID: ");

        Engagement selected = null;

        for (Engagement engagement : engagements) {
            if (engagement.getId().equals(engagementId)) {
                selected = engagement;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Engagement not found.");
            return;
        }

        System.out.print("Enter your Member ID: ");
        String memberId = readRequired("");

        Request request = selected.getRequest();
        Offer offer = selected.getOffer();

        if (memberId.equals(request.getRequesterId())) {

            if (selected.isRequesterConfirmed()) {
                System.out.println(
                        "Requester has already confirmed."
                );
                return;
            }

            selected.confirmByRequester();

            System.out.println(
                    "Requester confirmation recorded."
            );

        } else if (memberId.equals(offer.getProviderId())) {

            if (selected.isProviderConfirmed()) {
                System.out.println(
                        "Provider has already confirmed."
                );
                return;
            }

            selected.confirmByProvider();

            System.out.println(
                    "Provider confirmation recorded."
            );

        } else {

            System.out.println(
                    "You are not a participant in this Engagement."
            );
            return;
        }

        if (selected.isConfirmed()) {
            System.out.println(
                    "Both parties have confirmed the Engagement!"
            );
        } else {
            System.out.println(
                    "Waiting for the other party to confirm."
            );
        }
    }

    // 7. View Engagements
    private void viewEngagements() {

        System.out.println("\n--- Engagements ---");

        List<Engagement> engagements =
                engagementService.getEngagements();

        if (engagements.isEmpty()) {
            System.out.println("No engagements available.");
            return;
        }

        for (Engagement engagement : engagements) {

            System.out.println("----------------------------");

            System.out.println(
                    "Engagement ID: " + engagement.getId()
            );

            System.out.println(
                    "Request ID: " +
                            engagement.getRequest().getId()
            );

            System.out.println(
                    "Offer ID: " +
                            engagement.getOffer().getId()
            );

            System.out.println(
                    "Requester Confirmed: " +
                            engagement.isRequesterConfirmed()
            );

            System.out.println(
                    "Provider Confirmed: " +
                            engagement.isProviderConfirmed()
            );

            System.out.println(
                    "Fully Confirmed: " +
                            engagement.isConfirmed()
            );
        }
    }

    // Read non-empty input
    private String readRequired(String prompt) {

        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }

        String value = scanner.nextLine().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Input cannot be empty."
            );
        }

        return value;
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(
                    "Please provide valid skill categories."
            );

            System.out.println(
                    "Example: Gardening Transport Cooking"
            );

            return;
        }

        Set<SkillCategory> validCategories =
                Arrays.stream(args)
                        .map(SkillCategory::new)
                        .collect(Collectors.toSet());

        EngagementServiceImpl service =
                new EngagementServiceImpl(validCategories);

        MeithealCli cli = new MeithealCli(service);

        cli.start();
    }
}
