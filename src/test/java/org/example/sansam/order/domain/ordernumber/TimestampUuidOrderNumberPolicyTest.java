package org.example.sansam.order.domain.ordernumber;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TimestampUuidOrderNumberPolicyTest {

    @Test
    void makeOrderNumber요청을_통해_OrderNumber를_생성할_수_있다() {
        //given
        Clock fixed = Clock.fixed(Instant.parse("2025-08-18T22:12:34Z"),
                ZoneId.of("Asia/Seoul"));
        TimestampUuidOrderNumberPolicy p =
                new TimestampUuidOrderNumberPolicy(
                        fixed,
                        () -> UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
                );
        //when
        String no = p.makeOrderNumber();

        //then
        assertEquals("20250819071234-123e4567-e89b-12d3-a456-426614174000", no);
    }

    @Test
    void 포맷_YYYYMMDDHHMMSS_하이픈_UUID로_포맷에_대한_검증() {
        Clock fixed = Clock.fixed(Instant.parse("2025-08-19T07:12:34Z"), ZoneId.of("Asia/Seoul"));
        TimestampUuidOrderNumberPolicy p =
                new TimestampUuidOrderNumberPolicy(fixed, UUID::randomUUID);

        String no = p.makeOrderNumber();
        assertTrue(no.matches("^\\d{14}-[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"));
    }

    //동시에 같은 정책으로 호출
    @Test
    void 호출시마다_서로_다른_주문번호가_생성된다() {
        Clock fixed = Clock.fixed(Instant.parse("2025-08-19T07:12:34Z"), ZoneId.of("Asia/Seoul"));

        UUID u1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID u2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        Supplier<UUID> seq = new Supplier<UUID>() {
            int i = 0;

            @Override
            public UUID get() {
                if (i == 0) {
                    i++;
                    return u1;
                } else {
                    i++;
                    return u2;
                }
            }
        };

        TimestampUuidOrderNumberPolicy policy = new TimestampUuidOrderNumberPolicy(fixed, seq);

        String no1 = policy.makeOrderNumber();
        String no2 = policy.makeOrderNumber();

        assertNotEquals(no1, no2);
        assertTrue(no1.endsWith("-aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        assertTrue(no2.endsWith("-bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
    }

    @Test
    void 주입받은_clock이_null이면_NPE(){
        assertThrows(NullPointerException.class, () -> new TimestampUuidOrderNumberPolicy(null, UUID::randomUUID));
    }

    @Test
    void uuidSupplier_null이면_NPE(){
        assertThrows(NullPointerException.class, () -> new TimestampUuidOrderNumberPolicy(Clock.systemUTC(), null));
    }

    @Test
    void 타임존에_따라_로컬타임이_반영된다() {
        Instant now = Instant.parse("2025-08-18T22:12:34Z");

        String seoul = new TimestampUuidOrderNumberPolicy(
                Clock.fixed(now, ZoneId.of("Asia/Seoul")),
                () -> UUID.fromString("00000000-0000-0000-0000-000000000000")
        ).makeOrderNumber();

        String utc = new TimestampUuidOrderNumberPolicy(
                Clock.fixed(now, ZoneId.of("UTC")),
                () -> UUID.fromString("00000000-0000-0000-0000-000000000000")
        ).makeOrderNumber();

        assertTrue(seoul.startsWith("20250819071234-")); // KST = UTC+9
        assertTrue(utc.startsWith("20250818221234-"));   // UTC
    }

    @Test
    void 병렬호출_1000번_모두_서로_다른_주문번호() {
        Clock fixed = Clock.fixed(Instant.parse("2025-08-19T07:12:34Z"), ZoneId.of("Asia/Seoul"));
        var p = new TimestampUuidOrderNumberPolicy(fixed, UUID::randomUUID);

        var set = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        java.util.stream.IntStream.range(0, 1000).parallel()
                .forEach(i -> set.add(p.makeOrderNumber()));

        assertEquals(1000, set.size());
    }
}