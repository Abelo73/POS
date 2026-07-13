package com.novapos.inventory.api;

import com.novapos.inventory.api.dto.BatchDto;
import com.novapos.inventory.api.dto.BranchInventoryConfigDto;
import com.novapos.inventory.api.dto.CostConsumptionDto;
import com.novapos.inventory.api.dto.ReorderConfigDto;
import com.novapos.inventory.api.dto.StockCountDto;
import com.novapos.inventory.api.dto.StockMovementDto;
import com.novapos.inventory.api.dto.TransferOrderDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryFacade {

    StockMovementDto recordMovement(UUID productVariantId, UUID locationId, int quantityDelta,
                                     String reason, Long unitCostMinor, UUID batchId,
                                     String referenceType, UUID referenceId, UUID createdBy);

    int getOnHandQuantity(UUID productVariantId, UUID locationId);

    List<StockMovementDto> getMovements(UUID productVariantId, UUID locationId);

    void recordReceipt(UUID productVariantId, UUID locationId, int quantity,
                        long unitCostMinor, String batchCode, LocalDate expiryDate);

    CostConsumptionDto consumeFifo(UUID productVariantId, UUID locationId, int quantity);

    CostConsumptionDto consumeLifo(UUID productVariantId, UUID locationId, int quantity);

    BatchDto createBatch(UUID productVariantId, String batchCode, LocalDate expiryDate);

    List<BatchDto> getBatches(UUID productVariantId);

    List<BatchDto> findBatchesExpiringBefore(LocalDate date, UUID productVariantId);

    ReorderConfigDto upsertReorderConfig(UUID productVariantId, UUID locationId, int reorderLevel, int reorderQuantity);

    Optional<ReorderConfigDto> getReorderConfig(UUID productVariantId, UUID locationId);

    TransferOrderDto createTransfer(UUID sourceLocationId, UUID destinationLocationId,
                                     List<TransferLineInput> lines);

    Optional<TransferOrderDto> getTransfer(UUID transferOrderId);

    TransferOrderDto advanceTransferStatus(UUID transferOrderId, String targetStatus);

    StockCountDto createStockCount(UUID locationId, List<StockCountLineInput> lines, long varianceThreshold);

    Optional<StockCountDto> getStockCount(UUID stockCountId);

    StockCountDto submitStockCount(UUID stockCountId, List<CountedLineInput> countedLines);

    StockCountDto approveStockCount(UUID stockCountId);

    BranchInventoryConfigDto setBranchConfig(UUID branchId, boolean allowNegativeStock);

    Optional<BranchInventoryConfigDto> getBranchConfig(UUID branchId);

    record TransferLineInput(UUID productVariantId, int quantity) {}

    record StockCountLineInput(UUID productVariantId, int expectedQuantity) {}

    record CountedLineInput(UUID lineId, int countedQuantity) {}
}
