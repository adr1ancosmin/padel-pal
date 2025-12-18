package com.padelpal.courtservice.controller;

import com.padelpal.courtservice.model.Court;
import com.padelpal.courtservice.repository.CourtRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@CrossOrigin(origins = "*")
public class CourtController {

    private final CourtRepository repository;

    public CourtController(CourtRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Court createCourt(@RequestBody Court court) {
        return repository.save(court);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCourtById(@PathVariable Long id) {
        return repository.findById(id)
                .map(court -> ResponseEntity.ok((Object) court))
                .orElseGet(() -> ResponseEntity.badRequest().body("Court not found"));
    }

    @GetMapping
    public List<Court> getAllCourts() {
        return repository.findAll();
    }
}
