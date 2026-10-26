package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.domain.MemberId;
import ie.ucd.comp41670.meitheal.domain.MemberStatus;

public class Member {
    private MemberId id;
    private String name;
    private String email;
    private int balance;
    private MemberStatus status;

    public Member(MemberId id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.balance = 0;
        this.status = MemberStatus.ACTIVE;
    }

    public MemberId getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; } // 致命：允许外部随意绕过规则改余额
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
}