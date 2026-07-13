package com.novapos.purchasing.service;

import com.novapos.purchasing.api.PurchasingFacade;
import com.novapos.purchasing.api.dto.POLineDto;
import com.novapos.purchasing.api.dto.PurchaseOrderDto;
import com.novapos.purchasing.api.dto.SupplierDto;
import com.novapos.purchasing.domain.PurchaseOrder;
import com.novapos.purchasing.domain.PurchaseOrderLine;
import com.novapos.purchasing.domain.PurchaseOrderStatus;
import com.novapos.purchasing.domain.Supplier;
import com.novapos.purchasing.repository.PurchaseOrderLineRepository;
import com.novapos.purchasing.repository.PurchaseOrderRepository;
import com.novapos.purchasing.repository.SupplierRepository;
import com.novapos.purchasing.web.PurchasingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class PurchasingService implements PurchasingFacade {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderLineRepository poLineRepository;

    PurchasingService(SupplierRepository sr, PurchaseOrderRepository pr, PurchaseOrderLineRepository lr) {
        this.supplierRepository = sr; this.poRepository = pr; this.poLineRepository = lr;
    }

    @Override
    public SupplierDto createSupplier(UUID companyId, String name, String paymentTerms, Integer leadTimeDays) {
        var s = new Supplier(companyId, name, paymentTerms, leadTimeDays);
        return toDto(supplierRepository.save(s));
    }
    @Override
    public Optional<SupplierDto> getSupplier(UUID supplierId) {
        return supplierRepository.findByIdAndDeletedAtIsNull(supplierId).map(PurchasingService::toDto);
    }
    @Override
    public SupplierDto updateSupplier(UUID id, String name, String paymentTerms, Integer leadTimeDays) {
        var s = supplierRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> PurchasingException.supplierNotFound(id));
        if (name != null) s.setName(name);
        if (paymentTerms != null) s.setPaymentTerms(paymentTerms);
        if (leadTimeDays != null) s.setLeadTimeDays(leadTimeDays);
        s.markUpdated();
        return toDto(supplierRepository.save(s));
    }
    @Override
    public void deleteSupplier(UUID id) {
        var s = supplierRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> PurchasingException.supplierNotFound(id));
        s.setDeletedAt(Instant.now());
        supplierRepository.save(s);
    }
    @Override
    public List<SupplierDto> getSuppliersByCompany(UUID companyId) {
        return supplierRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream().map(PurchasingService::toDto).toList();
    }

    @Override
    public PurchaseOrderDto createPurchaseOrder(UUID supplierId, UUID branchId, List<POLineInput> lines) {
        supplierRepository.findByIdAndDeletedAtIsNull(supplierId).orElseThrow(() -> PurchasingException.supplierNotFound(supplierId));
        var po = new PurchaseOrder(supplierId, branchId);
        po = poRepository.save(po);
        for (var l : lines) {
            poLineRepository.save(new PurchaseOrderLine(po.getId(), l.productVariantId(), l.quantityOrdered(), l.unitCostMinor()));
        }
        return getPurchaseOrder(po.getId()).orElseThrow();
    }
    @Override
    public Optional<PurchaseOrderDto> getPurchaseOrder(UUID poId) {
        return poRepository.findById(poId).map(po -> {
            var lines = poLineRepository.findByPurchaseOrderId(poId);
            return toDto(po, lines);
        });
    }
    @Override
    public PurchaseOrderDto approvePurchaseOrder(UUID poId) {
        var po = poRepository.findById(poId).orElseThrow(() -> PurchasingException.poNotFound(poId));
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) throw PurchasingException.poNotDraft(poId);
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.markUpdated();
        po = poRepository.save(po);
        var lines = poLineRepository.findByPurchaseOrderId(poId);
        return toDto(po, lines);
    }
    @Override
    public PurchaseOrderDto receivePurchaseOrderLine(UUID poLineId, BigDecimal quantityReceived) {
        var pol = poLineRepository.findById(poLineId).orElseThrow(() -> PurchasingException.poLineNotFound(poLineId));
        pol.setQuantityReceived(pol.getQuantityReceived().add(quantityReceived));
        poLineRepository.save(pol);

        var po = poRepository.findById(pol.getPurchaseOrderId()).orElseThrow(() -> PurchasingException.poNotFound(pol.getPurchaseOrderId()));
        var lines = poLineRepository.findByPurchaseOrderId(po.getId());
        boolean allFullyReceived = lines.stream().allMatch(l -> l.getQuantityReceived().compareTo(l.getQuantityOrdered()) >= 0);
        boolean anyReceived = lines.stream().anyMatch(l -> l.getQuantityReceived().compareTo(BigDecimal.ZERO) > 0);
        if (allFullyReceived) po.setStatus(PurchaseOrderStatus.RECEIVED);
        else if (anyReceived) po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        po.markUpdated();
        po = poRepository.save(po);
        return toDto(po, lines);
    }
    @Override
    public void cancelPurchaseOrder(UUID poId) {
        var po = poRepository.findById(poId).orElseThrow(() -> PurchasingException.poNotFound(poId));
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        po.markUpdated();
        poRepository.save(po);
    }

    static SupplierDto toDto(Supplier s) { return new SupplierDto(s.getId(), s.getCompanyId(), s.getName(), s.getPaymentTerms(), s.getLeadTimeDays()); }
    static PurchaseOrderDto toDto(PurchaseOrder po, List<PurchaseOrderLine> lines) {
        return new PurchaseOrderDto(po.getId(), po.getSupplierId(), po.getBranchId(), po.getStatus().name(),
                lines.stream().map(PurchasingService::toDto).toList());
    }
    static POLineDto toDto(PurchaseOrderLine l) { return new POLineDto(l.getId(), l.getPurchaseOrderId(), l.getProductVariantId(), l.getQuantityOrdered(), l.getQuantityReceived(), l.getUnitCostMinor()); }
}
