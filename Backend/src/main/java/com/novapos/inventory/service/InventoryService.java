package com.novapos.inventory.service;

import com.novapos.inventory.api.InventoryFacade;
import com.novapos.inventory.api.dto.BatchDto;
import com.novapos.inventory.api.dto.BranchInventoryConfigDto;
import com.novapos.inventory.api.dto.CostConsumptionDto;
import com.novapos.inventory.api.dto.CostLayerDto;
import com.novapos.inventory.api.dto.ReorderConfigDto;
import com.novapos.inventory.api.dto.StockCountDto;
import com.novapos.inventory.api.dto.StockCountLineDto;
import com.novapos.inventory.api.dto.StockMovementDto;
import com.novapos.inventory.api.dto.TransferLineDto;
import com.novapos.inventory.api.dto.TransferOrderDto;
import com.novapos.inventory.domain.Batch;
import com.novapos.inventory.domain.BranchInventoryConfig;
import com.novapos.inventory.domain.CostLayer;
import com.novapos.inventory.domain.MovementReason;
import com.novapos.inventory.domain.ReorderConfig;
import com.novapos.inventory.domain.StockCount;
import com.novapos.inventory.domain.StockCountLine;
import com.novapos.inventory.domain.StockMovement;
import com.novapos.inventory.domain.TransferLine;
import com.novapos.inventory.domain.TransferOrder;
import com.novapos.inventory.domain.TransferStatus;
import com.novapos.inventory.repository.BatchRepository;
import com.novapos.inventory.repository.BranchInventoryConfigRepository;
import com.novapos.inventory.repository.CostLayerRepository;
import com.novapos.inventory.repository.ReorderConfigRepository;
import com.novapos.inventory.repository.StockCountLineRepository;
import com.novapos.inventory.repository.StockCountRepository;
import com.novapos.inventory.repository.StockMovementRepository;
import com.novapos.inventory.repository.TransferLineRepository;
import com.novapos.inventory.repository.TransferOrderRepository;
import com.novapos.inventory.web.InventoryException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class InventoryService implements InventoryFacade {

    private final StockMovementRepository stockMovementRepository;
    private final CostLayerRepository costLayerRepository;
    private final BatchRepository batchRepository;
    private final ReorderConfigRepository reorderConfigRepository;
    private final TransferOrderRepository transferOrderRepository;
    private final TransferLineRepository transferLineRepository;
    private final StockCountRepository stockCountRepository;
    private final StockCountLineRepository stockCountLineRepository;
    private final BranchInventoryConfigRepository branchInventoryConfigRepository;

    InventoryService(StockMovementRepository stockMovementRepository,
                     CostLayerRepository costLayerRepository,
                     BatchRepository batchRepository,
                     ReorderConfigRepository reorderConfigRepository,
                     TransferOrderRepository transferOrderRepository,
                     TransferLineRepository transferLineRepository,
                     StockCountRepository stockCountRepository,
                     StockCountLineRepository stockCountLineRepository,
                     BranchInventoryConfigRepository branchInventoryConfigRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.costLayerRepository = costLayerRepository;
        this.batchRepository = batchRepository;
        this.reorderConfigRepository = reorderConfigRepository;
        this.transferOrderRepository = transferOrderRepository;
        this.transferLineRepository = transferLineRepository;
        this.stockCountRepository = stockCountRepository;
        this.stockCountLineRepository = stockCountLineRepository;
        this.branchInventoryConfigRepository = branchInventoryConfigRepository;
    }

    @Override
    public StockMovementDto recordMovement(UUID productVariantId, UUID locationId, int quantityDelta,
                                            String reason, Long unitCostMinor, UUID batchId,
                                            String referenceType, UUID referenceId, UUID createdBy) {
        var movementReason = MovementReason.valueOf(reason.toUpperCase());
        var movement = new StockMovement(productVariantId, locationId, quantityDelta,
                movementReason, unitCostMinor, batchId, referenceType, referenceId, createdBy);
        movement = stockMovementRepository.save(movement);
        return toDto(movement);
    }

    @Override
    public int getOnHandQuantity(UUID productVariantId, UUID locationId) {
        return stockMovementRepository.sumQuantityDelta(productVariantId, locationId);
    }

    @Override
    public List<StockMovementDto> getMovements(UUID productVariantId, UUID locationId) {
        return stockMovementRepository.findByProductVariantIdAndLocationIdOrderByCreatedAtAsc(
                        productVariantId, locationId).stream()
                .map(InventoryService::toDto)
                .toList();
    }

    @Override
    public void recordReceipt(UUID productVariantId, UUID locationId, int quantity,
                               long unitCostMinor, String batchCode, LocalDate expiryDate) {
        UUID batchId = null;
        if (batchCode != null) {
            var batch = new Batch(productVariantId, batchCode, expiryDate);
            batch = batchRepository.save(batch);
            batchId = batch.getId();
        }

        var movement = new StockMovement(productVariantId, locationId, quantity,
                MovementReason.RECEIPT, unitCostMinor, batchId, null, null, null);
        stockMovementRepository.save(movement);

        var layer = new CostLayer(productVariantId, locationId, quantity, unitCostMinor);
        costLayerRepository.save(layer);
    }

    @Override
    public CostConsumptionDto consumeFifo(UUID productVariantId, UUID locationId, int quantity) {
        var layers = costLayerRepository
                .findByProductVariantIdAndLocationIdAndQuantityGreaterThanOrderByCreatedAtAsc(
                        productVariantId, locationId, 0);
        var result = consumeLayers(layers, quantity, "FIFO");
        recordConsumptionMovement(productVariantId, locationId, quantity, result);
        return result;
    }

    @Override
    public CostConsumptionDto consumeLifo(UUID productVariantId, UUID locationId, int quantity) {
        var layers = costLayerRepository
                .findByProductVariantIdAndLocationIdAndQuantityGreaterThanOrderByCreatedAtDesc(
                        productVariantId, locationId, 0);
        var result = consumeLayers(layers, quantity, "LIFO");
        recordConsumptionMovement(productVariantId, locationId, quantity, result);
        return result;
    }

    @Override
    public BatchDto createBatch(UUID productVariantId, String batchCode, LocalDate expiryDate) {
        var batch = new Batch(productVariantId, batchCode, expiryDate);
        batch = batchRepository.save(batch);
        return toDto(batch);
    }

    @Override
    public List<BatchDto> getBatches(UUID productVariantId) {
        return batchRepository.findByProductVariantId(productVariantId).stream()
                .map(InventoryService::toDto)
                .toList();
    }

    @Override
    public List<BatchDto> findBatchesExpiringBefore(LocalDate date, UUID productVariantId) {
        return batchRepository.findByProductVariantIdAndExpiryDateBefore(productVariantId, date).stream()
                .map(InventoryService::toDto)
                .toList();
    }

    private CostConsumptionDto consumeLayers(List<CostLayer> layers, int quantity, String method) {
        var remaining = quantity;
        long totalCost = 0;
        var consumedLayers = new ArrayList<CostLayerDto>();

        for (var layer : layers) {
            if (remaining <= 0) break;
            var take = Math.min(layer.getQuantity(), remaining);
            totalCost += take * layer.getUnitCostMinor();
            consumedLayers.add(new CostLayerDto(layer.getId(), layer.getProductVariantId(),
                    layer.getLocationId(), take, layer.getUnitCostMinor()));
            layer.setQuantity(layer.getQuantity() - take);
            remaining -= take;
        }

        if (consumedLayers.isEmpty()) {
            return new CostConsumptionDto(
                    layers.isEmpty() ? null : layers.getFirst().getProductVariantId(),
                    layers.isEmpty() ? null : layers.getFirst().getLocationId(),
                    0, 0, method, List.of());
        }

        costLayerRepository.saveAll(layers.stream()
                .filter(l -> l.getQuantity() == 0)
                .toList());
        costLayerRepository.saveAll(layers.stream()
                .filter(l -> l.getQuantity() > 0)
                .toList());

        return new CostConsumptionDto(
                layers.getFirst().getProductVariantId(),
                layers.getFirst().getLocationId(),
                quantity,
                totalCost,
                method,
                consumedLayers
        );
    }

    private void recordConsumptionMovement(UUID productVariantId, UUID locationId, int quantity,
                                            CostConsumptionDto result) {
        var unitCost = result.totalCostMinor() / quantity;
        var movement = new StockMovement(productVariantId, locationId, -quantity,
                MovementReason.SALE, unitCost, null, null, null, null);
        stockMovementRepository.save(movement);
    }

    @Override
    public ReorderConfigDto upsertReorderConfig(UUID productVariantId, UUID locationId, int reorderLevel, int reorderQuantity) {
        var existing = reorderConfigRepository.findByProductVariantIdAndLocationId(productVariantId, locationId);
        if (existing.isPresent()) {
            var config = existing.get();
            config.setReorderLevel(reorderLevel);
            config.setReorderQuantity(reorderQuantity);
            config.markUpdated();
            return toDto(reorderConfigRepository.save(config));
        }
        var config = new ReorderConfig(productVariantId, locationId, reorderLevel, reorderQuantity);
        return toDto(reorderConfigRepository.save(config));
    }

    @Override
    public Optional<ReorderConfigDto> getReorderConfig(UUID productVariantId, UUID locationId) {
        return reorderConfigRepository.findByProductVariantIdAndLocationId(productVariantId, locationId)
                .map(InventoryService::toDto);
    }

    @Override
    public TransferOrderDto createTransfer(UUID sourceLocationId, UUID destinationLocationId,
                                            List<TransferLineInput> lines) {
        var order = new TransferOrder(sourceLocationId, destinationLocationId);
        order = transferOrderRepository.save(order);

        for (var line : lines) {
            var tl = new TransferLine(order.getId(), line.productVariantId(), line.quantity());
            transferLineRepository.save(tl);
        }

        return getTransfer(order.getId()).orElseThrow();
    }

    @Override
    public Optional<TransferOrderDto> getTransfer(UUID transferOrderId) {
        return transferOrderRepository.findById(transferOrderId).map(order -> {
            var lines = transferLineRepository.findByTransferOrderId(transferOrderId).stream()
                    .map(InventoryService::toDto).toList();
            return toDto(order, lines);
        });
    }

    @Override
    public TransferOrderDto advanceTransferStatus(UUID transferOrderId, String targetStatus) {
        var order = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> InventoryException.transferNotFound(transferOrderId));
        var newStatus = TransferStatus.valueOf(targetStatus.toUpperCase());
        order.setStatus(newStatus);
        order.markUpdated();

        if (newStatus == TransferStatus.PICKED) {
            var lines = transferLineRepository.findByTransferOrderId(transferOrderId);
            for (var line : lines) {
                var movement = new StockMovement(line.getProductVariantId(), order.getSourceLocationId(),
                        -line.getQuantity(), MovementReason.TRANSFER_OUT, null, null, null, null, null);
                stockMovementRepository.save(movement);
            }
        }
        if (newStatus == TransferStatus.RECEIVED) {
            var lines = transferLineRepository.findByTransferOrderId(transferOrderId);
            for (var line : lines) {
                var movement = new StockMovement(line.getProductVariantId(), order.getDestinationLocationId(),
                        line.getQuantity(), MovementReason.TRANSFER_IN, null, null, null, null, null);
                stockMovementRepository.save(movement);
            }
        }

        transferOrderRepository.save(order);
        var lines = transferLineRepository.findByTransferOrderId(transferOrderId).stream()
                .map(InventoryService::toDto).toList();
        return toDto(order, lines);
    }

    @Override
    public StockCountDto createStockCount(UUID locationId, List<StockCountLineInput> lines, long varianceThreshold) {
        var count = new StockCount(locationId, varianceThreshold);
        count = stockCountRepository.save(count);

        for (var line : lines) {
            var scl = new StockCountLine(count.getId(), line.productVariantId(), line.expectedQuantity());
            stockCountLineRepository.save(scl);
        }

        return getStockCount(count.getId()).orElseThrow();
    }

    @Override
    public Optional<StockCountDto> getStockCount(UUID stockCountId) {
        return stockCountRepository.findById(stockCountId).map(count -> {
            var lines = stockCountLineRepository.findByStockCountId(stockCountId).stream()
                    .map(InventoryService::toDto).toList();
            return toDto(count, lines);
        });
    }

    @Override
    public StockCountDto submitStockCount(UUID stockCountId, List<CountedLineInput> countedLines) {
        var count = stockCountRepository.findById(stockCountId)
                .orElseThrow(() -> InventoryException.stockCountNotFound(stockCountId));
        if (!"DRAFT".equals(count.getStatus())) {
            throw InventoryException.invalidCountStatus(count.getStatus());
        }

        for (var cl : countedLines) {
            var line = stockCountLineRepository.findById(cl.lineId())
                    .orElseThrow(() -> InventoryException.countLineNotFound(cl.lineId()));
            line.setCountedQuantity(cl.countedQuantity());
            stockCountLineRepository.save(line);
        }

        var totalVariance = 0;
        var lines = stockCountLineRepository.findByStockCountId(stockCountId);
        for (var line : lines) {
            if (line.getCountedQuantity() != null) {
                totalVariance += Math.abs(line.getExpectedQuantity() - line.getCountedQuantity());
            }
        }

        if (totalVariance <= count.getVarianceThreshold()) {
            count.setStatus("APPROVED");
            for (var line : lines) {
                if (line.getCountedQuantity() != null && line.getCountedQuantity() != line.getExpectedQuantity()) {
                    var diff = line.getCountedQuantity() - line.getExpectedQuantity();
                    var movement = new StockMovement(line.getProductVariantId(), count.getLocationId(),
                            diff, MovementReason.COUNT_VARIANCE, null, null, null, null, null);
                    stockMovementRepository.save(movement);
                }
            }
        } else {
            count.setStatus("PENDING_APPROVAL");
        }
        count.markUpdated();
        stockCountRepository.save(count);

        var updatedLines = stockCountLineRepository.findByStockCountId(stockCountId).stream()
                .map(InventoryService::toDto).toList();
        return toDto(count, updatedLines);
    }

    @Override
    public StockCountDto approveStockCount(UUID stockCountId) {
        var count = stockCountRepository.findById(stockCountId)
                .orElseThrow(() -> InventoryException.stockCountNotFound(stockCountId));
        if (!"PENDING_APPROVAL".equals(count.getStatus())) {
            throw InventoryException.invalidCountStatus(count.getStatus());
        }

        count.setStatus("APPROVED");
        count.markUpdated();
        stockCountRepository.save(count);

        var lines = stockCountLineRepository.findByStockCountId(stockCountId);
        for (var line : lines) {
            if (line.getCountedQuantity() != null && line.getCountedQuantity() != line.getExpectedQuantity()) {
                var diff = line.getCountedQuantity() - line.getExpectedQuantity();
                var movement = new StockMovement(line.getProductVariantId(), count.getLocationId(),
                        diff, MovementReason.COUNT_VARIANCE, null, null, null, null, null);
                stockMovementRepository.save(movement);
            }
        }

        var updatedLines = lines.stream().map(InventoryService::toDto).toList();
        return toDto(count, updatedLines);
    }

    @Override
    public BranchInventoryConfigDto setBranchConfig(UUID branchId, boolean allowNegativeStock) {
        var existing = branchInventoryConfigRepository.findByBranchId(branchId);
        if (existing.isPresent()) {
            var config = existing.get();
            config.setAllowNegativeStock(allowNegativeStock);
            config.markUpdated();
            return toDto(branchInventoryConfigRepository.save(config));
        }
        var config = new BranchInventoryConfig(branchId, allowNegativeStock);
        return toDto(branchInventoryConfigRepository.save(config));
    }

    @Override
    public Optional<BranchInventoryConfigDto> getBranchConfig(UUID branchId) {
        return branchInventoryConfigRepository.findByBranchId(branchId)
                .map(InventoryService::toDto);
    }

    static StockMovementDto toDto(StockMovement movement) {
        return new StockMovementDto(
                movement.getId(),
                movement.getProductVariantId(),
                movement.getLocationId(),
                movement.getQuantityDelta(),
                movement.getReason().name(),
                movement.getUnitCostMinor(),
                movement.getBatchId(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getCreatedAt(),
                movement.getCreatedBy()
        );
    }

    static BatchDto toDto(Batch batch) {
        return new BatchDto(
                batch.getId(),
                batch.getProductVariantId(),
                batch.getBatchCode(),
                batch.getExpiryDate()
        );
    }

    static ReorderConfigDto toDto(ReorderConfig config) {
        return new ReorderConfigDto(config.getId(), config.getProductVariantId(),
                config.getLocationId(), config.getReorderLevel(), config.getReorderQuantity());
    }

    static TransferOrderDto toDto(TransferOrder order, List<TransferLineDto> lines) {
        return new TransferOrderDto(order.getId(), order.getSourceLocationId(),
                order.getDestinationLocationId(), order.getStatus().name(), lines);
    }

    static TransferLineDto toDto(TransferLine line) {
        return new TransferLineDto(line.getId(), line.getTransferOrderId(),
                line.getProductVariantId(), line.getQuantity());
    }

    static StockCountDto toDto(StockCount count, List<StockCountLineDto> lines) {
        return new StockCountDto(count.getId(), count.getLocationId(),
                count.getStatus(), count.getVarianceThreshold(), lines);
    }

    static StockCountLineDto toDto(StockCountLine line) {
        return new StockCountLineDto(line.getId(), line.getStockCountId(),
                line.getProductVariantId(), line.getExpectedQuantity(), line.getCountedQuantity());
    }

    static BranchInventoryConfigDto toDto(BranchInventoryConfig config) {
        return new BranchInventoryConfigDto(config.getId(), config.getBranchId(), config.isAllowNegativeStock());
    }
}
