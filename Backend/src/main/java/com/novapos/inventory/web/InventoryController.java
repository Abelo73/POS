package com.novapos.inventory.web;

import com.novapos.inventory.api.InventoryFacade;
import com.novapos.inventory.api.InventoryFacade.CountedLineInput;
import com.novapos.inventory.api.InventoryFacade.StockCountLineInput;
import com.novapos.inventory.api.InventoryFacade.TransferLineInput;
import com.novapos.inventory.api.dto.BatchDto;
import com.novapos.inventory.api.dto.BranchInventoryConfigDto;
import com.novapos.inventory.api.dto.CostConsumptionDto;
import com.novapos.inventory.api.dto.ReorderConfigDto;
import com.novapos.inventory.api.dto.StockCountDto;
import com.novapos.inventory.api.dto.StockMovementDto;
import com.novapos.inventory.api.dto.TransferOrderDto;
import com.novapos.inventory.web.dto.ConsumeRequest;
import com.novapos.inventory.web.dto.CreateBatchRequest;
import com.novapos.inventory.web.dto.CreateStockCountRequest;
import com.novapos.inventory.web.dto.CreateTransferRequest;
import com.novapos.inventory.web.dto.OnHandResponse;
import com.novapos.inventory.web.dto.ReceiptRequest;
import com.novapos.inventory.web.dto.RecordMovementRequest;
import com.novapos.inventory.web.dto.ReorderConfigRequest;
import com.novapos.inventory.web.dto.SetBranchConfigRequest;
import com.novapos.inventory.web.dto.SubmitStockCountRequest;
import com.novapos.inventory.web.dto.TransferStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
class InventoryController {

    private final InventoryFacade inventoryFacade;

    InventoryController(InventoryFacade inventoryFacade) {
        this.inventoryFacade = inventoryFacade;
    }

    @PostMapping("/movements")
    ResponseEntity<StockMovementDto> recordMovement(@Valid @RequestBody RecordMovementRequest request) {
        var movement = inventoryFacade.recordMovement(
                request.productVariantId(), request.locationId(), request.quantityDelta(),
                request.reason(), request.unitCostMinor(), request.batchId(),
                request.referenceType(), request.referenceId(), request.createdBy());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(movement.id())
                .toUri();
        return ResponseEntity.created(location).body(movement);
    }

    @GetMapping("/on-hand")
    ResponseEntity<OnHandResponse> getOnHand(@RequestParam UUID productVariantId, @RequestParam UUID locationId) {
        var qty = inventoryFacade.getOnHandQuantity(productVariantId, locationId);
        return ResponseEntity.ok(new OnHandResponse(qty));
    }

    @GetMapping("/movements")
    ResponseEntity<List<StockMovementDto>> getMovements(@RequestParam UUID productVariantId, @RequestParam UUID locationId) {
        return ResponseEntity.ok(inventoryFacade.getMovements(productVariantId, locationId));
    }

    @PostMapping("/receipts")
    ResponseEntity<Void> recordReceipt(@Valid @RequestBody ReceiptRequest request) {
        inventoryFacade.recordReceipt(request.productVariantId(), request.locationId(),
                request.quantity(), request.unitCostMinor(), request.batchCode(), request.expiryDate());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/consume-fifo")
    ResponseEntity<CostConsumptionDto> consumeFifo(@Valid @RequestBody ConsumeRequest request) {
        return ResponseEntity.ok(inventoryFacade.consumeFifo(
                request.productVariantId(), request.locationId(), request.quantity()));
    }

    @PostMapping("/consume-lifo")
    ResponseEntity<CostConsumptionDto> consumeLifo(@Valid @RequestBody ConsumeRequest request) {
        return ResponseEntity.ok(inventoryFacade.consumeLifo(
                request.productVariantId(), request.locationId(), request.quantity()));
    }

    @PostMapping("/batches")
    ResponseEntity<BatchDto> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        var batch = inventoryFacade.createBatch(request.productVariantId(), request.batchCode(), request.expiryDate());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(batch.id())
                .toUri();
        return ResponseEntity.created(location).body(batch);
    }

