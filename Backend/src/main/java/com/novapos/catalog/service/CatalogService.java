package com.novapos.catalog.service;

import com.novapos.catalog.api.CatalogFacade;
import com.novapos.catalog.api.dto.BrandDto;
import com.novapos.catalog.api.dto.CategoryDto;
import com.novapos.catalog.api.dto.ProductDto;
import com.novapos.catalog.api.dto.ProductVariantDto;
import com.novapos.catalog.domain.Brand;
import com.novapos.catalog.domain.Category;
import com.novapos.catalog.domain.Product;
import com.novapos.catalog.domain.ProductVariant;
import com.novapos.catalog.repository.BrandRepository;
import com.novapos.catalog.repository.CategoryRepository;
import com.novapos.catalog.repository.ProductRepository;
import com.novapos.catalog.repository.ProductVariantRepository;
import com.novapos.catalog.web.CatalogException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class CatalogService implements CatalogFacade {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    CatalogService(CategoryRepository categoryRepository, BrandRepository brandRepository,
                   ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public CategoryDto createCategory(UUID companyId, String name, UUID parentId) {
        var category = new Category(companyId, name, parentId);
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Override
    public Optional<CategoryDto> getCategory(UUID categoryId) {
        return categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .map(CatalogService::toDto);
    }

    @Override
    public CategoryDto updateCategory(UUID categoryId, String name, UUID parentId) {
        var category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> CatalogException.categoryNotFound(categoryId));
        if (name != null) {
            category.setName(name);
        }
        if (parentId != null) {
            category.setParentId(parentId);
        }
        category.markUpdated();
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        var category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> CatalogException.categoryNotFound(categoryId));
        category.setDeletedAt(Instant.now());
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryDto> getCategoriesByCompany(UUID companyId) {
        return categoryRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(CatalogService::toDto)
                .toList();
    }

    @Override
    public BrandDto createBrand(UUID companyId, String name) {
        var brand = new Brand(companyId, name);
        brand = brandRepository.save(brand);
        return toDto(brand);
    }

    @Override
    public Optional<BrandDto> getBrand(UUID brandId) {
        return brandRepository.findByIdAndDeletedAtIsNull(brandId)
                .map(CatalogService::toDto);
    }

    @Override
    public BrandDto updateBrand(UUID brandId, String name) {
        var brand = brandRepository.findByIdAndDeletedAtIsNull(brandId)
                .orElseThrow(() -> CatalogException.brandNotFound(brandId));
        if (name != null) {
            brand.setName(name);
        }
        brand.markUpdated();
        brand = brandRepository.save(brand);
        return toDto(brand);
    }

    @Override
    public void deleteBrand(UUID brandId) {
        var brand = brandRepository.findByIdAndDeletedAtIsNull(brandId)
                .orElseThrow(() -> CatalogException.brandNotFound(brandId));
        brand.setDeletedAt(Instant.now());
        brandRepository.save(brand);
    }

    @Override
    public List<BrandDto> getBrandsByCompany(UUID companyId) {
        return brandRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(CatalogService::toDto)
                .toList();
    }

    @Override
    public ProductDto createProduct(UUID companyId, String sku, String name, long basePriceMinor,
                                    String currency, String taxClass, UUID categoryId, UUID brandId) {
        if (productRepository.existsBySkuAndCompanyIdAndDeletedAtIsNull(sku, companyId)) {
            throw CatalogException.duplicateSku(sku, companyId);
        }
        var product = new Product(companyId, sku, name, basePriceMinor, currency, taxClass != null ? taxClass : "STANDARD");
        product.setCategoryId(categoryId);
        product.setBrandId(brandId);
        product = productRepository.save(product);
        return toDto(product);
    }

    @Override
    public Optional<ProductDto> getProduct(UUID productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
                .map(CatalogService::toDto);
    }

    @Override
    public ProductDto updateProduct(UUID productId, String sku, String name, Long basePriceMinor,
                                    String currency, String taxClass, UUID categoryId, UUID brandId) {
        var product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> CatalogException.productNotFound(productId));
        if (sku != null && !sku.equals(product.getSku())) {
            if (productRepository.existsBySkuAndCompanyIdAndDeletedAtIsNull(sku, product.getCompanyId())) {
                throw CatalogException.duplicateSku(sku, product.getCompanyId());
            }
            product.setSku(sku);
        }
        if (name != null) {
            product.setName(name);
        }
        if (basePriceMinor != null) {
            product.setBasePriceMinor(basePriceMinor);
        }
        if (currency != null) {
            product.setCurrency(currency);
        }
        if (taxClass != null) {
            product.setTaxClass(taxClass);
        }
        if (categoryId != null) {
            product.setCategoryId(categoryId);
        }
        if (brandId != null) {
            product.setBrandId(brandId);
        }
        product.markUpdated();
        product = productRepository.save(product);
        return toDto(product);
    }

    @Override
    public void deleteProduct(UUID productId) {
        var product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> CatalogException.productNotFound(productId));
        var now = Instant.now();
        product.setDeletedAt(now);

        var variants = productVariantRepository.findByProductIdAndDeletedAtIsNull(productId);
        for (var variant : variants) {
            variant.setDeletedAt(now);
        }
        productRepository.save(product);
        productVariantRepository.saveAll(variants);
    }

    @Override
    public List<ProductDto> getProductsByCompany(UUID companyId) {
        return productRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(CatalogService::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryIdAndDeletedAtIsNull(categoryId).stream()
                .map(CatalogService::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> getProductsByBrand(UUID brandId) {
        return productRepository.findByBrandIdAndDeletedAtIsNull(brandId).stream()
                .map(CatalogService::toDto)
                .toList();
    }

    @Override
    public ProductVariantDto createVariant(UUID productId, String variantName, String barcode, Long priceOverrideMinor) {
        productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> CatalogException.productNotFound(productId));
        var variant = new ProductVariant(productId, variantName, barcode, priceOverrideMinor);
        variant = productVariantRepository.save(variant);
        return toDto(variant);
    }

    @Override
    public Optional<ProductVariantDto> getVariant(UUID variantId) {
        return productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
                .map(CatalogService::toDto);
    }

    @Override
    public ProductVariantDto updateVariant(UUID variantId, String variantName, String barcode, Long priceOverrideMinor) {
        var variant = productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
                .orElseThrow(() -> CatalogException.variantNotFound(variantId));
        if (variantName != null) {
            variant.setVariantName(variantName);
        }
        if (barcode != null) {
            variant.setBarcode(barcode);
        }
        if (priceOverrideMinor != null) {
            variant.setPriceOverrideMinor(priceOverrideMinor);
        }
        variant.markUpdated();
        variant = productVariantRepository.save(variant);
        return toDto(variant);
    }

    @Override
    public void deleteVariant(UUID variantId) {
        var variant = productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
                .orElseThrow(() -> CatalogException.variantNotFound(variantId));
        variant.setDeletedAt(Instant.now());
        productVariantRepository.save(variant);
    }

    @Override
    public List<ProductVariantDto> getVariantsByProduct(UUID productId) {
        return productVariantRepository.findByProductIdAndDeletedAtIsNull(productId).stream()
                .map(CatalogService::toDto)
                .toList();
    }

    static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getCompanyId(),
                category.getParentId(),
                category.getName()
        );
    }

    static BrandDto toDto(Brand brand) {
        return new BrandDto(
                brand.getId(),
                brand.getCompanyId(),
                brand.getName()
        );
    }

    static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getCompanyId(),
                product.getSku(),
                product.getName(),
                product.getCategoryId(),
                product.getBrandId(),
                product.getBasePriceMinor(),
                product.getCurrency(),
                product.getTaxClass(),
                product.isTrackInventory(),
                product.getCostingMethod().name(),
                product.isComposite(),
                product.isSoldByWeight(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    static ProductVariantDto toDto(ProductVariant variant) {
        return new ProductVariantDto(
                variant.getId(),
                variant.getProductId(),
                variant.getVariantName(),
                variant.getBarcode(),
                variant.getPriceOverrideMinor()
        );
    }
}
