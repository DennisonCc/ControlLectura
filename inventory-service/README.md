# Inventory Service

Microservicio de inventario para plataforma de e-commerce que gestiona stock de productos y se comunica de forma asíncrona mediante RabbitMQ.

## 📋 Descripción

Este servicio es parte de una arquitectura de microservicios event-driven que:
- Consume eventos `OrderCreated` desde RabbitMQ
- Verifica disponibilidad de stock en PostgreSQL
- Reserva stock si está disponible
- Publica eventos `StockReserved` o `StockRejected` según el resultado
- Expone API REST para consultar stock de productos

## 🏗️ Arquitectura

```
┌─────────────┐      OrderCreated      ┌──────────────────┐
│Order Service│ ──────────────────────> │   RabbitMQ       │
└─────────────┘                         │  (Exchange)      │
                                        └────────┬─────────┘
                                                 │
                                                 ▼
                                        ┌──────────────────┐
                                        │Inventory Service │
                                        │  - Check Stock   │
                                        │  - Reserve Stock │
                                        └────────┬─────────┘
                                                 │
                    ┌────────────────────────────┼────────────────────────┐
                    ▼                            ▼                        ▼
            StockReserved              StockRejected              PostgreSQL
```

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **Spring AMQP** (RabbitMQ)
- **PostgreSQL**
- **Lombok**
- **Gradle**

## 📦 Estructura del Proyecto

```
inventory-service/
├── src/main/java/ec/edu/espe/inventory/
│   ├── InventoryServiceApplication.java
│   ├── config/
│   │   └── RabbitMQConfig.java          # Configuración de RabbitMQ
│   ├── model/
│   │   └── ProductStock.java            # Entidad JPA
│   ├── repository/
│   │   └── ProductStockRepository.java  # Repositorio JPA
│   ├── service/
│   │   └── InventoryService.java        # Lógica de negocio
│   ├── messaging/
│   │   ├── OrderEventConsumer.java      # Consumidor de eventos
│   │   └── EventPublisher.java          # Publicador de eventos
│   ├── dto/
│   │   ├── OrderCreatedEvent.java
│   │   ├── OrderItem.java
│   │   ├── StockReservedEvent.java
│   │   ├── StockRejectedEvent.java
│   │   └── ProductStockResponse.java
│   └── controller/
│       └── ProductStockController.java  # API REST
├── src/main/resources/
│   ├── application.properties
│   └── data.sql                         # Datos iniciales
├── build.gradle
├── Dockerfile
├── .env.example
└── README.md
```

## ⚙️ Configuración

### Variables de Entorno

Crea un archivo `.env` basado en `.env.example`:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=inventory_db
DB_USER=postgres
DB_PASSWORD=postgres

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
```

### Base de Datos

El servicio crea automáticamente la tabla `products_stock`:

```sql
CREATE TABLE products_stock (
    product_id UUID PRIMARY KEY,
    available_stock INTEGER NOT NULL,
    reserved_stock INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

Los datos de prueba se cargan automáticamente desde `data.sql`.

## 🔧 Ejecución

### Requisitos Previos

- Java 21
- PostgreSQL 15+
- RabbitMQ 3.12+

### Opción 1: Ejecución Local

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar el servicio
./gradlew bootRun
```

### Opción 2: Docker

```bash
# Construir imagen
docker build -t inventory-service .

# Ejecutar contenedor
docker run -p 8081:8081 \
  -e DB_HOST=postgres \
  -e RABBITMQ_HOST=rabbitmq \
  inventory-service
```

### Opción 3: Docker Compose

Ver el archivo `docker-compose.yml` en `/infrastructure` del repositorio principal.

## 📡 API REST

### Consultar Stock de un Producto

**Endpoint:** `GET /api/v1/products/{productId}/stock`

**Ejemplo de Request:**
```bash
curl http://localhost:8081/api/v1/products/a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d/stock
```

**Respuesta Exitosa (200):**
```json
{
  "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
  "availableStock": 25,
  "reservedStock": 3,
  "updatedAt": "2026-01-21T15:08:10"
}
```

**Respuesta Error (404):**
```json
{
  "error": "Product not found",
  "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d"
}
```

## 📨 Eventos RabbitMQ

### Configuración de Exchanges y Queues

- **Exchange:** `orders.exchange` (topic)
- **Queues:**
  - `inventory.orders.queue` - Recibe eventos `OrderCreated`
  - `orders.results.queue` - Envía eventos `StockReserved` / `StockRejected`

### Evento Consumido: OrderCreated

**Routing Key:** `order.created`

**Formato:**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "items": [
    {
      "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
      "quantity": 2
    }
  ]
}
```

### Evento Publicado: StockReserved

**Routing Key:** `stock.reserved`

**Formato:**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "status": "RESERVED",
  "timestamp": "2026-01-21T15:10:02"
}
```

### Evento Publicado: StockRejected

**Routing Key:** `stock.rejected`

**Formato:**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "status": "REJECTED",
  "reason": "Insufficient stock for product b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f",
  "timestamp": "2026-01-21T15:10:02"
}
```