    @GetMapping("/batches")
    ResponseEntity<List<BatchDto>> getBatches(@RequestParam UUID productVariantId) {
        return ResponseEntity.ok(inventoryFacade.getBatches(productVariantId));
    }

    @GetMapping("/batches/expiring-before")
    ResponseEntity<List<BatchDto>> findBatchesExpiringBefore(@RequestParam UUID productVariantId,
                                                              @RequestParam String date) {
        return ResponseEntity.ok(inventoryFacade.findBatchesExpiringBefore(
                java.time.LocalDate.parse(date), productVariantId));
    }

    @PutMapping("/reorder-config")
    ResponseEntity<ReorderConfigDto> upsertReorderConfig(@Valid @RequestBody ReorderConfigRequest request) {
        return ResponseEntity.ok(inventoryFacade.upsertReorderConfig(
                request.productVariantId(), request.locationId(),
                request.reorderLevel(), request.reorderQuantity()));
    }

    @GetMapping("/reorder-config")
    ResponseEntity<ReorderConfigDto> getReorderConfig(@RequestParam UUID productVariantId,
                                                       @RequestParam UUID locationId) {
        return inventoryFacade.getReorderConfig(productVariantId, locationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/transfers")
    ResponseEntity<TransferOrderDto> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        var lines = request.lines().stream()
                .map(l -> new TransferLineInput(l.productVariantId(), l.quantity()))
                .toList();
        var transfer = inventoryFacade.createTransfer(request.sourceLocationId(),
                request.destinationLocationId(), lines);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(transfer.id()).toUri();
        return ResponseEntity.created(location).body(transfer);
    }

    @GetMapping("/transfers/{transferOrderId}")
    ResponseEntity<TransferOrderDto> getTransfer(@PathVariable UUID transferOrderId) {
        return inventoryFacade.getTransfer(transferOrderId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> InventoryException.transferNotFound(transferOrderId));
    }

    @PutMapping("/transfers/{transferOrderId}/status")
    ResponseEntity<TransferOrderDto> advanceTransferStatus(@PathVariable UUID transferOrderId,
                                                            @RequestBody TransferStatusRequest request) {
        return ResponseEntity.ok(inventoryFacade.advanceTransferStatus(transferOrderId, request.status()));
    }

    @PostMapping("/stock-counts")
    ResponseEntity<StockCountDto> createStockCount(@Valid @RequestBody CreateStockCountRequest request) {
        var lines = request.lines().stream()
                .map(l -> new StockCountLineInput(l.productVariantId(), l.expectedQuantity()))
                .toList();
        var count = inventoryFacade.createStockCount(request.locationId(), lines, request.varianceThreshold());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(count.id()).toUri();
        return ResponseEntity.created(location).body(count);
    }

    @GetMapping("/stock-counts/{stockCountId}")
    ResponseEntity<StockCountDto> getStockCount(@PathVariable UUID stockCountId) {
        return inventoryFacade.getStockCount(stockCountId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> InventoryException.stockCountNotFound(stockCountId));
    }

    @PutMapping("/stock-counts/{stockCountId}/submit")
    ResponseEntity<StockCountDto> submitStockCount(@PathVariable UUID stockCountId,
                                                    @Valid @RequestBody SubmitStockCountRequest request) {
        var countedLines = request.lines().stream()
                .map(l -> new CountedLineInput(l.lineId(), l.countedQuantity()))
                .toList();
        return ResponseEntity.ok(inventoryFacade.submitStockCount(stockCountId, countedLines));
    }

    @PutMapping("/stock-counts/{stockCountId}/approve")
    ResponseEntity<StockCountDto> approveStockCount(@PathVariable UUID stockCountId) {
        return ResponseEntity.ok(inventoryFacade.approveStockCount(stockCountId));
    }

    @PutMapping("/branch-config")
    ResponseEntity<BranchInventoryConfigDto> setBranchConfig(@Valid @RequestBody SetBranchConfigRequest request) {
        return ResponseEntity.ok(inventoryFacade.setBranchConfig(request.branchId(), request.allowNegativeStock()));
    }

    @GetMapping("/branch-config")
    ResponseEntity<BranchInventoryConfigDto> getBranchConfig(@RequestParam UUID branchId) {
        return inventoryFacade.getBranchConfig(branchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
