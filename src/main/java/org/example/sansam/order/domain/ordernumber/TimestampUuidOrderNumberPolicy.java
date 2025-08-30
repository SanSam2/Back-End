package org.example.sansam.order.domain.ordernumber;


import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class TimestampUuidOrderNumberPolicy implements OrderNumberPolicy {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Clock clock;
    private final Supplier<UUID> uuidSupplier;

    public TimestampUuidOrderNumberPolicy(Clock clock, Supplier<UUID> uuidSupplier) {
        this.clock = Objects.requireNonNull(clock, "clock");;
        this.uuidSupplier = Objects.requireNonNull(uuidSupplier, "uuidSupplier");
    }

    @Override
    public String makeOrderNumber() {
        String ts = LocalDateTime.now(clock).format(F);
        String suffix = uuidSupplier.get().toString();
        return ts + "-" + suffix;
    }
}