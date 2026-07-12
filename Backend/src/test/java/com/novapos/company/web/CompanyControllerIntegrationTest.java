package com.novapos.company.web;

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
class CompanyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Create company, then fetch and update it")
    void createGetAndUpdateCompany() throws Exception {
        var result = createCompany("Test Corp", "USD", "HQ", "UTC", "DEFAULT", "USD");
        var companyId = extractId(result);

        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Corp"))
                .andExpect(jsonPath("$.defaultCurrency").value("USD"));

        mockMvc.perform(put("/api/v1/companies/{companyId}", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Corp\", \"defaultCurrency\": \"EUR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Corp"))
                .andExpect(jsonPath("$.defaultCurrency").value("EUR"));
    }

    @Test
    @DisplayName("Create company with branch, then list branches")
    void createCompanyAndListBranches() throws Exception {
        var result = createCompany("BranchCorp", "USD", "HQ", "UTC", "DEFAULT", "USD");
        var companyId = extractId(result);

        mockMvc.perform(post("/api/v1/companies/{companyId}/branches", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Branch A\", \"timezone\": \"America/New_York\", \"taxZone\": \"NYC\", \"currency\": \"USD\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/companies/{companyId}/branches", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("HQ"))
                .andExpect(jsonPath("$[1].name").value("Branch A"));
    }

    @Test
    @DisplayName("Delete non-existent company returns 404")
    void getNonExistentCompanyReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/companies/{companyId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMPANY_NOT_FOUND"));
    }

    @Test
    @DisplayName("Delete non-existent company returns 404")
    void deleteNonExistentCompanyReturns404() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/{companyId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create company and verify it appears in list")
    void listCompaniesIncludesCreatedCompany() throws Exception {
        var result = createCompany("ListCorp", "USD", "HQ", "UTC", "DEFAULT", "USD");
        var companyId = extractId(result);

        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("ListCorp"));
    }

    @Test
    @DisplayName("Delete existing company returns 204")
    void deleteExistingCompanyReturns204() throws Exception {
        var result = createCompany("DelCorp", "USD", "HQ", "UTC", "DEFAULT", "USD");
        var companyId = extractId(result);

        mockMvc.perform(delete("/api/v1/companies/{companyId}", companyId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId))
                .andExpect(status().isNotFound());
    }

    private MvcResult createCompany(String name, String currency, String branchName, String tz, String taxZone, String branchCurrency) throws Exception {
        return mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "%s",
                                    "defaultCurrency": "%s",
                                    "defaultBranchName": "%s",
                                    "timezone": "%s",
                                    "taxZone": "%s",
                                    "currency": "%s"
                                }
                                """, name, currency, branchName, tz, taxZone, branchCurrency)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UUID extractId(MvcResult result) {
        String location = result.getResponse().getHeader("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
