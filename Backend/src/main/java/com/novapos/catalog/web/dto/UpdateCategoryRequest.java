package com.novapos.catalog.web.dto;

import java.util.UUID;

public record UpdateCategoryRequest(
        String name,
        UUID parentId
) {
}
