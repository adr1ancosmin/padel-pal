package com.padelpal.bookingservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long courtId;
    private LocalDateTime time;

    public Booking() {}

    public Booking(Long userId, Long courtId, LocalDateTime time) {
        this.userId = userId;
        this.courtId = courtId;
        this.time = time;
    }

    public Long getId() { return id; }

    public Long getUserId() { return userId; }

    public Long getCourtId() { return courtId; }

    public LocalDateTime getTime() { return time; }
}
