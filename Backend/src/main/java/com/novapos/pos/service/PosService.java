package com.novapos.pos.service;

import com.novapos.pos.api.PosFacade;
import com.novapos.pos.api.dto.PaymentDto;
import com.novapos.pos.api.dto.ReturnLineDto;
import com.novapos.pos.api.dto.SaleDto;
import com.novapos.pos.api.dto.SaleLineDto;
import com.novapos.pos.domain.Payment;
import com.novapos.pos.domain.PaymentMethod;
import com.novapos.pos.domain.ReturnLine;
import com.novapos.pos.domain.Sale;
import com.novapos.pos.domain.SaleLine;
import com.novapos.pos.domain.SaleStatus;
import com.novapos.pos.repository.PaymentRepository;
import com.novapos.pos.repository.ReturnLineRepository;
import com.novapos.pos.repository.SaleLineRepository;
import com.novapos.pos.repository.SaleRepository;
import com.novapos.pos.web.PosException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class PosService implements PosFacade {

    private final SaleRepository saleRepository;
    private final SaleLineRepository saleLineRepository;
    private final PaymentRepository paymentRepository;
    private final ReturnLineRepository returnLineRepository;

    PosService(SaleRepository saleRepository, SaleLineRepository saleLineRepository,
               PaymentRepository paymentRepository, ReturnLineRepository returnLineRepository) {
        this.saleRepository = saleRepository;
        this.saleLineRepository = saleLineRepository;
        this.paymentRepository = paymentRepository;
        this.returnLineRepository = returnLineRepository;
    }

    @Override
    public SaleDto createSale(UUID branchId, UUID cashierId, UUID customerId, String currency, UUID clientUuid) {
        if (saleRepository.findByClientUuid(clientUuid).isPresent()) {
            throw PosException.duplicateClientUuid(clientUuid);
        }
        var sale = new Sale(branchId, cashierId, customerId, currency, clientUuid);
        sale = saleRepository.save(sale);
        return toDto(sale, List.of(), List.of());
    }

    @Override
    public Optional<SaleDto> getSale(UUID saleId) {
        return saleRepository.findById(saleId).map(sale -> {
            var lines = saleLineRepository.findBySaleId(saleId);
            var payments = paymentRepository.findBySaleId(saleId);
            return toDto(sale, lines, payments);
        });
    }

    @Override
    public Optional<SaleDto> getSaleByClientUuid(UUID clientUuid) {
        return saleRepository.findByClientUuid(clientUuid).map(sale -> {
            var lines = saleLineRepository.findBySaleId(sale.getId());
            var payments = paymentRepository.findBySaleId(sale.getId());
            return toDto(sale, lines, payments);
        });
    }

    @Override
    public SaleLineDto addSaleLine(UUID saleId, UUID productVariantId, BigDecimal quantity, long unitPriceMinor) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.OPEN) {
            throw PosException.saleNotOpen(saleId);
        }

        var line = new SaleLine(saleId, productVariantId, quantity, unitPriceMinor);
        line = saleLineRepository.save(line);

        recalculateSaleTotals(sale);
        return toDto(line);
    }

    @Override
    public void removeSaleLine(UUID saleLineId) {
        saleLineRepository.deleteById(saleLineId);
        var line = saleLineRepository.findById(saleLineId);
        if (line.isPresent()) {
            var sale = saleRepository.findById(line.get().getSaleId()).orElse(null);
            if (sale != null) recalculateSaleTotals(sale);
        }
    }

    @Override
    public SaleLineDto updateSaleLineQuantity(UUID saleLineId, BigDecimal quantity) {
        var line = saleLineRepository.findById(saleLineId)
                .orElseThrow(() -> PosException.saleLineNotFound(saleLineId));
        line.setQuantity(quantity);
        line = saleLineRepository.save(line);
        var sale = saleRepository.findById(line.getSaleId()).orElse(null);
        if (sale != null) recalculateSaleTotals(sale);
        return toDto(line);
    }

    @Override
    public PaymentDto addPayment(UUID saleId, String method, long amountMinor, String reference) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.OPEN) {
            throw PosException.saleNotOpen(saleId);
        }
        var payment = new Payment(saleId, PaymentMethod.valueOf(method.toUpperCase()), amountMinor, reference);
        payment = paymentRepository.save(payment);
        return toDto(payment);
    }

    @Override
    public SaleDto completeSale(UUID saleId) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.OPEN) {
            throw PosException.saleNotOpen(saleId);
        }

        var payments = paymentRepository.findBySaleId(saleId);
        long totalPaid = payments.stream().mapToLong(Payment::getAmountMinor).sum();
        if (totalPaid != sale.getTotalMinor()) {
            throw PosException.paymentMismatch(sale.getTotalMinor(), totalPaid);
        }

        sale.setStatus(SaleStatus.COMPLETED);
        sale.setCompletedAt(Instant.now());
        sale.markUpdated();
        sale = saleRepository.save(sale);

        var lines = saleLineRepository.findBySaleId(saleId);
        return toDto(sale, lines, payments);
    }

    @Override
    public SaleDto holdSale(UUID saleId) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.OPEN) {
            throw PosException.saleNotOpen(saleId);
        }
        sale.setStatus(SaleStatus.HELD);
        sale.markUpdated();
        sale = saleRepository.save(sale);
        var lines = saleLineRepository.findBySaleId(saleId);
        var payments = paymentRepository.findBySaleId(saleId);
        return toDto(sale, lines, payments);
    }

    @Override
    public SaleDto resumeSale(UUID saleId) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.HELD) {
            throw PosException.saleNotHeld(saleId);
        }
        sale.setStatus(SaleStatus.OPEN);
        sale.markUpdated();
        sale = saleRepository.save(sale);
        var lines = saleLineRepository.findBySaleId(saleId);
        var payments = paymentRepository.findBySaleId(saleId);
        return toDto(sale, lines, payments);
    }

    @Override
    public void voidSale(UUID saleId) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.OPEN && sale.getStatus() != SaleStatus.HELD) {
            throw PosException.cannotVoid(saleId);
        }
        sale.setStatus(SaleStatus.VOIDED);
        sale.markUpdated();
        saleRepository.save(sale);
    }

    @Override
    public List<ReturnLineDto> createReturn(UUID saleId, List<ReturnItemInput> items) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> PosException.saleNotFound(saleId));
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw PosException.saleNotCompleted(saleId);
        }

        var result = new ArrayList<ReturnLineDto>();
        for (var item : items) {
            var originalLine = saleLineRepository.findById(item.saleLineId())
                    .orElseThrow(() -> PosException.saleLineNotFound(item.saleLineId()));
            if (!originalLine.getSaleId().equals(saleId)) {
                throw PosException.lineNotInSale(item.saleLineId(), saleId);
            }

            var alreadyReturned = returnLineRepository.findByOriginalSaleLineId(item.saleLineId())
                    .stream().map(rl -> rl.getQuantity()).reduce(BigDecimal.ZERO, BigDecimal::add);
            var availableForReturn = originalLine.getQuantity().subtract(alreadyReturned);
            if (item.quantity().compareTo(availableForReturn) > 0) {
                throw PosException.excessiveReturn(item.saleLineId(), item.quantity(), availableForReturn);
            }

            var returnLine = new ReturnLine(item.saleLineId(), saleId, item.quantity(), item.refundMethod());
            returnLine = returnLineRepository.save(returnLine);
            result.add(toDto(returnLine));
        }

        sale.setStatus(SaleStatus.REFUNDED);
        sale.markUpdated();
        saleRepository.save(sale);

        return result;
    }

    private void recalculateSaleTotals(Sale sale) {
        var lines = saleLineRepository.findBySaleId(sale.getId());
        long subtotal = 0;
        long discount = 0;
        long tax = 0;
        for (var line : lines) {
            long lineTotal = line.getUnitPriceMinor() * line.getQuantity().longValue();
            subtotal += lineTotal;
            discount += line.getDiscountMinor();
            tax += line.getTaxMinor();
        }
        sale.recalculateTotals(subtotal, discount, tax);
        sale.markUpdated();
        saleRepository.save(sale);
    }

    static SaleDto toDto(Sale sale, List<SaleLine> lines, List<Payment> payments) {
        return new SaleDto(
                sale.getId(), sale.getBranchId(), sale.getCustomerId(), sale.getCashierId(),
                sale.getStatus().name(), sale.getSubtotalMinor(), sale.getDiscountMinor(),
                sale.getTaxMinor(), sale.getTotalMinor(), sale.getCurrency(), sale.getClientUuid(),
                sale.getCompletedAt(), sale.getCreatedAt(),
                lines.stream().map(PosService::toDto).toList(),
                payments.stream().map(PosService::toDto).toList()
        );
    }

    static SaleLineDto toDto(SaleLine line) {
        return new SaleLineDto(line.getId(), line.getSaleId(), line.getProductVariantId(),
                line.getQuantity(), line.getUnitPriceMinor(), line.getDiscountMinor(), line.getTaxMinor());
    }

    static PaymentDto toDto(Payment payment) {
        return new PaymentDto(payment.getId(), payment.getSaleId(), payment.getMethod().name(),
                payment.getAmountMinor(), payment.getReference(), payment.getCreatedAt());
    }

    static ReturnLineDto toDto(ReturnLine line) {
        return new ReturnLineDto(line.getId(), line.getOriginalSaleLineId(),
                line.getReturnSaleId(), line.getQuantity(), line.getRefundMethod());
    }
}
