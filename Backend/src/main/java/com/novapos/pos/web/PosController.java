package com.novapos.pos.web;

import com.novapos.pos.api.PosFacade;
import com.novapos.pos.api.PosFacade.ReturnItemInput;
import com.novapos.pos.api.dto.PaymentDto;
import com.novapos.pos.api.dto.ReturnLineDto;
import com.novapos.pos.api.dto.SaleDto;
import com.novapos.pos.api.dto.SaleLineDto;
import com.novapos.pos.web.dto.AddPaymentRequest;
import com.novapos.pos.web.dto.AddSaleLineRequest;
import com.novapos.pos.web.dto.CreateReturnRequest;
import com.novapos.pos.web.dto.CreateSaleRequest;
import com.novapos.pos.web.dto.UpdateQuantityRequest;
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
@RequestMapping("/api/v1/sales")
class PosController {

    private final PosFacade posFacade;

    PosController(PosFacade posFacade) {
        this.posFacade = posFacade;
    }

    @PostMapping
    ResponseEntity<SaleDto> createSale(@Valid @RequestBody CreateSaleRequest request) {
        var sale = posFacade.createSale(request.branchId(), request.cashierId(),
                request.customerId(), request.currency(), request.clientUuid());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(sale.id()).toUri();
        return ResponseEntity.created(location).body(sale);
    }

    @GetMapping("/{saleId}")
    ResponseEntity<SaleDto> getSale(@PathVariable UUID saleId) {
        return posFacade.getSale(saleId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
    }

    @GetMapping("/by-client/{clientUuid}")
    ResponseEntity<SaleDto> getSaleByClientUuid(@PathVariable UUID clientUuid) {
        return posFacade.getSaleByClientUuid(clientUuid)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> PosException.saleNotFound(clientUuid));
    }

    @PostMapping("/{saleId}/lines")
    ResponseEntity<SaleLineDto> addSaleLine(@PathVariable UUID saleId, @Valid @RequestBody AddSaleLineRequest request) {
        var line = posFacade.addSaleLine(saleId, request.productVariantId(),
                request.quantity(), request.unitPriceMinor());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(line.id()).toUri();
        return ResponseEntity.created(location).body(line);
    }

    @DeleteMapping("/lines/{saleLineId}")
    ResponseEntity<Void> removeSaleLine(@PathVariable UUID saleLineId) {
        posFacade.removeSaleLine(saleLineId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/lines/{saleLineId}/quantity")
    ResponseEntity<SaleLineDto> updateQuantity(@PathVariable UUID saleLineId,
                                                @Valid @RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok(posFacade.updateSaleLineQuantity(saleLineId, request.quantity()));
    }

    @PostMapping("/{saleId}/payments")
    ResponseEntity<PaymentDto> addPayment(@PathVariable UUID saleId, @Valid @RequestBody AddPaymentRequest request) {
        var payment = posFacade.addPayment(saleId, request.method(), request.amountMinor(), request.reference());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(payment.id()).toUri();
        return ResponseEntity.created(location).body(payment);
    }

    @PutMapping("/{saleId}/complete")
    ResponseEntity<SaleDto> completeSale(@PathVariable UUID saleId) {
        return ResponseEntity.ok(posFacade.completeSale(saleId));
    }

    @PutMapping("/{saleId}/hold")
    ResponseEntity<SaleDto> holdSale(@PathVariable UUID saleId) {
        return ResponseEntity.ok(posFacade.holdSale(saleId));
    }

    @PutMapping("/{saleId}/resume")
    ResponseEntity<SaleDto> resumeSale(@PathVariable UUID saleId) {
        return ResponseEntity.ok(posFacade.resumeSale(saleId));
    }

    @PutMapping("/{saleId}/void")
    ResponseEntity<Void> voidSale(@PathVariable UUID saleId) {
        posFacade.voidSale(saleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{saleId}/returns")
    ResponseEntity<List<ReturnLineDto>> createReturn(@PathVariable UUID saleId,
                                                      @Valid @RequestBody CreateReturnRequest request) {
        var items = request.items().stream()
                .map(i -> new ReturnItemInput(i.saleLineId(), i.quantity(), i.refundMethod()))
                .toList();
        return ResponseEntity.ok(posFacade.createReturn(saleId, items));
    }
}
