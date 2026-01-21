# E-Commerce Microservices System

Sistema de microservicios para e-commerce con arquitectura event-driven, comunicación asíncrona mediante RabbitMQ y procesamiento de pedidos con verificación de inventario.

## 📋 Descripción

Este proyecto implementa un sistema completo de procesamiento de pedidos utilizando:
- **Arquitectura de Microservicios** desacoplados
- **Mensajería Asíncrona** con RabbitMQ
- **Event-Driven Architecture** para escalabilidad
- **Dockerización Completa** con Docker Compose

## 🏗️ Arquitectura del Sistema

```
┌─────────────┐                                    ┌──────────────────┐
│   Cliente   │                                    │   PostgreSQL     │
└──────┬──────┘                                    │  (Orders DB)     │
       │                                           └────────▲─────────┘
       │ POST /api/v1/orders                                │
       ▼                                                    │
┌──────────────────┐         OrderCreated         ┌────────┴─────────┐
│  Order Service   │────────────────────────────>  │                  │
│  (Port 8080)     │                               │    RabbitMQ      │
└──────────────────┘                               │  (Exchange)      │
       │                                           └────────┬─────────┘
       │ StockReserved/Rejected                            │
       │<──────────────────────────────────────────────────┘
       │                                                    │
       │                                                    │ OrderCreated
       │                                                    ▼
       │                                           ┌──────────────────┐
       │                                           │ Inventory Service│
       │                                           │  (Port 8081)     │
       │                                           └────────┬─────────┘
       │                                                    │
       │                                                    ▼
       │                                           ┌──────────────────┐
       │                                           │   PostgreSQL     │
       │                                           │ (Inventory DB)   │
       │                                           └──────────────────┘
       │
       │ GET /api/v1/orders/{orderId}
       ▼
┌──────────────────┐
│   Cliente        │
└──────────────────┘
```

## 🚀 Microservicios

### 1. Order Service
- **Puerto:** 8080
- **Base de Datos:** PostgreSQL (orders_db)
- **Responsabilidades:**
  - Crear pedidos en estado PENDING
  - Publicar eventos OrderCreated
  - Consumir eventos StockReserved/StockRejected
  - Actualizar estado de pedidos (CONFIRMED/CANCELLED)
  - Exponer API REST para consultar pedidos

### 2. Inventory Service
- **Puerto:** 8081
- **Base de Datos:** PostgreSQL (inventory_db)
- **Responsabilidades:**
  - Consumir eventos OrderCreated
  - Verificar disponibilidad de stock
  - Reservar stock si está disponible
  - Publicar eventos StockReserved/StockRejected
  - Exponer API REST para consultar stock

## 📡 API Endpoints

### Order Service

#### Crear Pedido
```http
POST /api/v1/orders
Content-Type: application/json

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

**Respuesta (201):**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "status": "PENDING",
  "message": "Order received. Inventory check in progress."
}
```

#### Consultar Pedido
```http
GET /api/v1/orders/{orderId}
```

**Respuesta - Pedido Confirmado (200):**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "status": "CONFIRMED",
  "items": [...],
  "updatedAt": "2026-01-21T15:10:02Z"
}
```

**Respuesta - Pedido Cancelado (200):**
```json
{
  "orderId": "0d3f6b7c-9a8e-4c12-8f67-5e0c2a1b9d34",
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "status": "CANCELLED",
  "reason": "Insufficient stock for product ...",
  "items": [...],
  "updatedAt": "2026-01-21T15:10:02Z"
}
```

### Inventory Service

#### Consultar Stock
```http
GET /api/v1/products/{productId}/stock
```

**Respuesta (200):**
```json
{
  "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
  "availableStock": 25,
  "reservedStock": 3,
  "updatedAt": "2026-01-21T15:08:10Z"
}
```

## 📨 Eventos RabbitMQ

### Configuración

**Exchange:** `orders.exchange` (TopicExchange)

**Queues:**
- `inventory.orders.queue` - Inventory Service consume OrderCreated
- `orders.results.queue` - Order Service consume respuestas

**Routing Keys:**
- `order.created` - Para eventos OrderCreated
- `stock.reserved` - Para eventos StockReserved
- `stock.rejected` - Para eventos StockRejected

### Contratos de Eventos

**OrderCreated:**
```json
{
  "eventType": "OrderCreated",
  "orderId": "uuid",
  "customerId": "uuid",
  "correlationId": "uuid",
  "createdAt": "2026-01-21T15:10:02",
  "items": [{"productId": "uuid", "quantity": 2}]
}
```

**StockReserved:**
```json
{
  "orderId": "uuid",
  "status": "RESERVED",
  "timestamp": "2026-01-21T15:10:02"
}
```

**StockRejected:**
```json
{
  "orderId": "uuid",
  "status": "REJECTED",
  "reason": "Insufficient stock for product {uuid}",
  "timestamp": "2026-01-21T15:10:02"
}
```

## 🐳 Ejecución con Docker Compose

### Requisitos Previos
- Docker 20.10+
- Docker Compose 2.0+

### Iniciar el Sistema Completo

```bash
# Construir imágenes
docker-compose build

# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Ver estado de servicios
docker-compose ps
```

### Servicios y Puertos

| Servicio | Puerto Host | Descripción |
|----------|-------------|-------------|
| order-service | 8080 | API REST de pedidos |
| inventory-service | 8081 | API REST de inventario |
| postgres-orders | 5433 | Base de datos de pedidos |
| postgres-inventory | 5434 | Base de datos de inventario |
| rabbitmq | 5672 | AMQP |
| rabbitmq-management | 15672 | UI de administración |

### Detener el Sistema

```bash
# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (datos)
docker-compose down -v
```

## 🧪 Pruebas del Flujo Completo

### 1. Verificar que los servicios estén corriendo

```bash
docker-compose ps
```

Todos los servicios deben estar en estado `Up` y `healthy`.

### 2. Verificar Stock Disponible

```bash
curl http://localhost:8081/api/v1/products/a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d/stock
```

### 3. Crear un Pedido

```bash
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
```

Guarda el `orderId` de la respuesta.

### 4. Consultar Estado del Pedido

```bash
curl http://localhost:8080/api/v1/orders/{orderId}
```

Espera unos segundos y vuelve a consultar. El estado debe cambiar de `PENDING` a `CONFIRMED` o `CANCELLED`.

### 5. Verificar RabbitMQ Management UI

Accede a http://localhost:15672
- Usuario: `guest`
- Password: `guest`

Verifica las colas y mensajes procesados.

## 🔄 Flujo de Procesamiento

1. **Cliente** envía `POST /api/v1/orders` → **Order Service**
2. **Order Service** crea pedido con estado `PENDING` en PostgreSQL
3. **Order Service** publica evento `OrderCreated` → **RabbitMQ**
4. **Inventory Service** consume `OrderCreated` desde RabbitMQ
5. **Inventory Service** verifica stock en PostgreSQL
6. **Si hay stock:**
   - Reserva stock (decrementa `availableStock`, incrementa `reservedStock`)
   - Publica `StockReserved` → RabbitMQ
   - **Order Service** consume `StockReserved`
   - **Order Service** actualiza pedido a `CONFIRMED`
7. **Si NO hay stock:**
   - Publica `StockRejected` → RabbitMQ
   - **Order Service** consume `StockRejected`
   - **Order Service** actualiza pedido a `CANCELLED`
8. **Cliente** consulta `GET /api/v1/orders/{orderId}` para ver estado final

## 📊 Productos de Prueba Precargados

El Inventory Service incluye 5 productos con stock inicial:

| Product ID | Available Stock | Reserved Stock |
|------------|----------------|----------------|
| `a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d` | 50 | 0 |
| `b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f` | 30 | 0 |
| `c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f` | 100 | 0 |
| `d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a` | 15 | 0 |
| `e7f8a9b0-1c2d-3e4f-5a6b-7c8d9e0f1a2b` | 75 | 0 |

## 🛠️ Tecnologías

### Order Service
- Spring Boot 4.0.1
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- PostgreSQL
- Maven
- Lombok

### Inventory Service
- Spring Boot 4.0.1
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- PostgreSQL
- Gradle
- Lombok

### Infraestructura
- Docker & Docker Compose
- PostgreSQL 15
- RabbitMQ 3 (con Management UI)

## 📁 Estructura del Proyecto

```
DistriControlLectura/
├── order-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/ec/edu/espe/order_service/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── dto/
│   │       │   ├── listener/
│   │       │   ├── model/
│   │       │   ├── repository/
│   │       │   └── service/
│   │       └── resources/
│   │           └── application.yaml
│   ├── Dockerfile
│   ├── pom.xml
│   └── .env.example
├── inventory-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/ec/edu/espe/inventory/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── dto/
│   │       │   ├── messaging/
│   │       │   ├── model/
│   │       │   ├── repository/
│   │       │   └── service/
│   │       └── resources/
│   │           ├── application.properties
│   │           └── data.sql
│   ├── Dockerfile
│   ├── build.gradle
│   └── .env.example
├── docker-compose.yml
├── .env.example
└── README.md
```

## 🔒 Características de Robustez

- **Transacciones Atómicas:** Todas las operaciones de BD son transaccionales
- **Bloqueo Pesimista:** Previene condiciones de carrera en actualizaciones de stock
- **Health Checks:** Docker Compose verifica salud de servicios antes de iniciar dependientes
- **Reintentos Automáticos:** `restart: on-failure` en servicios
- **Persistencia de Datos:** Volúmenes de Docker para PostgreSQL y RabbitMQ
- **Logging Detallado:** Trazabilidad completa de eventos y operaciones

## 📝 Logs y Debugging

### Ver logs de un servicio específico

```bash
docker-compose logs -f order-service
docker-compose logs -f inventory-service
docker-compose logs -f rabbitmq
```

### Acceder a un contenedor

```bash
docker exec -it order-service sh
docker exec -it inventory-service sh
docker exec -it postgres-orders psql -U postgres -d orders_db
docker exec -it postgres-inventory psql -U postgres -d inventory_db
```

## 🤝 Contribución

Este proyecto es parte de un trabajo académico de la ESPE - Sistemas Distribuidos 2026.

## 📄 Licencia

Proyecto académico - ESPE 2026

---

**Desarrollado por:** Estudiantes de Sistemas Distribuidos - ESPE  
**Fecha:** Enero 2026
