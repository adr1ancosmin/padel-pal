package com.padelpal.bookingservice.service;

import com.padelpal.bookingservice.config.RabbitMQConfig;
import com.padelpal.bookingservice.dto.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springf ramework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service  // Marks this class as a Spring service component (business logic layer, auto-instantiated by Spring)
public class BookingEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;  // Spring's helper for sending messages to RabbitMQ

    public BookingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookingCreated(BookingEvent event) {
        try {
            logger.info("Publishing booking created event: {}", event);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,     // "booking.exchange"
                    RabbitMQConfig.BOOKING_ROUTING_KEY,  // "booking.created"
                    event                                 // BookingEvent object → converted to JSON
            );
            
            logger.info("Booking event published successfully");
                    
        } catch (Exception e) {
            // If RabbitMQ is down, log error but don't fail the booking
            logger.error("Failed to publish booking event: {}. Booking will still be created.", e.getMessage());
        }
    }
}
