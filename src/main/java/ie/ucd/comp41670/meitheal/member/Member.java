package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.MemberStatus;
import java.util.Objects;

public class Member {
    private static final int INITIAL_WELCOME_GRANT = 120; // R3 强制数字

    private final MemberId id;
    private final String name;
    private final String email;
    private int balance;
    private MemberStatus status;

    public Member(MemberId id, String name, String email) {
        this.id = Objects.requireNonNull(id, "MemberId cannot be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email required");
        }
        this.name = name.trim();
        this.email = email.trim();
        this.balance = INITIAL_WELCOME_GRANT; // 锁死 R3 规则
        this.status = MemberStatus.ACTIVE;
    }

    public MemberId getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getBalance() { return balance; }
    public MemberStatus getStatus() { return status; }

    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    public void reinstate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void credit(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Credit amount cannot be negative");
        }
        this.balance += amount;
    }

    public void debit(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Debit amount cannot be negative");
        }
        this.balance -= amount;
    }
}