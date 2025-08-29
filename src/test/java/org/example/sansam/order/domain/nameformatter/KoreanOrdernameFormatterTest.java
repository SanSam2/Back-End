package org.example.sansam.order.domain.nameformatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class KoreanOrdernameFormatterTest {

    private final OrderNameFormatter f = KoreanOrdernameFormatter.INSTANCE;

    @ParameterizedTest
    @CsvSource({
            "프라다 반팔,0,프라다 반팔",
            "프라다 반팔,1,프라다 반팔 외 1건",
            "셔츠,3,셔츠 외 3건"
    })
    void format규칙에_따라서_주문명을_만들어낼_수_있다(String first, int others, String expected) {
        //given -> 위에서 주어진 first, others, expected
        //when
        String name = f.format(first,others);

        //then
        assertEquals(expected,name);
    }

    @ParameterizedTest
    @CsvSource({
            "'화이트 셔츠',2,'화이트 셔츠 외 2건'",
            "'😀티셔츠',1,'😀티셔츠 외 1건'"
    })
    void 상품명에_유니코드와_공백문자가_포함된_경우에도_처리할_수_있다(String first, int others, String expected) {
        //given -> 위에서 주어진 first,others, expected
        //when
        String name = f.format(first, others);

        //then
        assertEquals(expected, name);
    }

    @Test
    void INSTANCE는_항상_같은_객체다(){
        //given & when & then
        assertSame(KoreanOrdernameFormatter.INSTANCE, KoreanOrdernameFormatter.INSTANCE);
    }

    @Test
    void 생성자는_private이다() throws Exception{
        //given & when
        var cunstroctor = KoreanOrdernameFormatter.class.getDeclaredConstructor();

       //then
        assertTrue(Modifier.isPrivate(cunstroctor.getModifiers()));
    }

}