## 🔄 Flujo de Procesamiento

1. **Recepción:** El servicio consume un evento `OrderCreated` desde RabbitMQ
2. **Verificación:** Verifica disponibilidad de stock para todos los productos del pedido
3. **Decisión:**
   - ✅ **Stock disponible:** Reserva stock (decrementa `availableStock`, incrementa `reservedStock`) y publica `StockReserved`
   - ❌ **Stock insuficiente:** Publica `StockRejected` con el ID del producto problemático
4. **Transaccionalidad:** Todas las operaciones de BD son atómicas (rollback si falla alguna)

## 🧪 Pruebas

### Ejecutar Tests

```bash
./gradlew test
```

### Prueba Manual con RabbitMQ

1. Acceder a RabbitMQ Management: http://localhost:15672
2. Ir a **Queues** → `inventory.orders.queue`
3. Publicar mensaje en **Publish message**:

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "items": [
    {
      "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
      "quantity": 2
    }
  ]
}
```

4. Verificar en `orders.results.queue` el evento de respuesta

## 📊 Productos de Prueba

El servicio incluye 5 productos precargados:

| Product ID | Available Stock | Reserved Stock |
|------------|----------------|----------------|
| `a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d` | 50 | 0 |
| `b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f` | 30 | 0 |
| `c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f` | 100 | 0 |
| `d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a` | 15 | 0 |
| `e7f8a9b0-1c2d-3e4f-5a6b-7c8d9e0f1a2b` | 75 | 0 |

## 🛡️ Características de Robustez

- **Bloqueo Pesimista:** Previene condiciones de carrera en actualizaciones concurrentes
- **Transacciones:** Garantiza atomicidad en reservas de stock
- **Manejo de Errores:** Captura excepciones y publica eventos de rechazo
- **Logging:** Trazabilidad completa de operaciones
- **Escalabilidad:** Soporta múltiples instancias del servicio

## 📝 Logs

El servicio genera logs detallados:

```
INFO  - Received OrderCreated event: orderId=..., items=2
INFO  - Checking stock availability for 2 items
INFO  - Stock availability check passed
INFO  - Reserving stock for order: ...
INFO  - Reserved 2 units of product a3c2b1d0-...
INFO  - Stock reservation completed for order: ...
INFO  - Publishing StockReserved event for order: ...
```

## 🤝 Integración con Order Service

Este servicio está diseñado para trabajar en conjunto con el **Order Service**. El flujo completo es:

1. Cliente → `POST /api/v1/orders` → Order Service
2. Order Service → Publica `OrderCreated` → RabbitMQ
3. **Inventory Service** → Consume `OrderCreated` → Verifica stock
4. **Inventory Service** → Publica `StockReserved`/`StockRejected` → RabbitMQ
5. Order Service → Consume resultado → Actualiza estado del pedido

## 📄 Licencia

Este proyecto es parte de un trabajo académico de la ESPE.

## 👥 Autor

Desarrollado como parte del curso de Sistemas Distribuidos - ESPE 2026
