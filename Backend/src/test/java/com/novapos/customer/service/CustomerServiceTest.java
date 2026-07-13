package com.novapos.customer.service;

import com.novapos.customer.repository.*; import com.novapos.customer.web.CustomerException;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*; import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.verify; import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock private CustomerRepository cr; @Mock private LoyaltyLedgerRepository llr; @Mock private StoreCreditLedgerRepository sclr;
    @InjectMocks private CustomerService cs;

    @Test @DisplayName("Redeem with insufficient points throws")
    void redeemInsufficientPoints() {
        var id = UUID.randomUUID();
        when(cr.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(new com.novapos.customer.domain.Customer(UUID.randomUUID(), "Test", null, null)));
        when(llr.sumPointsDelta(id)).thenReturn(0);
        assertThatThrownBy(() -> cs.redeemPoints(id, 100, null)).isInstanceOf(CustomerException.class).hasFieldOrPropertyWithValue("code", "INSUFFICIENT_POINTS");
    }

    @Test @DisplayName("Accrue points succeeds")
    void accruePointsSuccess() {
        var id = UUID.randomUUID();
        when(cr.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(new com.novapos.customer.domain.Customer(UUID.randomUUID(), "Test", null, null)));
        cs.accruePoints(id, 50, "ACCRUAL", null);
        verify(llr).save(any());
    }
}
