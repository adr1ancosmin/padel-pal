package com.padelpal.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service Application
 * 
 * This service demonstrates ASYNCHRONOUS COMMUNICATION in microservices:
 * - Listens to booking events from RabbitMQ message queue
 * - Processes notifications independently of the booking flow
 * - Stores notification records for audit/tracking purposes
 * 
 * Benefits demonstrated:
 * 1. DECOUPLING: Notification logic is separate from booking logic
 * 2. SCALABILITY: Can scale independently based on notification load
 * 3. FAULT TOLERANCE: If this service is down, messages queue up in RabbitMQ
 */
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
