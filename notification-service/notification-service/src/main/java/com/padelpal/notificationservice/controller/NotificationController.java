package com.padelpal.notificationservice.controller;

import com.padelpal.notificationservice.model.Notification;
import com.padelpal.notificationservice.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for notification endpoints.
 * 
 * Provides endpoints to query notifications that were
 * created asynchronously via RabbitMQ event processing.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Get all notifications in the system.
     */
    @GetMapping
    public List<Notification> getAllNotifications() {
        return repository.findAll();
    }

    /**
     * Get all notifications for a specific user.
     */
    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsByUser(@PathVariable Long userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Get all notifications for a specific booking.
     */
    @GetMapping("/booking/{bookingId}")
    public List<Notification> getNotificationsByBooking(@PathVariable Long bookingId) {
        return repository.findByBookingId(bookingId);
    }

    /**
     * Get a specific notification by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Health check endpoint to verify service is running.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification Service is running!");
    }
}
