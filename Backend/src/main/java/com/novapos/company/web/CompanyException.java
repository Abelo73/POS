package com.novapos.company.web;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CompanyException extends NovaPosException {

    public CompanyException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    public static CompanyException notFound(UUID companyId) {
        return new CompanyException("COMPANY_NOT_FOUND", "Company not found: " + companyId, HttpStatus.NOT_FOUND);
    }

    public static CompanyException branchNotFound(UUID branchId) {
        return new CompanyException("BRANCH_NOT_FOUND", "Branch not found: " + branchId, HttpStatus.NOT_FOUND);
    }

    public static CompanyException lastBranch() {
        return new CompanyException(
                "LAST_BRANCH",
                "Cannot delete the last branch of a company. A company must have at least one branch.",
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
