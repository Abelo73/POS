package com.novapos.pos.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class PosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private UUID createSale(String branchId, String cashierId, String clientUuid) throws Exception {
        var result = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"branchId": "%s", "cashierId": "%s",
                                "currency": "USD", "clientUuid": "%s"}
                                """, branchId, cashierId, clientUuid)))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    @DisplayName("Full sale lifecycle: create, add lines, pay, complete")
    void fullSaleLifecycle() throws Exception {
        var saleId = createSale(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/sales/{saleId}/lines", saleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productVariantId": "%s", "quantity": 2, "unitPriceMinor": 1500}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/sales/{saleId}/lines", saleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productVariantId": "%s", "quantity": 1, "unitPriceMinor": 1000}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/sales/{saleId}", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(2)))
                .andExpect(jsonPath("$.totalMinor").value(4000));

        mockMvc.perform(post("/api/v1/sales/{saleId}/payments", saleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\": \"CASH\", \"amountMinor\": 4000}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/sales/{saleId}/complete", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Hold and resume a sale")
    void holdAndResumeSale() throws Exception {
        var saleId = createSale(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());

        mockMvc.perform(put("/api/v1/sales/{saleId}/hold", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HELD"));

        mockMvc.perform(put("/api/v1/sales/{saleId}/resume", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("Duplicate client UUID returns 409")
    void duplicateClientUuidReturns409() throws Exception {
        var clientUuid = UUID.randomUUID().toString();
        createSale(UUID.randomUUID().toString(), UUID.randomUUID().toString(), clientUuid);

        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"branchId": "%s", "cashierId": "%s",
                                "currency": "USD", "clientUuid": "%s"}
                                """, UUID.randomUUID(), UUID.randomUUID(), clientUuid)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CLIENT_UUID"));
    }

    @Test
    @DisplayName("Payment mismatch prevents completion")
    void paymentMismatchPreventsCompletion() throws Exception {
        var saleId = createSale(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/sales/{saleId}/lines", saleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productVariantId": "%s", "quantity": 1, "unitPriceMinor": 5000}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/sales/{saleId}/payments", saleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\": \"CASH\", \"amountMinor\": 1000}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/sales/{saleId}/complete", saleId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PAYMENT_MISMATCH"));
    }

    @Test
    @DisplayName("Void open sale succeeds")
    void voidOpenSaleSucceeds() throws Exception {
        var saleId = createSale(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());

        mockMvc.perform(put("/api/v1/sales/{saleId}/void", saleId))
                .andExpect(status().isNoContent());
    }
}
