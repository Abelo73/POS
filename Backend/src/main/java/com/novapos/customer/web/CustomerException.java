package com.novapos.customer.web;
import com.novapos.shared.exception.NovaPosException; import org.springframework.http.HttpStatus; import java.util.UUID;
public class CustomerException extends NovaPosException {
    public CustomerException(String code, String message, HttpStatus status) { super(code, message, status); }
    public static CustomerException notFound(UUID id) { return new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found: " + id, HttpStatus.NOT_FOUND); }
    public static CustomerException insufficientPoints(UUID id, int req, int bal) { return new CustomerException("INSUFFICIENT_POINTS", "Insufficient points for customer " + id + ": requested " + req + ", balance " + bal, HttpStatus.UNPROCESSABLE_ENTITY); }
    public static CustomerException insufficientCredit(UUID id, long req, long bal) { return new CustomerException("INSUFFICIENT_CREDIT", "Insufficient store credit for customer " + id + ": requested " + req + ", balance " + bal, HttpStatus.UNPROCESSABLE_ENTITY); }
}
