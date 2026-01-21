# Order Service

Microservicio de gestión de pedidos para plataforma de e-commerce que procesa pedidos de forma asíncrona mediante RabbitMQ.

## 📋 Descripción

Este servicio es parte de una arquitectura de microservicios event-driven que:
- Recibe solicitudes HTTP para crear pedidos
- Crea pedidos en estado PENDING
- Publica eventos `OrderCreated` a RabbitMQ
- Consume eventos `StockReserved` / `StockRejected` desde RabbitMQ
- Actualiza estado de pedidos a CONFIRMED o CANCELLED
- Expone API REST para consultar pedidos

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **Spring AMQP** (RabbitMQ)
- **PostgreSQL**
- **Lombok**
- **Maven**

## 📦 Estructura del Proyecto

```
order-service/
├── src/main/java/ec/edu/espe/order_service/
│   ├── OrderServiceApplication.java
│   ├── config/
│   │   └── RabbitMQConfig.java          # Configuración de RabbitMQ
│   ├── model/
│   │   ├── Order.java                   # Entidad JPA
│   │   ├── OrderItem.java
│   │   ├── OrderStatus.java
│   │   └── ShippingAddress.java
│   ├── repository/
│   │   └── OrderRepository.java         # Repositorio JPA
│   ├── service/
│   │   └── OrderService.java            # Lógica de negocio
│   ├── listener/
│   │   └── OrderEventListener.java      # Consumidor de eventos
│   ├── dto/
│   │   ├── OrderRequest.java
│   │   ├── OrderResponse.java
│   │   ├── OrderCreatedEvent.java
│   │   ├── StockReservedEvent.java
│   │   └── StockRejectedEvent.java
│   └── controller/
│       └── OrderController.java         # API REST
├── src/main/resources/
│   └── application.yaml
├── pom.xml
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
DB_NAME=orders_db
DB_USER=postgres
DB_PASSWORD=postgres

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
```

### Base de Datos

El servicio crea automáticamente las tablas `orders` y `order_items`:

```sql
CREATE TABLE orders (
    order_id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    message VARCHAR(500),
    reason VARCHAR(500),
    payment_reference VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    -- shipping address (embedded)
    country VARCHAR(100),
    city VARCHAR(100),
    street VARCHAR(255),
    postal_code VARCHAR(20),
    zip VARCHAR(20)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255) REFERENCES orders(order_id),
    product_id VARCHAR(255),
    quantity INTEGER
);
```

## 🔧 Ejecución

### Requisitos Previos

- Java 21
- PostgreSQL 15+
- RabbitMQ 3.12+
- Maven 3.8+

### Opción 1: Ejecución Local

```bash
# Compilar el proyecto
./mvnw clean package

# Ejecutar el servicio
./mvnw spring-boot:run
```

### Opción 2: Docker

```bash
# Construir imagen
docker build -t order-service .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e RABBITMQ_HOST=rabbitmq \
  order-service
```

### Opción 3: Docker Compose

Ver el archivo `docker-compose.yml` en la raíz del repositorio.

## 📡 API REST

### Crear Pedido

**Endpoint:** `POST /api/v1/orders`

**Request:**
```json
{
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "items": [
    {
      "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
      "quantity": 2
    }
  ],
  "shippingAddress": {
    "country": "EC",
    "city": "Quito",
    "street": "Av. Amazonas",
    "postalCode": "170135"
  },
  "paymentReference": "pay_abc123"
}
```

**Response (201):**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "status": "PENDING",
  "message": "Order received. Inventory check in progress."
}
```

### Consultar Pedido

**Endpoint:** `GET /api/v1/orders/{orderId}`

**Response (200) - Confirmado:**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "status": "CONFIRMED",
  "items": [
    {
      "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
      "quantity": 2
    }
  ],
  "updatedAt": "2026-01-21T15:10:02Z"
}
```

**Response (200) - Cancelado:**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "status": "CANCELLED",
  "reason": "Insufficient stock for product b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f",
  "items": [...],
  "updatedAt": "2026-01-21T15:10:02Z"
}
```

## 📨 Eventos RabbitMQ

### Configuración

- **Exchange:** `orders.exchange` (topic)
- **Queues:**
  - `inventory.orders.queue` - Publica `OrderCreated`
  - `orders.results.queue` - Consume `StockReserved` / `StockRejected`

### Evento Publicado: OrderCreated

**Routing Key:** `order.created`

```json
{
  "eventType": "OrderCreated",
  "orderId": "uuid",
  "customerId": "uuid",
  "correlationId": "uuid",
  "createdAt": "2026-01-21T15:10:02",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ]
}
```

### Eventos Consumidos

**StockReserved** (routing key: `stock.reserved`):
```json
{
  "orderId": "uuid",
  "status": "RESERVED",
  "timestamp": "2026-01-21T15:10:02"
}
```

**StockRejected** (routing key: `stock.rejected`):
```json
{
  "orderId": "uuid",
  "status": "REJECTED",
  "reason": "Insufficient stock for product {uuid}",
  "timestamp": "2026-01-21T15:10:02"
}
```

## 🔄 Flujo de Procesamiento

1. Cliente envía `POST /api/v1/orders`
2. Order Service crea pedido con estado `PENDING` en PostgreSQL
3. Order Service publica evento `OrderCreated` a RabbitMQ
4. Order Service responde inmediatamente al cliente (201)
5. **Procesamiento asíncrono:**
   - Inventory Service procesa el pedido
   - Publica `StockReserved` o `StockRejected`
6. Order Service consume el resultado
7. Order Service actualiza estado a `CONFIRMED` o `CANCELLED`
8. Cliente puede consultar estado con `GET /api/v1/orders/{orderId}`

## 🧪 Pruebas

### Ejecutar Tests

```bash
./mvnw test
```

### Prueba Manual

```bash
# Crear pedido
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
    "items": [
      {
        "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
        "quantity": 2
      }
    ],
    "shippingAddress": {
      "country": "EC",
      "city": "Quito",
      "street": "Av. Amazonas",
      "postalCode": "170135"
    },
    "paymentReference": "pay_abc123"
  }'

# Consultar pedido (reemplaza {orderId})
curl http://localhost:8080/api/v1/orders/{orderId}
```

## 📝 Logs

El servicio genera logs detallados:

```
INFO  - Creating order for customer: 9f7a1e2a-...
INFO  - Published OrderCreated event for order: 0d3f6b7c-...
INFO  - Received StockReserved event for order: 0d3f6b7c-...
INFO  - Order 0d3f6b7c-... confirmed
```

## 🤝 Integración con Inventory Service

Este servicio trabaja en conjunto con el **Inventory Service**:

1. Order Service publica `OrderCreated`
2. Inventory Service verifica y reserva stock
3. Inventory Service publica resultado
4. Order Service actualiza estado del pedido

## 📄 Licencia

Este proyecto es parte de un trabajo académico de la ESPE.

## 👥 Autor

Desarrollado como parte del curso de Sistemas Distribuidos - ESPE 2026
