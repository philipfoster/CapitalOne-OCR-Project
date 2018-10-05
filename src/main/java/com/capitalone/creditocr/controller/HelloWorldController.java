package com.capitalone.creditocr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * This is a simple hello world endpoint to test the application.
 */
@RestController
public class HelloWorldController {

    private Random random = new Random();

    public HelloWorldController() {}


    @GetMapping("/random")
    public String process2(){
        return Integer.toString(random.nextInt(1000));
    }

    @GetMapping("/hello")
    public String processRequest() {
        return "Hello World!";
    }

}
