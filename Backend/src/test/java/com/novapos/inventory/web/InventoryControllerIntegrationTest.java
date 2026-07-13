package com.novapos.inventory.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final UUID variantId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();

    @Test
    @DisplayName("Record receipt movement and verify on-hand quantity")
    void recordReceiptAndCheckOnHand() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "productVariantId": "%s",
                                    "locationId": "%s",
                                    "quantityDelta": 100,
                                    "reason": "RECEIPT",
                                    "unitCostMinor": 1500
                                }
                                """, variantId, locationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productVariantId").value(variantId.toString()))
                .andExpect(jsonPath("$.quantityDelta").value(100))
                .andExpect(jsonPath("$.reason").value("RECEIPT"));

        mockMvc.perform(get("/api/v1/inventory/on-hand")
                        .param("productVariantId", variantId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onHandQuantity").value(100));
    }

    @Test
    @DisplayName("Multiple movements sum correctly for on-hand")
    void multipleMovementsSumCorrectly() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantityDelta": 50, "reason": "RECEIPT"}
                                """, variantId, locationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantityDelta": -10, "reason": "SALE"}
                                """, variantId, locationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantityDelta": 20, "reason": "RECEIPT"}
                                """, variantId, locationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/inventory/on-hand")
                        .param("productVariantId", variantId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onHandQuantity").value(60));
    }

    @Test
    @DisplayName("Get movements returns chronological list")
    void getMovementsReturnsChronologicalList() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantityDelta": 10, "reason": "RECEIPT"}
                                """, variantId, locationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantityDelta": -2, "reason": "SALE"}
                                """, variantId, locationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/inventory/movements")
                        .param("productVariantId", variantId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].quantityDelta").value(10))
                .andExpect(jsonPath("$[1].quantityDelta").value(-2));
    }

    @Test
    @DisplayName("Record receipt creates cost layers and FIFO consumes correctly")
    void receiptAndFifoConsumption() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantity": 10, "unitCostMinor": 1000}
                                """, variantId, locationId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/inventory/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantity": 20, "unitCostMinor": 1200}
                                """, variantId, locationId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/inventory/on-hand")
                        .param("productVariantId", variantId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onHandQuantity").value(30));

        mockMvc.perform(post("/api/v1/inventory/consume-fifo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s", "quantity": 15}
                                """, variantId, locationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("FIFO"))
                .andExpect(jsonPath("$.totalQuantity").value(15))
                .andExpect(jsonPath("$.consumedLayers", hasSize(2)))
                .andExpect(jsonPath("$.consumedLayers[0].unitCostMinor").value(1000))
                .andExpect(jsonPath("$.consumedLayers[0].quantity").value(10))
                .andExpect(jsonPath("$.consumedLayers[1].unitCostMinor").value(1200))
                .andExpect(jsonPath("$.consumedLayers[1].quantity").value(5));

        mockMvc.perform(get("/api/v1/inventory/on-hand")
                        .param("productVariantId", variantId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onHandQuantity").value(15));
    }

    @Test
    @DisplayName("Record receipt with batch and find expiring batches")
    void receiptWithBatchAndFindExpiring() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"productVariantId": "%s", "locationId": "%s",
                                "quantity": 5, "unitCostMinor": 500,
                                "batchCode": "BATCH-001", "expiryDate": "2026-12-31"}
                                """, variantId, locationId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/inventory/batches")
                        .param("productVariantId", variantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].batchCode").value("BATCH-001"));

        mockMvc.perform(get("/api/v1/inventory/batches/expiring-before")
                        .param("productVariantId", variantId.toString())
                        .param("date", "2099-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].batchCode").value("BATCH-001"));
    }
}
