package com.padelpal.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

@Configuration  // Marks this class as a configuration class (defines beans/settings for Spring)
public class RabbitMQConfig {

    public static final String BOOKING_QUEUE = "booking.queue";
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BOOKING_ROUTING_KEY = "booking.created";

    @Bean  // Tells Spring to create and manage this object as a bean (injectable dependency)
    public Queue bookingQueue() {
        return new Queue(BOOKING_QUEUE, true);  // true = durable (survives RabbitMQ restart)
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);  // Exchange routes messages to queues
    }

    @Bean
    public Binding binding(Queue bookingQueue, TopicExchange bookingExchange) {
        return BindingBuilder
                .bind(bookingQueue)          // Bind this queue
                .to(bookingExchange)         // to this exchange
                .with(BOOKING_ROUTING_KEY);  // when routing key matches
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();  // Converts Java objects to JSON
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
