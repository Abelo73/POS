package com.novapos.pos.web;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class PosException extends NovaPosException {

    public PosException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    public static PosException saleNotFound(UUID saleId) {
        return new PosException("SALE_NOT_FOUND", "Sale not found: " + saleId, HttpStatus.NOT_FOUND);
    }

    public static PosException saleLineNotFound(UUID saleLineId) {
        return new PosException("SALE_LINE_NOT_FOUND", "Sale line not found: " + saleLineId, HttpStatus.NOT_FOUND);
    }

    public static PosException saleNotOpen(UUID saleId) {
        return new PosException("SALE_NOT_OPEN", "Sale is not open: " + saleId, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static PosException saleNotHeld(UUID saleId) {
        return new PosException("SALE_NOT_HELD", "Sale is not held: " + saleId, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static PosException cannotVoid(UUID saleId) {
        return new PosException("CANNOT_VOID", "Only open or held sales can be voided: " + saleId, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static PosException saleNotCompleted(UUID saleId) {
        return new PosException("SALE_NOT_COMPLETED", "Sale is not completed: " + saleId, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static PosException paymentMismatch(long expected, long actual) {
        return new PosException("PAYMENT_MISMATCH",
                "Payment total (" + actual + ") does not match sale total (" + expected + ")",
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static PosException duplicateClientUuid(UUID clientUuid) {
        return new PosException("DUPLICATE_CLIENT_UUID", "A sale with this client UUID already exists: " + clientUuid, HttpStatus.CONFLICT);
    }

    public static PosException excessiveReturn(UUID lineId, BigDecimal requested, BigDecimal available) {
        return new PosException("EXCESSIVE_RETURN",
                "Cannot return " + requested + " units from line " + lineId + " (only " + available + " available)",
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static PosException lineNotInSale(UUID lineId, UUID saleId) {
        return new PosException("LINE_NOT_IN_SALE", "Line " + lineId + " does not belong to sale " + saleId, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
