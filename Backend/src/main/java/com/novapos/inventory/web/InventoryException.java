package com.novapos.inventory.web;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InventoryException extends NovaPosException {

    public InventoryException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    public static InventoryException invalidReason(String reason) {
        return new InventoryException("INVALID_REASON", "Invalid movement reason: " + reason, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static InventoryException transferNotFound(UUID transferOrderId) {
        return new InventoryException("TRANSFER_NOT_FOUND", "Transfer order not found: " + transferOrderId, HttpStatus.NOT_FOUND);
    }

    public static InventoryException stockCountNotFound(UUID stockCountId) {
        return new InventoryException("STOCK_COUNT_NOT_FOUND", "Stock count not found: " + stockCountId, HttpStatus.NOT_FOUND);
    }

    public static InventoryException countLineNotFound(UUID lineId) {
        return new InventoryException("COUNT_LINE_NOT_FOUND", "Stock count line not found: " + lineId, HttpStatus.NOT_FOUND);
    }

    public static InventoryException invalidCountStatus(String status) {
        return new InventoryException("INVALID_COUNT_STATUS", "Invalid stock count status for this operation: " + status, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static InventoryException negativeStockNotAllowed(UUID productVariantId, UUID locationId) {
        return new InventoryException("NEGATIVE_STOCK_NOT_ALLOWED", "Negative stock not allowed for variant " + productVariantId + " at location " + locationId, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
