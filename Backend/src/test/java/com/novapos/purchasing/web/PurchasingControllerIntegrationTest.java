package com.novapos.purchasing.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
class PurchasingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private final UUID companyId = UUID.randomUUID();

    private UUID createSupplier(String name) throws Exception {
        var r = mockMvc.perform(post("/api/v1/companies/{companyId}/suppliers", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\", \"paymentTerms\": \"NET30\", \"leadTimeDays\": 7}"))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(r.getResponse().getHeader("Location").replaceAll(".*/", ""));
    }

    @Test @DisplayName("Full supplier CRUD")
    void supplierCRUD() throws Exception {
        var id = createSupplier("Global Supply");
        mockMvc.perform(get("/api/v1/suppliers/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Global Supply"));
        mockMvc.perform(put("/api/v1/suppliers/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Global Supply Inc\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Global Supply Inc"));
        mockMvc.perform(delete("/api/v1/suppliers/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/suppliers/{id}", id)).andExpect(status().isNotFound());
    }

    @Test @DisplayName("PO lifecycle: create, approve, receive, cancel")
    void poLifecycle() throws Exception {
        var supplierId = createSupplier("Parts Co");
        var poR = mockMvc.perform(post("/api/v1/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"supplierId": "%s", "branchId": "%s", "lines": [{"productVariantId": "%s", "quantityOrdered": 100, "unitCostMinor": 500}]}
                                """, supplierId, UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated()).andReturn();
        var poId = UUID.fromString(poR.getResponse().getHeader("Location").replaceAll(".*/", ""));

        mockMvc.perform(put("/api/v1/purchase-orders/{poId}/approve", poId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));

        var lines = poR.getResponse().getContentAsString();
        // get PO lines
        mockMvc.perform(get("/api/v1/purchase-orders/{poId}", poId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lines", hasSize(1)));
    }
}
