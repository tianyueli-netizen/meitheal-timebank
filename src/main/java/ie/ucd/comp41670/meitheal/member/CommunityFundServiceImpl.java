package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.CommunityFundService;
import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommunityFundServiceImpl implements CommunityFundService {
    private final MemberRepository memberRepository;
    private int fundBalance = 0;
    // ❌ AI 缺陷：按天记录，后面用 30 天滑动窗口判断
    private final Map<MemberId, LocalDate> grantHistory = new ConcurrentHashMap<>();

    public CommunityFundServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void depositLevy(int amount) {
        this.fundBalance += amount;
    }

    @Override
    public void grantRelief(MemberId recipientId, int amount, LocalDate grantDate) {
        Member recipient = memberRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (recipient.getBalance() <= 0) { // 错误：R11 规定必须是严格低于0 (below zero)
            LocalDate lastGrant = grantHistory.get(recipientId);
            if (lastGrant != null && ChronoUnit.DAYS.between(lastGrant, grantDate) < 30) {
                throw new IllegalStateException("Cannot receive grant within 30 days"); // 错误：违反 calendar month
            }
            // 错误：没有前置检查 fundBalance >= amount，直接扣减可能导致基金变负 (破坏 INV-11)
            this.fundBalance -= amount;
            recipient.credit(amount);
            grantHistory.put(recipientId, grantDate);
        }
    }

    @Override
    public int getFundBalance() {
        return fundBalance;
    }
}