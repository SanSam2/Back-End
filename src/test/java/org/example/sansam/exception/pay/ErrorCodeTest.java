package org.example.sansam.exception.pay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class ErrorCodeTest {

    @Test
    void 모든_ErrorCode가_메시지를_담고있다() {
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(code.getMessage()).isNotNull().isNotBlank();
        }
    }
}