package com.novapos.pos.service;

import com.novapos.pos.domain.Payment;
import com.novapos.pos.domain.PaymentMethod;
import com.novapos.pos.domain.Sale;
import com.novapos.pos.domain.SaleLine;
import com.novapos.pos.domain.SaleStatus;
import com.novapos.pos.repository.PaymentRepository;
import com.novapos.pos.repository.ReturnLineRepository;
import com.novapos.pos.repository.SaleLineRepository;
import com.novapos.pos.repository.SaleRepository;
import com.novapos.pos.web.PosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PosServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleLineRepository saleLineRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ReturnLineRepository returnLineRepository;

    @InjectMocks
    private PosService posService;

    @Test
    @DisplayName("Create sale succeeds")
    void createSaleSuccess() {
        var branchId = UUID.randomUUID();
        var cashierId = UUID.randomUUID();
        var clientUuid = UUID.randomUUID();

        when(saleRepository.findByClientUuid(clientUuid)).thenReturn(Optional.empty());
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = posService.createSale(branchId, cashierId, null, "USD", clientUuid);

        assertThat(dto.status()).isEqualTo("OPEN");
        assertThat(dto.branchId()).isEqualTo(branchId);
        assertThat(dto.clientUuid()).isEqualTo(clientUuid);
    }

    @Test
    @DisplayName("Complete sale with matching payments succeeds")
    void completeSaleWithMatchingPayments() {
        var saleId = UUID.randomUUID();
        var sale = new Sale(UUID.randomUUID(), UUID.randomUUID(), null, "USD", UUID.randomUUID());
        sale.setTotalMinor(2000L);

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(paymentRepository.findBySaleId(saleId)).thenReturn(List.of(
                new Payment(saleId, PaymentMethod.CASH, 1000L, null),
                new Payment(saleId, PaymentMethod.CARD, 1000L, null)
        ));
        when(saleLineRepository.findBySaleId(saleId)).thenReturn(List.of());
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        var result = posService.completeSale(saleId);

        assertThat(result.status()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Complete sale with mismatched payments throws")
    void completeSalePaymentMismatch() {
        var saleId = UUID.randomUUID();
        var sale = new Sale(UUID.randomUUID(), UUID.randomUUID(), null, "USD", UUID.randomUUID());
        sale.setTotalMinor(2000L);

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(paymentRepository.findBySaleId(saleId)).thenReturn(List.of(
                new Payment(saleId, PaymentMethod.CASH, 500L, null)
        ));

        assertThatThrownBy(() -> posService.completeSale(saleId))
                .isInstanceOf(PosException.class)
                .hasFieldOrPropertyWithValue("code", "PAYMENT_MISMATCH");
    }

    @Test
    @DisplayName("Hold and resume sale")
    void holdAndResumeSale() {
        var saleId = UUID.randomUUID();
        var sale = new Sale(UUID.randomUUID(), UUID.randomUUID(), null, "USD", UUID.randomUUID());

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleLineRepository.findBySaleId(saleId)).thenReturn(List.of());
        when(paymentRepository.findBySaleId(saleId)).thenReturn(List.of());
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        var held = posService.holdSale(saleId);
        assertThat(held.status()).isEqualTo("HELD");

        sale.setStatus(SaleStatus.HELD);
        var resumed = posService.resumeSale(saleId);
        assertThat(resumed.status()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("Duplicate client UUID throws conflict")
    void duplicateClientUuid() {
        var clientUuid = UUID.randomUUID();
        when(saleRepository.findByClientUuid(clientUuid))
                .thenReturn(Optional.of(new Sale(UUID.randomUUID(), UUID.randomUUID(), null, "USD", clientUuid)));

        assertThatThrownBy(() -> posService.createSale(UUID.randomUUID(), UUID.randomUUID(), null, "USD", clientUuid))
                .isInstanceOf(PosException.class)
                .hasFieldOrPropertyWithValue("code", "DUPLICATE_CLIENT_UUID");
    }
}
