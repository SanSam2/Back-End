package org.example.sansam.exception.pay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class CustomExceptionTest {

    @Test
    void CustomException이_메시지를_제대로_노출해야한다() {
        CustomException ex = new CustomException(ErrorCode.NO_ITEM_IN_ORDER);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NO_ITEM_IN_ORDER);
        assertThat(ex).hasMessage(ErrorCode.NO_ITEM_IN_ORDER.getMessage());
    }
}