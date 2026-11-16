package ie.ucd.comp41670.meitheal.contract;

import ie.ucd.comp41670.meitheal.domain.LedgerEntry;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.time.LocalDate;
import java.util.List;

public interface CommunityFundService {
    void depositLevy(int amount);
    void grantRelief(MemberId recipientId, int amount, LocalDate grantDate);
    int getFundBalance();
    List<LedgerEntry> getFundLedgerEntries();
}