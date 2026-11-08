# Debt register

## Member hotspot oddity

`ie.ucd.comp41670.meitheal.member.Member` currently allows `credit()` and `debit()` to operate even after `suspend()`. The only guard is `assertActive()`, which throws only when `status == MemberStatus.SUSPENDED`; it does not prevent balance changes on suspended members. This is a real behavioural quirk of the current implementation, not a spec-driven rule, so it is pinned here without changing production behaviour and should be reconsidered during a deliberate policy refactor.
