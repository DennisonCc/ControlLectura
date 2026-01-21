package ec.edu.espe.inventory.controller;

import ec.edu.espe.inventory.dto.ProductStockResponse;
import ec.edu.espe.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductStockController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}/stock")
    public ResponseEntity<?> getProductStock(@PathVariable String productId) {
        try {
            log.info("Received request to get stock for product: {}", productId);
            
            UUID uuid = UUID.fromString(productId);
            ProductStockResponse response = inventoryService.getProductStock(uuid);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", productId);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid product ID format", productId));
        } catch (RuntimeException e) {
            log.error("Product not found: {}", productId);
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Product not found", productId));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String error, String productId) {}
}
