package com.padelpal.notificationservice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a booking event received from RabbitMQ.
 * This must match the structure sent by the Booking Service.
 */
public class BookingEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long bookingId;
    private Long userId;
    private Long courtId;
    private LocalDateTime bookingTime;
    private String eventType;
    private LocalDateTime eventTimestamp;

    public BookingEvent() {}

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourtId() {
        return courtId;
    }

    public void setCourtId(Long courtId) {
        this.courtId = courtId;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    @Override
    public String toString() {
        return "BookingEvent{" +
                "bookingId=" + bookingId +
                ", userId=" + userId +
                ", courtId=" + courtId +
                ", bookingTime=" + bookingTime +
                ", eventType='" + eventType + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
}
