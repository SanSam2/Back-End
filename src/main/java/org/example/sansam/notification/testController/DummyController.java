package org.example.sansam.notification.testController;

import jakarta.validation.constraints.NotBlank;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    @GetMapping("/custom")
    public void custom() {
        throw new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND);
    }

    @PostMapping("/valid")
    public void valid(@RequestBody @jakarta.validation.Valid ValidDto dto) {}

    public static class ValidDto {
        @NotBlank
        public String name;
    }

    @GetMapping("/missing")
    public void missing(@RequestParam String param) {}

    @GetMapping("/type-mismatch/{id}")
    public void typeMismatch(@PathVariable Long id) {}

    @PostMapping("/json")
    public void json(@RequestBody ValidDto dto) {}

    @PostMapping("/post-only")
    public void postOnly() {}

    @GetMapping("/data-integrity")
    public void dataIntegrity() {
        throw new org.springframework.dao.DataIntegrityViolationException("duplicate");
    }

    @GetMapping("/illegal")
    public void illegal() {
        throw new IllegalArgumentException("boom");
    }
}
