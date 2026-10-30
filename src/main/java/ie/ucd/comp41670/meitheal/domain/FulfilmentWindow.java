package ie.ucd.comp41670.meitheal.domain;

import java.time.Instant;
import java.util.Objects;

public record FulfilmentWindow(Instant earliest, Instant latest) {

    public FulfilmentWindow {
        Objects.requireNonNull(earliest, "earliest must not be null");
        Objects.requireNonNull(latest, "latest must not be null");

        if (earliest.isAfter(latest)) {
            throw new IllegalArgumentException(
                    "earliest must not be after latest"
            );
        }
    }
}
