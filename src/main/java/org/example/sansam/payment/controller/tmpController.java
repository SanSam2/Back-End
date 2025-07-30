package org.example.sansam.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payments")
public class tmpController {

    @GetMapping("/success")
    public String success() { //결제 승인에 필요한 paymentKey, orderId, amount 파라미터를 리턴해준다.
        return "success";
    }

    @GetMapping("/failure")
    public String failure() {
        return "failure";
    }
}
