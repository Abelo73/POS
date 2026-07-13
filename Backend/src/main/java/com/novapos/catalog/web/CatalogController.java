package com.novapos.catalog.web;

import com.novapos.catalog.api.CatalogFacade;
import com.novapos.catalog.api.dto.BrandDto;
import com.novapos.catalog.api.dto.BundleBreakdownDto;
import com.novapos.catalog.api.dto.CategoryDto;
import com.novapos.catalog.api.dto.EffectivePriceDto;
import com.novapos.catalog.api.dto.ProductDto;
import com.novapos.catalog.api.dto.ProductVariantDto;
import com.novapos.catalog.web.dto.AddBundleComponentRequest;
import com.novapos.catalog.web.dto.BranchPriceOverrideRequest;
import com.novapos.catalog.web.dto.CreateBrandRequest;
import com.novapos.catalog.web.dto.CreateCategoryRequest;
import com.novapos.catalog.web.dto.CreateProductRequest;
import com.novapos.catalog.web.dto.CreateVariantRequest;
import com.novapos.catalog.web.dto.UpdateBrandRequest;
import com.novapos.catalog.web.dto.UpdateCategoryRequest;
import com.novapos.catalog.web.dto.UpdateProductRequest;
import com.novapos.catalog.web.dto.UpdateVariantRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
class CatalogController {

    private final CatalogFacade catalogFacade;

