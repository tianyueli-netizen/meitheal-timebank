# C-I-E Policy Prompt

## Context
This project uses Java 21, Maven, and JUnit 5, and runs entirely in-memory.

My responsibilities include:
- R2: A member's balance must not fall below -180.
- R10: The requester must pay an additional 5% community tax (rounded down).
- Arbitration of R2/R10 conflicts, handling business exceptions, invariant auditing, and adversarial testing.

Please review the existing code and relevant tests before proposing any changes.

## Intent
Implement the policies incrementally within the current architecture, ensuring backward compatibility with already merged behaviors.

Fixed Rules:
- If the labor cost itself would cause a balance to drop below -180: Maintain the existing rejection outcome.
- If the labor cost can be accommodated: The provider should receive the full labor points.
- The community tax must not cause the requester's balance to fall below -180; the tax may be partially or fully waived if necessary.
- Use `int` for point storage and public parameters (do not use `double` or `float`).
- Use `long` for intermediate calculations to prevent `int` overflow.
- Policies are strictly for calculation or validation; they must not modify members, funds, or the ledger.

## Expectation
Complete only one specific step per round, as directed by me.

1. Begin by outlining your current responsibilities, the files involved in the plan, and any potential compatibility risks.
2. Do not create or modify any files until an implementation is explicitly requested; focus on analysis only.
3. Shared areas—such as `domain`, `contract`, and `app`—are to be treated as read-only for this round.
4. After implementation, explain the rationale for the changes, provide the commands for verification, and note any scenarios not yet covered.
5. Do not perform automatic Git commits, and do not fabricate test results, defects, or records of AI usage.