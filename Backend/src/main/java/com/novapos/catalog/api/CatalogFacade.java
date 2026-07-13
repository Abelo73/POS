package com.novapos.catalog.api;

import com.novapos.catalog.api.dto.BrandDto;
import com.novapos.catalog.api.dto.BundleBreakdownDto;
import com.novapos.catalog.api.dto.CategoryDto;
import com.novapos.catalog.api.dto.EffectivePriceDto;
import com.novapos.catalog.api.dto.ProductDto;
import com.novapos.catalog.api.dto.ProductVariantDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogFacade {

    CategoryDto createCategory(UUID companyId, String name, UUID parentId);

    Optional<CategoryDto> getCategory(UUID categoryId);

    CategoryDto updateCategory(UUID categoryId, String name, UUID parentId);

    void deleteCategory(UUID categoryId);

    List<CategoryDto> getCategoriesByCompany(UUID companyId);

    BrandDto createBrand(UUID companyId, String name);

    Optional<BrandDto> getBrand(UUID brandId);

    BrandDto updateBrand(UUID brandId, String name);

    void deleteBrand(UUID brandId);

    List<BrandDto> getBrandsByCompany(UUID companyId);

    ProductDto createProduct(UUID companyId, String sku, String name, long basePriceMinor,
                             String currency, String taxClass, UUID categoryId, UUID brandId);

    Optional<ProductDto> getProduct(UUID productId);

    ProductDto updateProduct(UUID productId, String sku, String name, Long basePriceMinor,
                             String currency, String taxClass, UUID categoryId, UUID brandId);

    void deleteProduct(UUID productId);

    List<ProductDto> getProductsByCompany(UUID companyId);

    List<ProductDto> getProductsByCategory(UUID categoryId);

    List<ProductDto> getProductsByBrand(UUID brandId);

    ProductVariantDto createVariant(UUID productId, String variantName, String barcode, Long priceOverrideMinor);

    Optional<ProductVariantDto> getVariant(UUID variantId);

    ProductVariantDto updateVariant(UUID variantId, String variantName, String barcode, Long priceOverrideMinor);

    void deleteVariant(UUID variantId);

    List<ProductVariantDto> getVariantsByProduct(UUID productId);

    EffectivePriceDto getEffectivePrice(UUID productVariantId, UUID branchId);

    void upsertBranchPriceOverride(UUID productId, UUID branchId, long priceMinor);

    BundleBreakdownDto getBundleBreakdown(UUID productId);

    void addBundleComponent(UUID bundleProductId, UUID componentProductId, BigDecimal quantity);

    void removeBundleComponent(UUID bundleProductId, UUID componentProductId);
}
