package ie.ucd.comp41670.meitheal.domain;

/**
 * Represents the types of transactions recorded in the ledger.
 * R3: Welcome grants and fund grants introduce credits.
 * R10: Community levy is credited to the community fund.
 * R11: The community fund grants credits to members.
 */
public enum TransactionType {
    WELCOME_GRANT,
    TRANSFER,
    COMMUNITY_LEVY,
    FUND_GRANT
}
