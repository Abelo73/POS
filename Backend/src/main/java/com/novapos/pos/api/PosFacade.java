package com.novapos.pos.api;

import com.novapos.pos.api.dto.PaymentDto;
import com.novapos.pos.api.dto.ReturnLineDto;
import com.novapos.pos.api.dto.SaleDto;
import com.novapos.pos.api.dto.SaleLineDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PosFacade {

    SaleDto createSale(UUID branchId, UUID cashierId, UUID customerId, String currency, UUID clientUuid);

    Optional<SaleDto> getSale(UUID saleId);

    Optional<SaleDto> getSaleByClientUuid(UUID clientUuid);

    SaleLineDto addSaleLine(UUID saleId, UUID productVariantId, BigDecimal quantity, long unitPriceMinor);

    void removeSaleLine(UUID saleLineId);

    SaleLineDto updateSaleLineQuantity(UUID saleLineId, BigDecimal quantity);

    PaymentDto addPayment(UUID saleId, String method, long amountMinor, String reference);

    SaleDto completeSale(UUID saleId);

    SaleDto holdSale(UUID saleId);

    SaleDto resumeSale(UUID saleId);

    void voidSale(UUID saleId);

    List<ReturnLineDto> createReturn(UUID saleId, List<ReturnItemInput> items);

    record ReturnItemInput(UUID saleLineId, BigDecimal quantity, String refundMethod) {}
}
