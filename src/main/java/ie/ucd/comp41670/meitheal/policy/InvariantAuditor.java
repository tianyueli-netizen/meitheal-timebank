package ie.ucd.comp41670.meitheal.policy;

import ie.ucd.comp41670.meitheal.domain.MemberId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvariantAuditor {
    public List<String> audit(
            Map<MemberId, Integer> capturedBalances,
            int fundBalance,
            int authorisedIssuedCredits
    ) {
        Map<MemberId, Integer> balances = Map.copyOf(capturedBalances);
        if (authorisedIssuedCredits < 0) {
            throw new IllegalArgumentException("authorisedIssuedCredits cannot be negative");
        }

        List<String> messages = new ArrayList<>();

        long totalMemberBalances = 0L;
        for (Map.Entry<MemberId, Integer> entry : balances.entrySet()) {
            MemberId memberId = entry.getKey();
            int balance = entry.getValue();
            totalMemberBalances += balance;

            if (balance < StandardOverdraftPolicy.MINIMUM_BALANCE) {
                messages.add("R2: member " + memberId + " balance=" + balance + " below floor=" + StandardOverdraftPolicy.MINIMUM_BALANCE);
            }
        }

        long fund = fundBalance;
        if (fund < 0L) {
            messages.add("R11: fund balance=" + fund + " below floor=0");
        }

        long total = totalMemberBalances + fund;
        if (total != authorisedIssuedCredits) {
            messages.add("R3: member balances + fund=" + total + " does not equal authorised issued credits=" + authorisedIssuedCredits);
        }

        return List.copyOf(messages);
    }
}