    CatalogController(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    @PostMapping("/companies/{companyId}/categories")
    ResponseEntity<CategoryDto> createCategory(@PathVariable UUID companyId, @Valid @RequestBody CreateCategoryRequest request) {
        var category = catalogFacade.createCategory(companyId, request.name(), request.parentId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(category.id())
                .toUri();
        return ResponseEntity.created(location).body(category);
    }

    @GetMapping("/categories/{categoryId}")
    ResponseEntity<CategoryDto> getCategory(@PathVariable UUID categoryId) {
        return catalogFacade.getCategory(categoryId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> CatalogException.categoryNotFound(categoryId));
    }

    @PutMapping("/categories/{categoryId}")
    ResponseEntity<CategoryDto> updateCategory(@PathVariable UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
        var category = catalogFacade.updateCategory(categoryId, request.name(), request.parentId());
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        catalogFacade.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies/{companyId}/categories")
    ResponseEntity<List<CategoryDto>> getCategoriesByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(catalogFacade.getCategoriesByCompany(companyId));
    }

    @PostMapping("/companies/{companyId}/brands")
    ResponseEntity<BrandDto> createBrand(@PathVariable UUID companyId, @Valid @RequestBody CreateBrandRequest request) {
        var brand = catalogFacade.createBrand(companyId, request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(brand.id())
                .toUri();
        return ResponseEntity.created(location).body(brand);
    }

    @GetMapping("/brands/{brandId}")
    ResponseEntity<BrandDto> getBrand(@PathVariable UUID brandId) {
        return catalogFacade.getBrand(brandId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> CatalogException.brandNotFound(brandId));
    }

    @PutMapping("/brands/{brandId}")
    ResponseEntity<BrandDto> updateBrand(@PathVariable UUID brandId, @Valid @RequestBody UpdateBrandRequest request) {
        var brand = catalogFacade.updateBrand(brandId, request.name());
        return ResponseEntity.ok(brand);
    }

    @DeleteMapping("/brands/{brandId}")
    ResponseEntity<Void> deleteBrand(@PathVariable UUID brandId) {
        catalogFacade.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies/{companyId}/brands")
    ResponseEntity<List<BrandDto>> getBrandsByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(catalogFacade.getBrandsByCompany(companyId));
    }

    @PostMapping("/companies/{companyId}/products")
    ResponseEntity<ProductDto> createProduct(@PathVariable UUID companyId, @Valid @RequestBody CreateProductRequest request) {
        var product = catalogFacade.createProduct(companyId, request.sku(), request.name(),
                request.basePriceMinor(), request.currency(), request.taxClass(),
                request.categoryId(), request.brandId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.id())
                .toUri();
        return ResponseEntity.created(location).body(product);
    }

    @GetMapping("/products/{productId}")
    ResponseEntity<ProductDto> getProduct(@PathVariable UUID productId) {
        return catalogFacade.getProduct(productId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> CatalogException.productNotFound(productId));
    }

    @PutMapping("/products/{productId}")
    ResponseEntity<ProductDto> updateProduct(@PathVariable UUID productId, @Valid @RequestBody UpdateProductRequest request) {
        var product = catalogFacade.updateProduct(productId, request.sku(), request.name(),
                request.basePriceMinor(), request.currency(), request.taxClass(),
                request.categoryId(), request.brandId());
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/products/{productId}")
    ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        catalogFacade.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies/{companyId}/products")
    ResponseEntity<List<ProductDto>> getProductsByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(catalogFacade.getProductsByCompany(companyId));
    }

    @GetMapping("/categories/{categoryId}/products")
    ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(catalogFacade.getProductsByCategory(categoryId));
    }

    @GetMapping("/brands/{brandId}/products")
    ResponseEntity<List<ProductDto>> getProductsByBrand(@PathVariable UUID brandId) {
        return ResponseEntity.ok(catalogFacade.getProductsByBrand(brandId));
    }

    @PostMapping("/products/{productId}/variants")
    ResponseEntity<ProductVariantDto> createVariant(@PathVariable UUID productId, @Valid @RequestBody CreateVariantRequest request) {
        var variant = catalogFacade.createVariant(productId, request.variantName(), request.barcode(), request.priceOverrideMinor());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(variant.id())
                .toUri();
        return ResponseEntity.created(location).body(variant);
    }

    @GetMapping("/variants/{variantId}")
    ResponseEntity<ProductVariantDto> getVariant(@PathVariable UUID variantId) {
        return catalogFacade.getVariant(variantId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> CatalogException.variantNotFound(variantId));
    }

    @PutMapping("/variants/{variantId}")
    ResponseEntity<ProductVariantDto> updateVariant(@PathVariable UUID variantId, @Valid @RequestBody UpdateVariantRequest request) {
        var variant = catalogFacade.updateVariant(variantId, request.variantName(), request.barcode(), request.priceOverrideMinor());
        return ResponseEntity.ok(variant);
    }

    @DeleteMapping("/variants/{variantId}")
    ResponseEntity<Void> deleteVariant(@PathVariable UUID variantId) {
        catalogFacade.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{productId}/variants")
    ResponseEntity<List<ProductVariantDto>> getVariantsByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(catalogFacade.getVariantsByProduct(productId));
    }

    @GetMapping("/variants/{variantId}/branches/{branchId}/effective-price")
    ResponseEntity<EffectivePriceDto> getEffectivePrice(@PathVariable UUID variantId, @PathVariable UUID branchId) {
        return ResponseEntity.ok(catalogFacade.getEffectivePrice(variantId, branchId));
    }

    @PutMapping("/products/{productId}/branches/{branchId}/price-override")
    ResponseEntity<Void> upsertBranchPriceOverride(@PathVariable UUID productId, @PathVariable UUID branchId,
                                                    @RequestBody BranchPriceOverrideRequest request) {
        catalogFacade.upsertBranchPriceOverride(productId, branchId, request.priceMinor());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/{productId}/bundle-breakdown")
    ResponseEntity<BundleBreakdownDto> getBundleBreakdown(@PathVariable UUID productId) {
        return ResponseEntity.ok(catalogFacade.getBundleBreakdown(productId));
    }

    @PostMapping("/products/{productId}/bundle-components")
    ResponseEntity<Void> addBundleComponent(@PathVariable UUID productId, @Valid @RequestBody AddBundleComponentRequest request) {
        catalogFacade.addBundleComponent(productId, request.componentProductId(), request.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/products/{productId}/bundle-components/{componentProductId}")
    ResponseEntity<Void> removeBundleComponent(@PathVariable UUID productId, @PathVariable UUID componentProductId) {
        catalogFacade.removeBundleComponent(productId, componentProductId);
        return ResponseEntity.noContent().build();
    }
}
