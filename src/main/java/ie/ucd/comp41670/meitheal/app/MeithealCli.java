
package ie.ucd.comp41670.meitheal.app;

import java.util.Scanner;

public class MeithealCli {

    private final Scanner scanner;

    public MeithealCli() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {

        boolean running = true;
        printWelcomeMessage();
        while (running) {

            printMenu();

            System.out.print("Your choice: ");

            String choice = scanner.nextLine();

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

                case "0":
                    running = false;
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    private void printWelcomeMessage() {
        System.out.println("================================");
        System.out.println(" Welcome to Meitheal Timebank");
        System.out.println("================================");
    }
    private void printMenu() {
        System.out.println("\nPlease select an option:");
        System.out.println("1. Create a Request");
        System.out.println("2. View Requests");
        System.out.println("3. View Offers");
        System.out.println("4. Accept an Offer");
        System.out.println("0. Exit");
    }

    private void createRequest() {
        System.out.println("Create Request selected.");
        // TODO: Connect to EngagementServiceImpl
    }

    private void viewRequests() {
        System.out.println("View Requests selected.");
        // TODO: Display available requests
    }

    private void viewOffers() {
        System.out.println("View Offers selected.");
        // TODO: Display available offers
    }

    private void acceptOffer() {
        System.out.println("Accept Offer selected.");
        // TODO: Call service to accept an offer
    }

    public static void main(String[] args) {
        MeithealCli cli = new MeithealCli();
        cli.start();
    }
}
