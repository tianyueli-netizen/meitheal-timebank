package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.CommunityFundService;
import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.TransactionType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommunityFundServiceImpl implements CommunityFundService {
    private final MemberRepository memberRepository;
    private int fundBalance = 0;
    private final Map<MemberId, YearMonth> lastGrantMonth = new ConcurrentHashMap<>();
    private final List<LedgerEntry> fundEntries = new ArrayList<>();

    public CommunityFundServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "Repository required");
    }

    @Override
    public synchronized void depositLevy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Levy deposit cannot be negative");
        }
        this.fundBalance += amount;
        fundEntries.add(new LedgerEntry(
                "LEVY-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                null,
                amount,
                TransactionType.COMMUNITY_LEVY,
                Instant.now()
        ));
    }

    @Override
    public synchronized void grantRelief(MemberId recipientId, int amount, LocalDate grantDate) {
        Objects.requireNonNull(recipientId, "Recipient ID required");
        Objects.requireNonNull(grantDate, "Grant date required");
        if (amount <= 0) {
            throw new IllegalArgumentException("Grant amount must be strictly positive");
        }

        if (this.fundBalance < amount) {
            throw new IllegalStateException("INV-11 Violation: Insufficient fund balance (" + fundBalance + ") for grant (" + amount + ")");
        }

        Member recipient = memberRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + recipientId.value()));

        if (recipient.getBalance() >= 0) {
            throw new IllegalStateException("R11 Violation: Member balance (" + recipient.getBalance() + ") is not below zero");
        }

        YearMonth currentMonth = YearMonth.from(grantDate);
        YearMonth previousMonth = lastGrantMonth.get(recipientId);
        if (previousMonth != null && previousMonth.equals(currentMonth)) {
            throw new IllegalStateException("INV-12 Violation: Member already received a grant in calendar month: " + currentMonth);
        }

        this.fundBalance -= amount;
        recipient.credit(amount);
        lastGrantMonth.put(recipientId, currentMonth);

        // ✅ 修复 1: 系统基金发放时 fromMemberId 为 null，toMemberId 为 recipientId
        fundEntries.add(new LedgerEntry(
                "GRANT-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                recipientId,
                amount,
                TransactionType.FUND_GRANT,
                Instant.now()
        ));
    }

    @Override
    public synchronized int getFundBalance() {
        return fundBalance;
    }

    @Override
    public synchronized List<LedgerEntry> getFundLedgerEntries() {
        return List.copyOf(fundEntries);
    }
}