package com.novapos.user.web;

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
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Get all roles returns 17 seeded roles")
    void getAllRolesReturnsSeededRoles() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(17)))
                .andExpect(jsonPath("$[0].name").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$[0].id", notNullValue()));
    }

    @Test
    @DisplayName("Create user returns 201 and correct fields")
    void createUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "companyId": "%s",
                                    "email": "cashier@test.com"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("cashier@test.com"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("Get user by ID returns user with roles")
    void getUserByIdReturnsUserWithRoles() throws Exception {
        var companyId = UUID.randomUUID();

        var result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "companyId": "%s",
                                    "email": "manager@test.com"
                                }
                                """.formatted(companyId)))
                .andExpect(status().isCreated())
                .andReturn();

        var location = result.getResponse().getHeader("Location");
        var userId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

        var branchId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "roleName": "BRANCH_MANAGER",
                                    "branchId": "%s"
                                }
                                """.formatted(branchId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("manager@test.com"))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0].roleName").value("BRANCH_MANAGER"))
                .andExpect(jsonPath("$.roles[0].branchId").value(branchId.toString()));
    }

    @Test
    @DisplayName("Duplicate email returns 409")
    void duplicateEmailReturns409() throws Exception {
        var companyId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "companyId": "%s",
                                    "email": "dup@test.com"
                                }
                                """.formatted(companyId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "companyId": "%s",
                                    "email": "dup@test.com"
                                }
                                """.formatted(companyId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_EXISTS"));
    }

    @Test
    @DisplayName("Non-existent user returns 404")
    void nonExistentUserReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("Assign non-existent role returns 404")
    void assignNonExistentRoleReturns404() throws Exception {
        var companyId = UUID.randomUUID();
        var result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "companyId": "%s",
                                    "email": "norole@test.com"
                                }
                                """.formatted(companyId)))
                .andExpect(status().isCreated())
                .andReturn();

        var location = result.getResponse().getHeader("Location");
        var userId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\": \"NONEXISTENT\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_FOUND"));
    }
}
