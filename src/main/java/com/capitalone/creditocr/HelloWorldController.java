package com.capitalone.creditocr;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a simple hello world endpoint to test the application.
 *
 * TODO: remove this
 */
@RestController
public class HelloWorldController {

    @RequestMapping(
            value = "/hello",
            method = RequestMethod.GET
    )
    public String processRequest() {
        return "Hello World";
    }
}
