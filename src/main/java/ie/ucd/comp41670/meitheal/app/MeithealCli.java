package ie.ucd.comp41670.meitheal.app;

import ie.ucd.comp41670.meitheal.contract.CommunityFundService;
import ie.ucd.comp41670.meitheal.contract.Ledger;
import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.*;
import ie.ucd.comp41670.meitheal.engagement.Engagement;
import ie.ucd.comp41670.meitheal.engagement.EngagementServiceImpl;
import ie.ucd.comp41670.meitheal.engagement.Offer;
import ie.ucd.comp41670.meitheal.engagement.Request;
import ie.ucd.comp41670.meitheal.ledger.AppendOnlyLedger;
import ie.ucd.comp41670.meitheal.ledger.LedgerReport;
import ie.ucd.comp41670.meitheal.member.CommunityFundServiceImpl;
import ie.ucd.comp41670.meitheal.member.InMemoryMemberRepository;
import ie.ucd.comp41670.meitheal.member.Member;
import ie.ucd.comp41670.meitheal.policy.InvariantAuditor;
import ie.ucd.comp41670.meitheal.policy.TensionArbiter;
import ie.ucd.comp41670.meitheal.session.CancellationManager;
import ie.ucd.comp41670.meitheal.session.EngagementId;
import ie.ucd.comp41670.meitheal.session.Session;
import ie.ucd.comp41670.meitheal.session.SessionDuration;
import ie.ucd.comp41670.meitheal.session.SessionServiceImpl;

import java.io.PrintStream;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** Console adapter; the nested application coordinates the existing modules. */
public class MeithealCli {
    private final Scanner scanner;
    private final PrintStream out;
    private final Application application;

    public MeithealCli(EngagementServiceImpl engagementService) {
        this(engagementService, new Scanner(System.in), System.out, Clock.systemUTC());
    }

    public MeithealCli(EngagementServiceImpl engagementService, Scanner scanner,
                       PrintStream out, Clock clock) {
        this.scanner = Objects.requireNonNull(scanner);
        this.out = Objects.requireNonNull(out);
        this.application = new Application(engagementService, clock);
    }

