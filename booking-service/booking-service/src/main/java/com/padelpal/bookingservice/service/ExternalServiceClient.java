package com.padelpal.bookingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service  // Marks this class as a Spring service component (business logic layer, auto-instantiated by Spring)
public class ExternalServiceClient {

    private final WebClient webClientUser;   // For calling User Service
    private final WebClient webClientCourt;  // For calling Court Service

    public ExternalServiceClient(
            @Value("${user.service.url}") String userUrl,  // @Value injects value from application.properties
            @Value("${court.service.url}") String courtUrl  // @Value injects value from application.properties
    ) {
        this.webClientUser = WebClient.create(userUrl);
        this.webClientCourt = WebClient.create(courtUrl);
    }

    // HTTP GET to User Service - returns true if user exists
    public boolean userExists(Long userId) {
        try {
            webClientUser.get().uri("/" + userId).retrieve().bodyToMono(String.class).block();
            return true;
        } catch (Exception e) {
            return false;  // 404 or connection error
        }
    }

    // HTTP GET to Court Service - returns true if court exists
    public boolean courtExists(Long courtId) {
        try {
            webClientCourt.get().uri("/" + courtId).retrieve().bodyToMono(String.class).block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
