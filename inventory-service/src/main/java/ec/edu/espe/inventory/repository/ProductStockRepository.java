package ec.edu.espe.inventory.repository;

import ec.edu.espe.inventory.model.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductStock> findByProductId(UUID productId);
}
