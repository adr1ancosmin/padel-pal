package com.padelpal.bookingservice.controller;

import com.padelpal.bookingservice.dto.BookingEvent;
import com.padelpal.bookingservice.model.Booking;
import com.padelpal.bookingservice.repository.BookingRepository;
import com.padelpal.bookingservice.service.BookingEventPublisher;
import com.padelpal.bookingservice.service.ExternalServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController  // Combines @Controller + @ResponseBody: marks this as a REST API controller that returns JSON
@RequestMapping("/api/bookings")  // Base URL path - all endpoints in this class start with /api/bookings
@CrossOrigin(origins = "*")  // CORS: allows requests from any origin (needed for frontend on different port)
public class BookingController {

    private final BookingRepository repo;
    private final ExternalServiceClient client;       // Calls User/Court services via HTTP
    private final BookingEventPublisher eventPublisher;  // Sends messages to RabbitMQ

    public BookingController(BookingRepository repo, 
                            ExternalServiceClient client,
                            BookingEventPublisher eventPublisher) {
        this.repo = repo;
        this.client = client;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping  // Handles HTTP GET requests to /api/bookings
    public List<Booking> getAll() {
        return repo.findAll();
    }

    @GetMapping("/user/{userId}")  // Handles GET /api/bookings/user/{userId}
    public List<Booking> getByUser(@PathVariable Long userId) {  // @PathVariable extracts {userId} from URL
        return repo.findByUserId(userId);
    }

    @PostMapping  // Handles HTTP POST requests to /api/bookings
    public ResponseEntity<?> create(@RequestParam Long userId,  // @RequestParam extracts ?userId=X from query string
                                    @RequestParam Long courtId) {

        // Validate user exists (HTTP call to User Service)
        if (!client.userExists(userId)) {
            return ResponseEntity.badRequest().body("User does not exist");
        }
        
        // Validate court exists (HTTP call to Court Service)
        if (!client.courtExists(courtId)) {
            return ResponseEntity.badRequest().body("Court does not exist");
        }

        // Save booking to database
        Booking booking = new Booking(userId, courtId, LocalDateTime.now());
        repo.save(booking);

        // Publish event to RabbitMQ (async - Notification Service will pick this up)
        BookingEvent event = new BookingEvent(
                booking.getId(),
                booking.getUserId(),
                booking.getCourtId(),
                booking.getTime(),
                "BOOKING_CREATED"
        );
        eventPublisher.publishBookingCreated(event);

        return ResponseEntity.ok(booking);
    }

    @DeleteMapping("/{id}")  // Handles HTTP DELETE requests to /api/bookings/{id}
    public ResponseEntity<?> delete(@PathVariable Long id) {  // @PathVariable extracts {id} from URL
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.ok("Booking deleted");
    }
}
