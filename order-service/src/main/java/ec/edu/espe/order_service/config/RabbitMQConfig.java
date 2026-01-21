package ec.edu.espe.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "orders.exchange";
    
    // Queues
    public static final String INVENTORY_ORDERS_QUEUE = "inventory.orders.queue";
    public static final String ORDERS_RESULTS_QUEUE = "orders.results.queue";
    
    // Routing Keys
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
    public static final String STOCK_REJECTED_ROUTING_KEY = "stock.rejected";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue inventoryOrdersQueue() {
        return QueueBuilder.durable(INVENTORY_ORDERS_QUEUE).build();
    }

    @Bean
    public Queue ordersResultsQueue() {
        return QueueBuilder.durable(ORDERS_RESULTS_QUEUE).build();
    }

    @Bean
    public Binding bindingOrderCreated(Queue inventoryOrdersQueue, TopicExchange exchange) {
        return BindingBuilder.bind(inventoryOrdersQueue)
                .to(exchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingStockReserved(Queue ordersResultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ordersResultsQueue)
                .to(exchange)
                .with(STOCK_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingStockRejected(Queue ordersResultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ordersResultsQueue)
                .to(exchange)
                .with(STOCK_REJECTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
