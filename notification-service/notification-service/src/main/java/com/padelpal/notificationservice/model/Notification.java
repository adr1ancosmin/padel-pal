package com.padelpal.notificationservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a notification record.
 * 
 * Notifications are created asynchronously when booking events
 * are received from RabbitMQ.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long bookingId;
    
    @Column(length = 500)
    private String message;
    
    private String status;
    private String notificationType;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Long userId, Long bookingId, String message, 
                       String status, String notificationType) {
        this.userId = userId;
        this.bookingId = bookingId;
        this.message = message;
        this.status = status;
        this.notificationType = notificationType;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