    public void start() {
        out.println("================================");
        out.println(" Welcome to Meitheal Timebank");
        out.println("================================");
        out.println("Register members with option 8 before publishing requests or offers.");
        boolean running = true;
        while (running) {
            printMenu();
            if (!scanner.hasNextLine()) break;
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> createRequest();
                    case "2" -> viewRequests();
                    case "3" -> viewOffers();
                    case "4" -> acceptOffer();
                    case "5" -> createOffer();
                    case "6" -> confirmSession();
                    case "7" -> viewMyEngagements();
                    case "8" -> registerMember();
                    case "9" -> changeStatus();
                    case "10" -> cancelEngagement();
                    case "11" -> applyForGrant();
                    case "12" -> viewBalances();
                    case "13" -> viewLedger();
                    case "14" -> report();
                    case "15" -> confirmAgreement();
                    case "0" -> {
                        running = false;
                        out.println("Goodbye!");
                    }
                    default -> out.println("Invalid option. Please try again.");
                }
            } catch (EndOfInput end) {
                out.println("Goodbye!");
                running = false;
            } catch (IllegalArgumentException | IllegalStateException
                     | DateTimeException | ArithmeticException exception) {
                out.println("Error: " + exception.getMessage());
            }
        }
    }

    private void printMenu() {
        out.println("\nPlease select an option:");
        out.println("1. Create a Request");
        out.println("2. View Requests");
        out.println("3. View Offers");
        out.println("4. Accept an Offer / Propose an Engagement");
        out.println("5. Create an Offer");
        out.println("6. Confirm a Completed Session and Settle");
        out.println("7. View My Engagements");
        out.println("8. Register a Member");
        out.println("9. Suspend / Reinstate a Member");
        out.println("10. Cancel an Engagement");
        out.println("11. Apply for a Community Grant");
        out.println("12. Query Balances (member ID or ALL)");
        out.println("13. Query Ledger (member ID or ALL)");
        out.println("14. System Report and Invariant Audit");
        out.println("15. Accept an Engagement Agreement");
        out.println("0. Exit");
        out.print("Your choice: ");
    }

    private void registerMember() {
        String name = readRequired("Member name: ");
        String email = readRequired("Member email: ");
        Member member = application.register(name, email);
        out.println("Member registered: " + member.getId().value());
        printMember(member);
        printAudit();
    }

    private void changeStatus() {
        String id = readRequired("Member ID: ");
        String action = readRequired("Action (SUSPEND or REINSTATE): ");
        printMember(application.changeStatus(id, action));
        printAudit();
    }

    private void createRequest() {
        out.println("\n--- Create Request ---");
        String id = readRequired("Request ID: ");
        String memberId = readRequired("Requester ID: ");
        SkillCategory category = new SkillCategory(readRequired("Skill Category: "));
        String description = readRequired("Description: ");
        Instant earliest = readInstant("Earliest time (e.g. 2026-12-01T09:00:00Z): ");
        Instant latest = readInstant("Latest time (e.g. 2026-12-01T17:00:00Z): ");
        application.createRequest(new Request(id, memberId, category, description,
                new FulfilmentWindow(earliest, latest)));
        out.println("Request created successfully!");
    }

    private void createOffer() {
        out.println("\n--- Create Offer ---");
        String id = readRequired("Offer ID: ");
        String memberId = readRequired("Provider ID: ");
        SkillCategory category = new SkillCategory(readRequired("Skill Category: "));
        String description = readRequired("Description: ");
        application.createOffer(new Offer(id, memberId, category, description));
        out.println("Offer created successfully!");
    }

    private void viewRequests() {
        out.println("\n--- Available Requests ---");
        List<Request> list = application.engagements.getRequests();
        if (list.isEmpty()) out.println("No requests available.");
        for (Request request : list) {
            out.println("Request ID: " + request.getId());
            out.println("Requester ID: " + request.getRequesterId());
            out.println("Category: " + request.getCategory().name());
            out.println("Description: " + request.getDescription());
            out.println("Earliest: " + request.getFulfilmentWindow().earliest());
            out.println("Latest: " + request.getFulfilmentWindow().latest());
        }
    }

    private void viewOffers() {
        out.println("\n--- Available Offers ---");
        List<Offer> list = application.engagements.getOffers();
        if (list.isEmpty()) out.println("No offers available.");
        for (Offer offer : list) {
            out.println("Offer ID: " + offer.getId());
            out.println("Provider ID: " + offer.getProviderId());
            out.println("Category: " + offer.getCategory().name());
            out.println("Description: " + offer.getDescription());
        }
    }

    private void acceptOffer() {
        out.println("\n--- Accept an Offer ---");
        if (application.engagements.getRequests().isEmpty()) {
            out.println("No requests available.");
            return;
        }
        if (application.engagements.getOffers().isEmpty()) {
            out.println("No offers available.");
            return;
        }
        viewRequests();
        viewOffers();
        String requestId = readRequired("Request ID: ");
        String offerId = readRequired("Offer ID: ");
        Instant start = readInstant("Agreed start (ISO-8601 UTC): ");
        int minutes = readInt("Session duration in whole minutes (1-480): ");
        Engagement engagement = application.propose(requestId, offerId, start, minutes);
        out.println("Engagement created successfully!");
        out.println("Engagement ID: " + engagement.getId());
        out.println("Both parties accept the agreement with option 15.");
        out.println("After the work, each party confirms completion with option 6.");
    }

    private void confirmAgreement() {
        String id = readRequired("Engagement ID: ");
        String member = readRequired("Your Member ID: ");
        Engagement agreement = application.acceptAgreement(id, member);
        out.println(agreement.isConfirmed() ? "Agreement confirmed by both parties."
                : "Agreement acceptance recorded; waiting for the other party.");
    }

    private void confirmSession() {
        String id = readRequired("Engagement ID: ");
        String member = readRequired("Your Member ID: ");
        Confirmation result = application.confirmAndSettle(id, member);
        switch (result.status()) {
            case WAITING -> out.println("Completion confirmation recorded; waiting for the other party.");
            case REJECTED -> out.println("Settlement rejected: labor alone would breach the overdraft floor.");
            case ALREADY_SETTLED -> out.println("Already settled. No credits transferred again.");
            case SETTLED -> {
                out.println("Settlement approved.");
                out.println("Labor credits: " + result.settlement().laborCredits());
                out.println("Actual levy: " + result.settlement().actualLevyCredits());
            }
        }
        Engagement agreement = application.engagement(id);
        printMember(application.member(agreement.getRequest().getRequesterId()));
        printMember(application.member(agreement.getOffer().getProviderId()));
        out.println("Community fund balance: " + application.fund.getFundBalance());
        printAudit();
    }

    private void viewMyEngagements() {
        String memberId = readRequired("Your Member ID: ");
        application.member(memberId);
        out.println("\n--- My Engagements ---");
        List<Engagement> list = application.engagements.getEngagements().stream()
                .filter(e -> e.getRequest().getRequesterId().equals(memberId)
                        || e.getOffer().getProviderId().equals(memberId)).toList();
        if (list.isEmpty()) out.println("No engagements available.");
        for (Engagement agreement : list) {
            Session session = application.session(agreement.getId());
            out.println("Engagement ID: " + agreement.getId());
            out.println("Request ID: " + agreement.getRequest().getId());
            out.println("Offer ID: " + agreement.getOffer().getId());
            out.println("Agreed start: " + application.starts.get(agreement.getId()));
            out.println("Duration minutes: " + session.getDurationMinutes());
            out.println("Requester agreement accepted: " + agreement.isRequesterConfirmed());
            out.println("Provider agreement accepted: " + agreement.isProviderConfirmed());
            out.println("Requester completion confirmed: " + session.isRequesterConfirmed());
            out.println("Provider completion confirmed: " + session.isProviderConfirmed());
            out.println("Cancelled: " + session.isCancelled());
            out.println("Settled: " + session.isCreditsTransferred());
        }
    }

    private void cancelEngagement() {
        String id = readRequired("Engagement ID: ");
        String member = readRequired("Your Member ID: ");
        boolean late = application.cancel(id, member);
        out.println("Engagement cancelled. Credits transferred: 0.");
        out.println("Late cancellation: " + late);
        out.println("Late cancellation count: "
                + application.cancellations.getCancellationCount(new MemberId(member)));
        printAudit();
    }

    private void applyForGrant() {
        String id = readRequired("Recipient Member ID: ");
        int amount = readInt("Relief amount: ");
        application.grant(id, amount);
        out.println("Community grant approved for " + LocalDate.now(application.clock) + ".");
        printMember(application.member(id));
        out.println("Community fund balance: " + application.fund.getFundBalance());
        printAudit();
    }

    private void viewBalances() {
        String id = readRequired("Member ID or ALL: ");
        if (id.equalsIgnoreCase("ALL")) {
            for (Member member : application.members.findAll()) printMember(member);
        } else printMember(application.member(id));
        out.println("Community fund balance: " + application.fund.getFundBalance());
    }

    private void viewLedger() {
        String id = readRequired("Member ID or ALL: ");
        LedgerReport report = new LedgerReport(application.ledger);
        if (id.equalsIgnoreCase("ALL")) out.print(report.generateFullReport());
        else out.print(report.generateMemberReport(application.member(id).getId()));
    }

    private void report() {
        long total = application.members.findAll().stream().mapToLong(Member::getBalance).sum();
        out.println("Registered members: " + application.members.count());
        out.println("Total member balances: " + total);
        out.println("Community fund balance: " + application.fund.getFundBalance());
        out.println("Total credits in circulation: " + (total + application.fund.getFundBalance()));
        out.println("Authorised issued credits: " + application.authorisedIssuedCredits);
        out.println("Global ledger entries: " + application.ledger.getEntryCount());
        printAudit();
    }

    private void printMember(Member member) {
        out.println("Member ID: " + member.getId().value() + " | Name: " + member.getName()
                + " | Status: " + member.getStatus() + " | Balance: " + member.getBalance());
    }

    private void printAudit() {
        List<String> violations = application.audit();
        if (violations.isEmpty()) out.println("Invariant audit: OK");
        else violations.forEach(message -> out.println("Invariant violation: " + message));
    }

    private String readRequired(String prompt) {
        out.print(prompt);
        if (!scanner.hasNextLine()) throw new EndOfInput();
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) throw new IllegalArgumentException("Input cannot be empty.");
        return value;
    }

    private Instant readInstant(String prompt) { return Instant.parse(readRequired(prompt)); }
    private int readInt(String prompt) { return Integer.parseInt(readRequired(prompt)); }
    private static final class EndOfInput extends RuntimeException { }
    private enum ConfirmationStatus { WAITING, SETTLED, REJECTED, ALREADY_SETTLED }
    private record Confirmation(ConfirmationStatus status, SettlementResult settlement) { }

    /**
     * Single-threaded integration stays in this file to respect the requested scope.
     * Business rejection and overflow are preflighted. Arbitrary dependency failures
     * cannot be rolled back by the current member, fund and ledger interfaces.
     */
    private static final class Application {
        private final EngagementServiceImpl engagements;
        private final MemberRepository members = new InMemoryMemberRepository();
        private final CommunityFundService fund = new CommunityFundServiceImpl(members);
        private final Ledger ledger = new AppendOnlyLedger();
        private final CancellationManager cancellations = new CancellationManager();
        private final SessionServiceImpl sessions = new SessionServiceImpl(cancellations);
        private final TensionArbiter arbiter = new TensionArbiter();
        private final InvariantAuditor auditor = new InvariantAuditor();
        private final Map<String, Session> sessionByEngagement = new LinkedHashMap<>();
        private final Map<String, Instant> starts = new LinkedHashMap<>();
        private final Clock clock;
        private int authorisedIssuedCredits;

        private Application(EngagementServiceImpl engagements, Clock clock) {
            this.engagements = Objects.requireNonNull(engagements);
            this.clock = Objects.requireNonNull(clock);
        }

        private Member member(String id) {
            return members.findById(new MemberId(id))
                    .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
        }

        private Member activeMember(String id) {
            Member member = member(id);
            member.assertActive();
            return member;
        }

        private Member register(String name, String email) {
            int issuedAfter = Math.addExact(authorisedIssuedCredits, 120);
            Member member = members.registerMember(name, email);
            ledger.append(new LedgerEntry("WELCOME-" + member.getId().value(), null,
                    member.getId(), 120, TransactionType.WELCOME_GRANT, clock.instant()));
            authorisedIssuedCredits = issuedAfter;
            return member;
        }

        private Member changeStatus(String id, String action) {
            Member member = member(id);
            if (action.equalsIgnoreCase("SUSPEND")) member.suspend();
            else if (action.equalsIgnoreCase("REINSTATE")) member.reinstate();
            else throw new IllegalArgumentException("Action must be SUSPEND or REINSTATE.");
            return member;
        }

        private void createRequest(Request request) {
            activeMember(request.getRequesterId());
            engagements.createRequest(request);
        }

        private void createOffer(Offer offer) {
            activeMember(offer.getProviderId());
            engagements.createOffer(offer);
        }

        private Engagement propose(String requestId, String offerId, Instant start, int minutes) {
            Request request = engagements.findRequestById(requestId);
            Offer offer = engagements.findOfferById(offerId);
            if (request == null) throw new IllegalArgumentException("Request not found.");
            if (offer == null) throw new IllegalArgumentException("Offer not found.");
            Member requester = activeMember(request.getRequesterId());
            Member provider = activeMember(offer.getProviderId());
            engagements.validateFulfilmentTime(request.getFulfilmentWindow(), start);
            SessionDuration duration = SessionDuration.ofMinutes(minutes);
            String id = UUID.randomUUID().toString();
            Session session = sessions.createSession(new EngagementId(id), provider.getId(),
                    requester.getId(), duration);
            session.setStart(start);
            Engagement agreement = engagements.createEngagement(id, request, offer);
            sessionByEngagement.put(id, session);
            starts.put(id, start);
            return agreement;
        }

        private Engagement engagement(String id) {
            Engagement agreement = engagements.findEngagementById(id);
            if (agreement == null) throw new IllegalArgumentException("Engagement not found.");
            return agreement;
        }

        private Session session(String id) {
            Session session = sessionByEngagement.get(id);
            if (session == null) throw new IllegalStateException("No recorded session for this engagement.");
            return session;
        }

        private Session participantSession(String id, String memberId) {
            member(memberId);
            Session session = session(id);
            MemberId participant = new MemberId(memberId);
            if (!participant.equals(session.getProviderId())
                    && !participant.equals(session.getRequesterId())) {
                throw new IllegalArgumentException("You are not a participant in this engagement.");
            }
            return session;
        }

        private Engagement acceptAgreement(String id, String memberId) {
            Session session = participantSession(id, memberId);
            if (session.isCancelled()) throw new IllegalStateException("Engagement is cancelled.");
            Engagement agreement = engagement(id);
            if (!agreement.isConfirmed()) {
                activeMember(agreement.getRequest().getRequesterId());
                activeMember(agreement.getOffer().getProviderId());
            }
            engagements.validateFulfilmentTime(agreement.getRequest().getFulfilmentWindow(), starts.get(id));
            if (agreement.getRequest().getRequesterId().equals(memberId)) agreement.confirmByRequester();
            else agreement.confirmByProvider();
            return agreement;
        }

        private Confirmation confirmAndSettle(String id, String memberId) {
            Session session = participantSession(id, memberId);
            if (session.isCancelled()) throw new IllegalStateException("Engagement is cancelled.");
            if (session.isCreditsTransferred()) {
                return new Confirmation(ConfirmationStatus.ALREADY_SETTLED, null);
            }
            Engagement agreement = engagement(id);
            if (!agreement.isConfirmed()) {
                throw new IllegalStateException("Both parties must accept the agreement with option 15 first.");
            }
            engagements.validateFulfilmentTime(agreement.getRequest().getFulfilmentWindow(), starts.get(id));
            // Existing agreements may pay earned credits after a member is suspended.
            sessions.confirm(session, new MemberId(memberId));
            if (!session.isConfirmed()) return new Confirmation(ConfirmationStatus.WAITING, null);
            Member requester = member(agreement.getRequest().getRequesterId());
            Member provider = member(agreement.getOffer().getProviderId());
            SettlementResult result = arbiter.calculateSettlement(requester.getBalance(),
                    TimeCredit.fromMinutes(session.getDurationMinutes()));
            if (!result.isApproved()) return new Confirmation(ConfirmationStatus.REJECTED, result);

            int debit = Math.toIntExact((long) result.laborCredits() + result.actualLevyCredits());
            Math.toIntExact((long) requester.getBalance() - debit);
            Math.toIntExact((long) provider.getBalance() + result.laborCredits());
            Math.toIntExact((long) fund.getFundBalance() + result.actualLevyCredits());
            LedgerEntry transfer = new LedgerEntry("TRANSFER-" + id, requester.getId(),
                    provider.getId(), result.laborCredits(), TransactionType.TRANSFER, clock.instant());
            List<LedgerEntry> fundBefore = fund.getFundLedgerEntries();
            if (result.actualLevyCredits() > 0) fund.depositLevy(result.actualLevyCredits());
            requester.debit(debit);
            provider.credit(result.laborCredits());
            ledger.append(transfer);
            appendNewFundEntries(fundBefore, requester.getId());
            sessions.transferCredits(session);
            return new Confirmation(ConfirmationStatus.SETTLED, result);
        }

        private boolean cancel(String id, String memberId) {
            Session session = participantSession(id, memberId);
            if (session.isCancelled()) throw new IllegalStateException("Engagement is already cancelled.");
            if (session.isCreditsTransferred()) throw new IllegalStateException("A settled session cannot be cancelled.");
            Engagement agreement = engagement(id);
            Instant now = clock.instant();
            Instant start = starts.get(id);
            // R14 concerns agreement acceptance, not the R4 completion flags.
            // Adapt these distinct states here rather than faking completion.
            boolean late = agreement.isConfirmed() && !now.isAfter(start)
                    && Duration.between(now, start).compareTo(Duration.ofHours(24)) < 0;
            session.cancel();
            if (late) cancellations.recordCancellation(new MemberId(memberId));
            return late;
        }

        private void grant(String id, int amount) {
            Member recipient = member(id);
            if (amount <= 0) throw new IllegalArgumentException("Relief amount must be positive.");
            Math.toIntExact((long) recipient.getBalance() + amount);
            List<LedgerEntry> fundBefore = fund.getFundLedgerEntries();
            // Do not expose arbitrary backdated grant dates through the CLI.
            fund.grantRelief(recipient.getId(), amount, LocalDate.now(clock));
            appendNewFundEntries(fundBefore, null);
        }

        private void appendNewFundEntries(List<LedgerEntry> before, MemberId payer) {
            List<LedgerEntry> after = fund.getFundLedgerEntries();
            for (int index = before.size(); index < after.size(); index++) {
                LedgerEntry entry = after.get(index);
                MemberId from = entry.type() == TransactionType.COMMUNITY_LEVY
                        ? Objects.requireNonNull(payer) : entry.fromMemberId();
                // Preserve source event IDs. The auxiliary log is not added to totals.
                ledger.append(new LedgerEntry(entry.entryId(), from, entry.toMemberId(),
                        entry.amount(), entry.type(), entry.timestamp()));
            }
        }

        private List<String> audit() {
            Map<MemberId, Integer> balances = members.findAll().stream()
                    .collect(Collectors.toMap(Member::getId, Member::getBalance));
            return auditor.audit(balances, fund.getFundBalance(), authorisedIssuedCredits);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide valid skill categories.");
            System.out.println("Example: GARDENING TRANSPORT COOKING");
            return;
        }
        Set<SkillCategory> categories = Arrays.stream(args)
                .map(SkillCategory::new).collect(Collectors.toSet());
        new MeithealCli(new EngagementServiceImpl(categories)).start();
    }
}
