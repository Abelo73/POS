package com.novapos.shared.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErrorTestController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("NovaPosException returns documented JSON shape with correct HTTP status")
    void novaPosExceptionReturnsErrorResponseShape() throws Exception {
        mockMvc.perform(get("/api/v1/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Field 'name' is required."))
                .andExpect(jsonPath("$.traceId").isString())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    @DisplayName("404 NovaPosException returns correct status code")
    void notFoundExceptionReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No product found with id=X."));
    }

    @Test
    @DisplayName("Unhandled exceptions return 500 with safe message, not stack trace")
    void unhandledExceptionReturns500WithoutStackTrace() throws Exception {
        mockMvc.perform(get("/api/v1/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.traceId").isString());
    }
}
