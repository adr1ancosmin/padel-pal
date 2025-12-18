package com.padelpal.notificationservice.service;

import com.padelpal.notificationservice.config.RabbitMQConfig;
import com.padelpal.notificationservice.dto.BookingEvent;
import com.padelpal.notificationservice.model.Notification;
import com.padelpal.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service  // Marks this class as a Spring service component (business logic layer, auto-instantiated by Spring)
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);
    private final NotificationRepository notificationRepository;

    public BookingEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)  // Subscribes to RabbitMQ queue - method called automatically when message arrives
    public void handleBookingEvent(BookingEvent event) {
        logger.info("Received booking event from RabbitMQ: {}", event);

        try {
            // Build notification message
            String message = String.format(
                "Booking Confirmed! Your booking #%d for Court %d has been confirmed. " +
                "Scheduled time: %s. Enjoy your padel game!",
                event.getBookingId(),
                event.getCourtId(),
                event.getBookingTime()
            );

            // Create and save notification to database
            Notification notification = new Notification(
                event.getUserId(),
                event.getBookingId(),
                message,
                "SENT",
                "BOOKING_CONFIRMATION"
            );
            notificationRepository.save(notification);
            
            logger.info("Notification created for user {}", notification.getUserId());

        } catch (Exception e) {
            logger.error("Failed to process booking event: {}", e.getMessage());
            throw e;  // RabbitMQ will redeliver the message
        }
    }
}
