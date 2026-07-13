package com.novapos.inventory.service;

import com.novapos.inventory.domain.Batch;
import com.novapos.inventory.domain.CostLayer;
import com.novapos.inventory.domain.MovementReason;
import com.novapos.inventory.domain.StockMovement;
import com.novapos.inventory.repository.BatchRepository;
import com.novapos.inventory.repository.CostLayerRepository;
import com.novapos.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private CostLayerRepository costLayerRepository;
    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Record a receipt movement and verify DTO")
    void recordReceiptMovement() {
        var variantId = UUID.randomUUID();
        var locationId = UUID.randomUUID();
        var movement = new StockMovement(variantId, locationId, 50,
                MovementReason.RECEIPT, 1000L, null, null, null, null);

        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(movement);

        var dto = inventoryService.recordMovement(variantId, locationId, 50,
                "RECEIPT", 1000L, null, null, null, null);

        assertThat(dto.productVariantId()).isEqualTo(variantId);
        assertThat(dto.quantityDelta()).isEqualTo(50);
        assertThat(dto.reason()).isEqualTo("RECEIPT");
        assertThat(dto.unitCostMinor()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Record a sale movement (negative delta)")
    void recordSaleMovement() {
        var variantId = UUID.randomUUID();
        var locationId = UUID.randomUUID();
        var movement = new StockMovement(variantId, locationId, -3,
                MovementReason.SALE, null, null, "sale", UUID.randomUUID(), null);

        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(movement);

        var dto = inventoryService.recordMovement(variantId, locationId, -3,
                "SALE", null, null, "sale", movement.getReferenceId(), null);

        assertThat(dto.quantityDelta()).isEqualTo(-3);
        assertThat(dto.reason()).isEqualTo("SALE");
        assertThat(dto.referenceType()).isEqualTo("sale");
    }

    @Test
    @DisplayName("getOnHandQuantity returns sum of deltas")
    void getOnHandReturnsSum() {
        var variantId = UUID.randomUUID();
        var locationId = UUID.randomUUID();

        when(stockMovementRepository.sumQuantityDelta(variantId, locationId)).thenReturn(47);

        var onHand = inventoryService.getOnHandQuantity(variantId, locationId);

        assertThat(onHand).isEqualTo(47);
    }

    @Test
    @DisplayName("getOnHandQuantity returns 0 when no movements exist")
    void getOnHandReturnsZeroWhenNoMovements() {
        var variantId = UUID.randomUUID();
        var locationId = UUID.randomUUID();

        when(stockMovementRepository.sumQuantityDelta(variantId, locationId)).thenReturn(0);

        var onHand = inventoryService.getOnHandQuantity(variantId, locationId);

        assertThat(onHand).isZero();
    }

    @Test
    @DisplayName("FIFO consumption consumes oldest layers first")
    void fifoConsumesOldestFirst() {
        var variantId = UUID.randomUUID();
        var locationId = UUID.randomUUID();

        var layer1 = new CostLayer(variantId, locationId, 10, 1000L);
        var layer2 = new CostLayer(variantId, locationId, 20, 1200L);

        when(costLayerRepository
                .findByProductVariantIdAndLocationIdAndQuantityGreaterThanOrderByCreatedAtAsc(
                        variantId, locationId, 0))
                .thenReturn(List.of(layer1, layer2));

        var result = inventoryService.consumeFifo(variantId, locationId, 15);

        assertThat(result.method()).isEqualTo("FIFO");
        assertThat(result.consumedLayers()).hasSize(2);
        assertThat(result.consumedLayers().get(0).unitCostMinor()).isEqualTo(1000L);
        assertThat(result.consumedLayers().get(0).quantity()).isEqualTo(10);
        assertThat(result.consumedLayers().get(1).unitCostMinor()).isEqualTo(1200L);
        assertThat(result.consumedLayers().get(1).quantity()).isEqualTo(5);

        assertThat(layer1.getQuantity()).isZero();
        assertThat(layer2.getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("LIFO consumption consumes newest layers first")
    void lifoConsumesNewestFirst() {
        var variantId = UUID.randomUUID();
        var locationId = UUID.randomUUID();

        var layer1 = new CostLayer(variantId, locationId, 10, 1000L);
        var layer2 = new CostLayer(variantId, locationId, 20, 1200L);

        when(costLayerRepository
                .findByProductVariantIdAndLocationIdAndQuantityGreaterThanOrderByCreatedAtDesc(
                        variantId, locationId, 0))
                .thenReturn(List.of(layer2, layer1));

        var result = inventoryService.consumeLifo(variantId, locationId, 25);

        assertThat(result.method()).isEqualTo("LIFO");
        assertThat(result.consumedLayers()).hasSize(2);
        assertThat(result.consumedLayers().get(0).unitCostMinor()).isEqualTo(1200L);
        assertThat(result.consumedLayers().get(0).quantity()).isEqualTo(20);
        assertThat(result.consumedLayers().get(1).unitCostMinor()).isEqualTo(1000L);
        assertThat(result.consumedLayers().get(1).quantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("findBatchesExpiringBefore returns matching batches")
    void findBatchesExpiringBeforeReturnsMatching() {
        var variantId = UUID.randomUUID();
        var today = LocalDate.now();
        var batch = new Batch(variantId, "BATCH-001", today.minusDays(5));

        when(batchRepository.findByProductVariantIdAndExpiryDateBefore(variantId, today))
                .thenReturn(List.of(batch));

        var result = inventoryService.findBatchesExpiringBefore(today, variantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).batchCode()).isEqualTo("BATCH-001");
    }
}
