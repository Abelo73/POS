package com.novapos.catalog.web;

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
class CatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final UUID companyId = UUID.randomUUID();

    @Test
    @DisplayName("Create category, then fetch and update it")
    void createGetAndUpdateCategory() throws Exception {
        var result = createCategory("Electronics", null);
        var categoryId = extractId(result);

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));

        mockMvc.perform(put("/api/v1/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Home Electronics\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Home Electronics"));
    }

    @Test
    @DisplayName("Create category tree with parent-child relationship")
    void createCategoryTree() throws Exception {
        var parentResult = createCategory("Electronics", null);
        var parentId = extractId(parentResult);

        var childResult = createCategory("Laptops", parentId);
        var childId = extractId(childResult);

        mockMvc.perform(get("/api/v1/categories/{categoryId}", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptops"))
                .andExpect(jsonPath("$.parentId").value(parentId.toString()));
    }

    @Test
    @DisplayName("Delete non-existent category returns 404")
    void getNonExistentCategoryReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("Create brand, then fetch and update it")
    void createGetAndUpdateBrand() throws Exception {
        var result = createBrand("Nike");
        var brandId = extractId(result);

        mockMvc.perform(get("/api/v1/brands/{brandId}", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nike"));

        mockMvc.perform(put("/api/v1/brands/{brandId}", brandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Adidas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Adidas"));
    }

    @Test
    @DisplayName("Delete non-existent brand returns 404")
    void getNonExistentBrandReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/brands/{brandId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRAND_NOT_FOUND"));
    }

    @Test
    @DisplayName("Create product with SKU uniqueness, then update and delete")
    void createProductWithSkuUniqueness() throws Exception {
        var catResult = createCategory("Phones", null);
        var catId = extractId(catResult);
        var brandResult = createBrand("Apple");
        var brandId = extractId(brandResult);

        var productResult = createProduct("IPHONE-15", "iPhone 15", 99900L, "USD", catId, brandId);
        var productId = extractId(productResult);

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("IPHONE-15"))
                .andExpect(jsonPath("$.name").value("iPhone 15"));

        mockMvc.perform(put("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"iPhone 15 Pro\", \"basePriceMinor\": 109900}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.basePriceMinor").value(109900));

        mockMvc.perform(post("/api/v1/companies/{companyId}/products", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku": "IPHONE-15", "name": "Dup Phone",
                                "basePriceMinor": 1000, "currency": "USD"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_SKU"));

        mockMvc.perform(delete("/api/v1/products/{productId}", productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create variants for a product and list them")
    void createVariantsAndList() throws Exception {
        var productResult = createProduct("SHIRT-001", "T-Shirt", 2500L, "USD", null, null);
        var productId = extractId(productResult);

        mockMvc.perform(post("/api/v1/products/{productId}/variants", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantName\": \"Medium / Blue\", \"barcode\": \"5901234567890\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products/{productId}/variants", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantName\": \"Large / Red\", \"barcode\": \"5901234567891\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products/{productId}/variants", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].variantName").value("Medium / Blue"))
                .andExpect(jsonPath("$[1].variantName").value("Large / Red"));
    }

    @Test
    @DisplayName("List products by category and brand filters")
    void listProductsByCategoryAndBrand() throws Exception {
        var electronicId = extractId(createCategory("Electronics", null));
        var clothingId = extractId(createCategory("Clothing", null));
        var samsungId = extractId(createBrand("Samsung"));
        var nikeId = extractId(createBrand("Nike"));

        createProduct("TV-001", "Smart TV", 50000L, "USD", electronicId, samsungId);
        createProduct("SHOE-001", "Running Shoe", 12000L, "USD", clothingId, nikeId);

        mockMvc.perform(get("/api/v1/categories/{categoryId}/products", electronicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Smart TV"));

        mockMvc.perform(get("/api/v1/brands/{brandId}/products", nikeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Running Shoe"));
    }

    @Test
    @DisplayName("Delete existing category and brand return 204")
    void deleteCategoryAndBrand() throws Exception {
        var catId = extractId(createCategory("Toys", null));
        var brandId = extractId(createBrand("Lego"));

        mockMvc.perform(delete("/api/v1/categories/{categoryId}", catId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/categories/{categoryId}", catId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/brands/{brandId}", brandId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/brands/{brandId}", brandId))
                .andExpect(status().isNotFound());
    }

    private MvcResult createCategory(String name, UUID parentId) throws Exception {
        var parentJson = parentId != null ? ", \"parentId\": \"" + parentId + "\"" : "";
        return mockMvc.perform(post("/api/v1/companies/{companyId}/categories", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"name\": \"%s\"%s}", name, parentJson)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private MvcResult createBrand(String name) throws Exception {
        return mockMvc.perform(post("/api/v1/companies/{companyId}/brands", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"name\": \"%s\"}", name)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private MvcResult createProduct(String sku, String name, long basePriceMinor, String currency,
                                     UUID categoryId, UUID brandId) throws Exception {
        var catJson = categoryId != null ? ", \"categoryId\": \"" + categoryId + "\"" : "";
        var brandJson = brandId != null ? ", \"brandId\": \"" + brandId + "\"" : "";
        return mockMvc.perform(post("/api/v1/companies/{companyId}/products", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"sku": "%s", "name": "%s", "basePriceMinor": %d,
                                "currency": "%s"%s%s}
                                """, sku, name, basePriceMinor, currency, catJson, brandJson)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UUID extractId(MvcResult result) {
        String location = result.getResponse().getHeader("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
