package com.novapos.purchasing.web;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class PurchasingException extends NovaPosException {
    public PurchasingException(String code, String message, HttpStatus status) { super(code, message, status); }
    public static PurchasingException supplierNotFound(UUID id) { return new PurchasingException("SUPPLIER_NOT_FOUND", "Supplier not found: " + id, HttpStatus.NOT_FOUND); }
    public static PurchasingException poNotFound(UUID id) { return new PurchasingException("PO_NOT_FOUND", "Purchase order not found: " + id, HttpStatus.NOT_FOUND); }
    public static PurchasingException poLineNotFound(UUID id) { return new PurchasingException("PO_LINE_NOT_FOUND", "PO line not found: " + id, HttpStatus.NOT_FOUND); }
    public static PurchasingException poNotDraft(UUID id) { return new PurchasingException("PO_NOT_DRAFT", "Only draft POs can be approved: " + id, HttpStatus.UNPROCESSABLE_ENTITY); }
}
