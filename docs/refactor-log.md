# Member hotspot refactor log

Planned safe sequence of transformations for `Member`:
1. Extract constructor validation into helper methods.
2. Extract amount-validation guards from `credit` and `debit`.
3. Extract the active-status guard into a helper and route `assertActive()` through it.

## Step 1: extract constructor validation
Generate: Extract `validateMemberId`, `validateName`, and `validateEmail` from the constructor while preserving the same exceptions and values.
Check: Run the characterization suite (`MemberTest`) to confirm constructor behavior remains identical and surrounding invariants are unchanged.
Decide: Accept. The refactoring is narrow, does not change semantics, and makes the constructor easier to read without affecting the active-state oddity.
Committed as: `refactor: extract constructor validation`

## Step 2: extract amount validation guards
Generate: Extract `validateCreditAmount` and `validateDebitAmount` from `credit` and `debit` while keeping the exact same rejection conditions and balance updates.
Check: Run the characterization suite again; it remains green and the same suspended-member oddity is still pinned.
Decide: Accept. There is no behavior change beyond removing repeated conditionals, and the surrounding invariants still hold.
Committed as: `refactor: extract amount validation guards`

## Step 3: extract active-state guard (riskiest transformation)
Fresh prompt used before acceptance:

Before version:
```java
public void assertActive() {
    if (this.status == MemberStatus.SUSPENDED) {
        throw new IllegalStateException("R6 Violation: Suspended member cannot perform active operations");
    }
}
```

Candidate bad after version:
```java
public void credit(int amount) {
    assertActive();
    if (amount < 0) throw new IllegalArgumentException("Credit amount cannot be negative");
    this.balance += amount;
}
```

Concrete inputs where they would differ: `Member member = new Member(...); member.suspend(); member.credit(25);`.
- Before: returns normally and balance becomes 145.
- Bad after: throws `IllegalStateException`.

Generate: Extract a `requireActive()` helper used by `assertActive()`, with no change to `credit` or `debit` behaviour.
Check: Run `MemberTest` after adding the adversarial inversion candidate assertion; the suite remains green because the pinned behavior is still accepted.
Decide: Accept the helper extraction only if `credit` and `debit` stay untouched. This is the highest-risk refactor because it sits on the `suspended-member` oddity, so the adversarial test is required before accepting it.
Committed as: `refactor: extract active-state guard`
