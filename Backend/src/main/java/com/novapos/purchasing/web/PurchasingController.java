package com.novapos.purchasing.web;

import com.novapos.purchasing.api.PurchasingFacade;
import com.novapos.purchasing.api.PurchasingFacade.POLineInput;
import com.novapos.purchasing.api.dto.PurchaseOrderDto;
import com.novapos.purchasing.api.dto.SupplierDto;
import com.novapos.purchasing.web.dto.CreatePORequest;
import com.novapos.purchasing.web.dto.CreateSupplierRequest;
import com.novapos.purchasing.web.dto.ReceivePOLineRequest;
import com.novapos.purchasing.web.dto.UpdateSupplierRequest;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
class PurchasingController {

    private final PurchasingFacade purchasingFacade;
    PurchasingController(PurchasingFacade f) { this.purchasingFacade = f; }

    @PostMapping("/companies/{companyId}/suppliers")
    ResponseEntity<SupplierDto> createSupplier(@PathVariable UUID companyId, @Valid @RequestBody CreateSupplierRequest r) {
        var s = purchasingFacade.createSupplier(companyId, r.name(), r.paymentTerms(), r.leadTimeDays());
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(s.id()).toUri();
        return ResponseEntity.created(uri).body(s);
    }
    @GetMapping("/suppliers/{supplierId}")
    ResponseEntity<SupplierDto> getSupplier(@PathVariable UUID supplierId) {
        return purchasingFacade.getSupplier(supplierId).map(ResponseEntity::ok).orElseThrow(() -> PurchasingException.supplierNotFound(supplierId));
    }
    @PutMapping("/suppliers/{supplierId}")
    ResponseEntity<SupplierDto> updateSupplier(@PathVariable UUID supplierId, @Valid @RequestBody UpdateSupplierRequest r) {
        return ResponseEntity.ok(purchasingFacade.updateSupplier(supplierId, r.name(), r.paymentTerms(), r.leadTimeDays()));
    }
    @DeleteMapping("/suppliers/{supplierId}")
    ResponseEntity<Void> deleteSupplier(@PathVariable UUID supplierId) {
        purchasingFacade.deleteSupplier(supplierId); return ResponseEntity.noContent().build();
    }
    @GetMapping("/companies/{companyId}/suppliers")
    ResponseEntity<List<SupplierDto>> getSuppliersByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(purchasingFacade.getSuppliersByCompany(companyId));
    }

    @PostMapping("/purchase-orders")
    ResponseEntity<PurchaseOrderDto> createPO(@Valid @RequestBody CreatePORequest r) {
        var lines = r.lines().stream().map(l -> new POLineInput(l.productVariantId(), l.quantityOrdered(), l.unitCostMinor())).toList();
        var po = purchasingFacade.createPurchaseOrder(r.supplierId(), r.branchId(), lines);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(po.id()).toUri();
        return ResponseEntity.created(uri).body(po);
    }
    @GetMapping("/purchase-orders/{poId}")
    ResponseEntity<PurchaseOrderDto> getPO(@PathVariable UUID poId) {
        return purchasingFacade.getPurchaseOrder(poId).map(ResponseEntity::ok).orElseThrow(() -> PurchasingException.poNotFound(poId));
    }
    @PutMapping("/purchase-orders/{poId}/approve")
    ResponseEntity<PurchaseOrderDto> approvePO(@PathVariable UUID poId) {
        return ResponseEntity.ok(purchasingFacade.approvePurchaseOrder(poId));
    }
    @PutMapping("/purchase-orders/{poId}/cancel")
    ResponseEntity<Void> cancelPO(@PathVariable UUID poId) {
        purchasingFacade.cancelPurchaseOrder(poId); return ResponseEntity.noContent().build();
    }
    @PutMapping("/po-lines/{poLineId}/receive")
    ResponseEntity<PurchaseOrderDto> receiveLine(@PathVariable UUID poLineId, @Valid @RequestBody ReceivePOLineRequest r) {
        return ResponseEntity.ok(purchasingFacade.receivePurchaseOrderLine(poLineId, r.quantityReceived()));
    }
}
