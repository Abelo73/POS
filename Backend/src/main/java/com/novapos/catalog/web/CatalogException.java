package com.novapos.catalog.web;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CatalogException extends NovaPosException {

    public CatalogException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    public static CatalogException categoryNotFound(UUID categoryId) {
        return new CatalogException("CATEGORY_NOT_FOUND", "Category not found: " + categoryId, HttpStatus.NOT_FOUND);
    }

    public static CatalogException brandNotFound(UUID brandId) {
        return new CatalogException("BRAND_NOT_FOUND", "Brand not found: " + brandId, HttpStatus.NOT_FOUND);
    }

    public static CatalogException productNotFound(UUID productId) {
        return new CatalogException("PRODUCT_NOT_FOUND", "Product not found: " + productId, HttpStatus.NOT_FOUND);
    }

    public static CatalogException variantNotFound(UUID variantId) {
        return new CatalogException("VARIANT_NOT_FOUND", "Product variant not found: " + variantId, HttpStatus.NOT_FOUND);
    }

    public static CatalogException duplicateSku(String sku, UUID companyId) {
        return new CatalogException("DUPLICATE_SKU", "SKU '" + sku + "' already exists for this company", HttpStatus.CONFLICT);
    }

    public static CatalogException notABundle(UUID productId) {
        return new CatalogException("NOT_A_BUNDLE", "Product is not a composite/bundle: " + productId, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
