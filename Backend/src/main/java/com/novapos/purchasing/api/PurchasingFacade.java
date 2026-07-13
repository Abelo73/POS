package com.novapos.purchasing.api;

import com.novapos.purchasing.api.dto.POLineDto;
import com.novapos.purchasing.api.dto.PurchaseOrderDto;
import com.novapos.purchasing.api.dto.SupplierDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchasingFacade {

    SupplierDto createSupplier(UUID companyId, String name, String paymentTerms, Integer leadTimeDays);
    Optional<SupplierDto> getSupplier(UUID supplierId);
    SupplierDto updateSupplier(UUID supplierId, String name, String paymentTerms, Integer leadTimeDays);
    void deleteSupplier(UUID supplierId);
    List<SupplierDto> getSuppliersByCompany(UUID companyId);

    PurchaseOrderDto createPurchaseOrder(UUID supplierId, UUID branchId, List<POLineInput> lines);
    Optional<PurchaseOrderDto> getPurchaseOrder(UUID poId);
    PurchaseOrderDto approvePurchaseOrder(UUID poId);
    PurchaseOrderDto receivePurchaseOrderLine(UUID poLineId, BigDecimal quantityReceived);
    void cancelPurchaseOrder(UUID poId);

    record POLineInput(UUID productVariantId, BigDecimal quantityOrdered, long unitCostMinor) {}
}
