package com.padelpal.notificationservice.repository;

import com.padelpal.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a specific user
     */
    List<Notification> findByUserId(Long userId);
    
    /**
     * Find all notifications for a specific booking
     */
    List<Notification> findByBookingId(Long bookingId);
    
    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(String status);
}
