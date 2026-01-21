package ec.edu.espe.order_service.service;

import ec.edu.espe.order_service.config.RabbitMQConfig;
import ec.edu.espe.order_service.dto.*;
import ec.edu.espe.order_service.model.Order;
import ec.edu.espe.order_service.model.OrderItem;
import ec.edu.espe.order_service.model.OrderStatus;
import ec.edu.espe.order_service.model.ShippingAddress;
import ec.edu.espe.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.PENDING);
        order.setMessage("Order received. Inventory check in progress.");
        order.setPaymentReference(request.getPaymentReference());
        
        if (request.getShippingAddress() != null) {
            ShippingAddress address = new ShippingAddress();
            address.setCountry(request.getShippingAddress().getCountry());
            address.setCity(request.getShippingAddress().getCity());
            address.setStreet(request.getShippingAddress().getStreet());
            address.setPostalCode(request.getShippingAddress().getPostalCode());
            address.setZip(request.getShippingAddress().getZip()); 
            order.setShippingAddress(address);
        }

        List<OrderItem> items = request.getItems().stream().map(itemDto -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemDto.getProductId());
            item.setQuantity(itemDto.getQuantity());
            return item;
        }).collect(Collectors.toList());
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        // Publish Event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventType("OrderCreated")
                .orderId(savedOrder.getOrderId())
                .correlationId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .items(request.getItems())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, event);
        log.info("Published OrderCreated event for order: {}", savedOrder.getOrderId());

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .message(order.getMessage())
                .reason(order.getReason())
                .items(itemDtos)
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    @Transactional
    public void confirmOrder(String orderId) {
         Order order = orderRepository.findById(orderId).orElse(null);
         if(order != null) {
             order.setStatus(OrderStatus.CONFIRMED);
             // order.setMessage("Order Confirmed"); // Optional
             orderRepository.save(order);
             log.info("Order {} confirmed", orderId);
         } else {
             log.warn("Order {} not found for confirmation", orderId);
         }
    }

    @Transactional
    public void cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if(order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setReason(reason);
            orderRepository.save(order);
            log.info("Order {} cancelled: {}", orderId, reason);
        } else {
             log.warn("Order {} not found for cancellation", orderId);
        }
    }
}
