package com.michelet.waiting;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/waitings/hello")
    public String hello() {
        return "hello from waiting-service";
    }
}