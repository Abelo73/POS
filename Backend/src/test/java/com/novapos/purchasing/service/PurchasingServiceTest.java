package com.novapos.purchasing.service;

import com.novapos.purchasing.domain.PurchaseOrder;
import com.novapos.purchasing.domain.PurchaseOrderStatus;
import com.novapos.purchasing.domain.Supplier;
import com.novapos.purchasing.repository.PurchaseOrderLineRepository;
import com.novapos.purchasing.repository.PurchaseOrderRepository;
import com.novapos.purchasing.repository.SupplierRepository;
import com.novapos.purchasing.web.PurchasingException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchasingServiceTest {

    @Mock private SupplierRepository supplierRepository;
    @Mock private PurchaseOrderRepository poRepository;
    @Mock private PurchaseOrderLineRepository poLineRepository;
    @InjectMocks private PurchasingService purchasingService;

    @Test @DisplayName("Create supplier")
    void createSupplier() {
        var companyId = UUID.randomUUID();
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var dto = purchasingService.createSupplier(companyId, "Acme", "NET30", 7);
        assertThat(dto.name()).isEqualTo("Acme");
        assertThat(dto.paymentTerms()).isEqualTo("NET30");
    }

    @Test @DisplayName("Approve PO in draft status")
    void approveDraftPO() {
        var poId = UUID.randomUUID();
        var po = new PurchaseOrder(UUID.randomUUID(), UUID.randomUUID());
        when(poRepository.findById(poId)).thenReturn(Optional.of(po));
        when(poLineRepository.findByPurchaseOrderId(poId)).thenReturn(java.util.List.of());
        when(poRepository.save(any())).thenReturn(po);
        var dto = purchasingService.approvePurchaseOrder(poId);
        assertThat(dto.status()).isEqualTo("APPROVED");
    }

    @Test @DisplayName("Approve non-draft PO throws")
    void approveNonDraftThrows() {
        var poId = UUID.randomUUID();
        var po = new PurchaseOrder(UUID.randomUUID(), UUID.randomUUID());
        po.setStatus(PurchaseOrderStatus.APPROVED);
        when(poRepository.findById(poId)).thenReturn(Optional.of(po));
        assertThatThrownBy(() -> purchasingService.approvePurchaseOrder(poId))
                .isInstanceOf(PurchasingException.class).hasFieldOrPropertyWithValue("code", "PO_NOT_DRAFT");
    }

    @Test @DisplayName("Delete supplier soft-deletes")
    void deleteSupplier() {
        var id = UUID.randomUUID();
        var s = new Supplier(UUID.randomUUID(), "Acme", null, null);
        when(supplierRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(s));
        purchasingService.deleteSupplier(id);
        assertThat(s.getDeletedAt()).isNotNull();
    }
}
