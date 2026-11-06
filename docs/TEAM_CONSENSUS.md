# Team Consensus & Architecture Specification

## 1. Tension Resolution: R2 (Overdraft Floor -180) vs R10 (5% Community Levy)
- **Conflict Scenario**: A requester at `-120` credits confirms a 1-hour (`60` credits) session. Labor deduction brings balance to `-180` (legal under R2). However, the 5% levy (`3` credits) would push the balance to `-183`, breaching R2.
- **Adopted Resolution (Two-Phase Settlement)**:
    1. **Phase 1 (Labor Transfer)**: Execute the `60`-credit transfer to protect the provider's earned labor rights under R4.
    2. **Phase 2 (Levy Cap/Waiver)**: Cap the actual levy at `max(0, (balance - laborCost) - (-180))`. In this edge case, `0` levy is charged.
- **Rejected Alternative & Cost**: Rejecting the entire transaction when `labor + levy < -180` was rejected because it punishes an innocent provider who already completed the work.

## 2. Rule Yielding No Invariant
- **Rule R12 (Read-only inspection & defensive copying)**: R12 governs API encapsulation and memory reference safety (`List.copyOf`), rather than a state assertion over domain data transitions.