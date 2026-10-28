package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.CommunityFundService;
import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CommunityFundServiceImpl implements CommunityFundService {
    private final MemberRepository memberRepository;
    private int fundBalance = 0;

    // 核心修复：采用 YearMonth 映射，精确锚定自然日历月 (INV-12)
    private final Map<MemberId, YearMonth> lastGrantMonth = new ConcurrentHashMap<>();

    public CommunityFundServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "Repository required");
    }

    @Override
    public synchronized void depositLevy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Levy deposit cannot be negative");
        }
        this.fundBalance += amount;
    }

    @Override
    public synchronized void grantRelief(MemberId recipientId, int amount, LocalDate grantDate) {
        Objects.requireNonNull(recipientId, "Recipient ID required");
        Objects.requireNonNull(grantDate, "Grant date required");
        if (amount <= 0) {
            throw new IllegalArgumentException("Grant amount must be strictly positive");
        }

        // 1. 守卫 INV-11：基金不能透支自身没有的积分 (The fund cannot grant credits it does not hold)
        if (this.fundBalance < amount) {
            throw new IllegalStateException("INV-11 Violation: Insufficient fund balance (" + fundBalance + ") for grant (" + amount + ")");
        }

        Member recipient = memberRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + recipientId.value()));

        // 2. 守卫 R11：只有余额严格低于 0 的会员有资格申请
        if (recipient.getBalance() >= 0) {
            throw new IllegalStateException("R11 Violation: Member balance (" + recipient.getBalance() + ") is not below zero");
        }

        // 3. 守卫 INV-12：每个自然日历月至多一次救济 (At most one grant per calendar month)
        YearMonth currentMonth = YearMonth.from(grantDate);
        YearMonth previousMonth = lastGrantMonth.get(recipientId);
        if (previousMonth != null && previousMonth.equals(currentMonth)) {
            throw new IllegalStateException("INV-12 Violation: Member already received a grant in calendar month: " + currentMonth);
        }

        // 状态变更与资产划转
        this.fundBalance -= amount;
        recipient.credit(amount);
        lastGrantMonth.put(recipientId, currentMonth);
    }

    @Override
    public synchronized int getFundBalance() {
        return fundBalance;
    }
}