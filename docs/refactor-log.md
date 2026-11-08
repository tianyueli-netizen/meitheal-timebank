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
