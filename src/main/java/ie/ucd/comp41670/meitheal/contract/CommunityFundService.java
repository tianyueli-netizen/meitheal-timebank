package ie.ucd.comp41670.meitheal.contract;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.time.LocalDate;

public interface CommunityFundService {
    void depositLevy(int amount);
    void grantRelief(MemberId recipientId, int amount, LocalDate grantDate);
    int getFundBalance();
}