package com.novapos.catalog.service;

import com.novapos.catalog.domain.Brand;
import com.novapos.catalog.domain.Category;
import com.novapos.catalog.domain.Product;
import com.novapos.catalog.domain.ProductVariant;
import com.novapos.catalog.repository.BrandRepository;
import com.novapos.catalog.repository.CategoryRepository;
import com.novapos.catalog.repository.ProductRepository;
import com.novapos.catalog.repository.ProductVariantRepository;
import com.novapos.catalog.web.CatalogException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    @DisplayName("Creating a product with duplicate SKU throws conflict")
    void duplicateSkuThrowsConflict() {
        var companyId = UUID.randomUUID();
        when(productRepository.existsBySkuAndCompanyIdAndDeletedAtIsNull("SKU001", companyId))
                .thenReturn(true);

        assertThatThrownBy(() -> catalogService.createProduct(companyId, "SKU001", "Test Product",
                1000L, "USD", "STANDARD", null, null))
                .isInstanceOf(CatalogException.class)
                .hasFieldOrPropertyWithValue("code", "DUPLICATE_SKU");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Creating a product successfully returns DTO")
    void createProductSuccess() {
        var companyId = UUID.randomUUID();
        var product = new Product(companyId, "SKU001", "Test Product", 1000L, "USD", "STANDARD");

        when(productRepository.existsBySkuAndCompanyIdAndDeletedAtIsNull("SKU001", companyId))
                .thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        var dto = catalogService.createProduct(companyId, "SKU001", "Test Product",
                1000L, "USD", "STANDARD", null, null);

        assertThat(dto.sku()).isEqualTo("SKU001");
        assertThat(dto.name()).isEqualTo("Test Product");
        assertThat(dto.basePriceMinor()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Updating product SKU to existing one throws conflict")
    void updateSkuToDuplicateThrowsConflict() {
        var productId = UUID.randomUUID();
        var companyId = UUID.randomUUID();
        var product = new Product(companyId, "SKU001", "Test Product", 1000L, "USD", "STANDARD");

        when(productRepository.findByIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndCompanyIdAndDeletedAtIsNull("SKU002", companyId))
                .thenReturn(true);

        assertThatThrownBy(() -> catalogService.updateProduct(productId, "SKU002", null,
                null, null, null, null, null))
                .isInstanceOf(CatalogException.class)
                .hasFieldOrPropertyWithValue("code", "DUPLICATE_SKU");
    }

    @Test
    @DisplayName("Fetching non-existent category throws not found")
    void nonExistentCategoryThrowsNotFound() {
        var categoryId = UUID.randomUUID();
        when(categoryRepository.findByIdAndDeletedAtIsNull(categoryId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateCategory(categoryId, "New Name", null))
                .isInstanceOf(CatalogException.class)
                .hasFieldOrPropertyWithValue("code", "CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("Fetching non-existent brand throws not found")
    void nonExistentBrandThrowsNotFound() {
        var brandId = UUID.randomUUID();
        when(brandRepository.findByIdAndDeletedAtIsNull(brandId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateBrand(brandId, "New Name"))
                .isInstanceOf(CatalogException.class)
                .hasFieldOrPropertyWithValue("code", "BRAND_NOT_FOUND");
    }

    @Test
    @DisplayName("Deleting a product soft-deletes it and its variants")
    void deleteProductSoftDeletesProductAndVariants() {
        var productId = UUID.randomUUID();
        var companyId = UUID.randomUUID();
        var product = new Product(companyId, "SKU001", "Test Product", 1000L, "USD", "STANDARD");
        var variant = new ProductVariant(productId, "Large", null, null);

        when(productRepository.findByIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.of(product));
        when(productVariantRepository.findByProductIdAndDeletedAtIsNull(productId))
                .thenReturn(java.util.List.of(variant));

        catalogService.deleteProduct(productId);

        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(variant.getDeletedAt()).isNotNull();
        verify(productRepository).save(product);
        verify(productVariantRepository).saveAll(any());
    }

    @Test
    @DisplayName("Creating variant for non-existent product throws not found")
    void createVariantForNonExistentProductThrowsNotFound() {
        var productId = UUID.randomUUID();
        when(productRepository.findByIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.createVariant(productId, "Large", null, null))
                .isInstanceOf(CatalogException.class)
                .hasFieldOrPropertyWithValue("code", "PRODUCT_NOT_FOUND");
    }

    @Test
    @DisplayName("Creating category successfully returns DTO")
    void createCategorySuccess() {
        var companyId = UUID.randomUUID();
        var category = new Category(companyId, "Electronics", null);

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        var dto = catalogService.createCategory(companyId, "Electronics", null);

        assertThat(dto.name()).isEqualTo("Electronics");
        assertThat(dto.companyId()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("Creating brand successfully returns DTO")
    void createBrandSuccess() {
        var companyId = UUID.randomUUID();
        var brand = new Brand(companyId, "Nike");

        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        var dto = catalogService.createBrand(companyId, "Nike");

        assertThat(dto.name()).isEqualTo("Nike");
        assertThat(dto.companyId()).isEqualTo(companyId);
    }
}
