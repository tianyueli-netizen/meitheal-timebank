# C-I-E Prompting Policy for Meitheal Time-Bank

- **Context (C)**: Java 21 domain model for COMP41670 Meitheal Time-Bank, strictly in-memory without frameworks.
- **Intent (I)**: Enforce exact specification constants (`-180` overdraft floor, `120` welcome grant, `60` credits/hour, `5%` floor levy, `8h` max session, `24h` cancellation window).
- **Expectation (E)**: Use `int` for all credit calculations (never `double`/`float`), Java 21 `record` with compact constructors for value objects, and `List.copyOf()` for R12 collection encapsulation.