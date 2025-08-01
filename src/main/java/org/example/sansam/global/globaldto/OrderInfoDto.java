package org.example.sansam.global.globaldto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


import java.time.LocalDateTime;

@Data
@Getter
@RequiredArgsConstructor
public class OrderInfoDto {
    private String username;
    private String ordername;
    private LocalDateTime orderdate;
    private LocalDateTime paydate;
}
