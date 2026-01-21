package ec.edu.espe.inventory.service;

import ec.edu.espe.inventory.dto.OrderItem;
import ec.edu.espe.inventory.dto.ProductStockResponse;
import ec.edu.espe.inventory.model.ProductStock;
import ec.edu.espe.inventory.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductStockRepository productStockRepository;

    /**
     * Verifica si hay stock suficiente para todos los items del pedido
     */
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(List<OrderItem> items) {
        log.info("Checking stock availability for {} items", items.size());
        
        for (OrderItem item : items) {
            Optional<ProductStock> stockOpt = productStockRepository.findByProductId(item.getProductId());
            
            if (stockOpt.isEmpty()) {
                log.warn("Product not found: {}", item.getProductId());
                return false;
            }
            
            ProductStock stock = stockOpt.get();
            if (stock.getAvailableStock() < item.getQuantity()) {
                log.warn("Insufficient stock for product {}: available={}, requested={}", 
                    item.getProductId(), stock.getAvailableStock(), item.getQuantity());
                return false;
            }
        }
        
        log.info("Stock availability check passed");
        return true;
    }

    /**
     * Reserva stock para los items del pedido
     */
    @Transactional
    public void reserveStock(UUID orderId, List<OrderItem> items) {
        log.info("Reserving stock for order: {}", orderId);
        
        for (OrderItem item : items) {
            ProductStock stock = productStockRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            
            // Decrementar stock disponible e incrementar stock reservado
            stock.setAvailableStock(stock.getAvailableStock() - item.getQuantity());
            stock.setReservedStock(stock.getReservedStock() + item.getQuantity());
            
            productStockRepository.save(stock);
            log.info("Reserved {} units of product {}", item.getQuantity(), item.getProductId());
        }
        
        log.info("Stock reservation completed for order: {}", orderId);
    }

    /**
     * Obtiene el stock de un producto específico
     */
    @Transactional(readOnly = true)
    public ProductStockResponse getProductStock(UUID productId) {
        log.info("Getting stock for product: {}", productId);
        
        ProductStock stock = productStockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        return new ProductStockResponse(
                stock.getProductId(),
                stock.getAvailableStock(),
                stock.getReservedStock(),
                stock.getUpdatedAt()
        );
    }

    /**
     * Encuentra el primer producto sin stock suficiente
     */
    @Transactional(readOnly = true)
    public UUID findFirstInsufficientStockProduct(List<OrderItem> items) {
        for (OrderItem item : items) {
            Optional<ProductStock> stockOpt = productStockRepository.findByProductId(item.getProductId());
            
            if (stockOpt.isEmpty() || stockOpt.get().getAvailableStock() < item.getQuantity()) {
                return item.getProductId();
            }
        }
        return null;
    }
}
