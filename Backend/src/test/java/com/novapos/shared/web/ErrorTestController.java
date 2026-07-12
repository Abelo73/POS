package com.novapos.shared.web;

import com.novapos.shared.exception.TestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
class ErrorTestController {

    @GetMapping("/bad-request")
    String throwBadRequest() {
        throw new TestException("VALIDATION_ERROR", "Field 'name' is required.", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/not-found")
    String throwNotFound() {
        throw new TestException("RESOURCE_NOT_FOUND", "No product found with id=X.", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/server-error")
    String throwServerError() {
        throw new RuntimeException("Simulated unexpected failure");
    }
}
