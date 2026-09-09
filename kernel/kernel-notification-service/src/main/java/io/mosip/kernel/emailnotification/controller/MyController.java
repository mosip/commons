package io.mosip.kernel.emailnotification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/hello")
public class MyController {

    @GetMapping("/greet")
    public String hello() {
        return "Hello World";
    }
}
