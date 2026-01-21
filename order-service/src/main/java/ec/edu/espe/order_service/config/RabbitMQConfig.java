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

    public static final String EXCHANGE_NAME = "orders.exchange";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String INVENTORY_RESPONSE_ROUTING_KEY = "inventory.response";
    
    public static final String ORDER_CREATED_QUEUE = "inventory.requests"; 
    public static final String ORDER_RESPONSE_QUEUE = "order.responses"; 

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE);
    }

    @Bean
    public Queue orderResponseQueue() {
        return new Queue(ORDER_RESPONSE_QUEUE);
    }

    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingOrderResponse(Queue orderResponseQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderResponseQueue).to(exchange).with(INVENTORY_RESPONSE_ROUTING_KEY);
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
