package com.padelpal.userservice.controller;

import com.padelpal.userservice.model.User;
import com.padelpal.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController  // Combines @Controller + @ResponseBody: marks this as a REST API controller that returns JSON
@RequestMapping("/api/users")  // Base URL path - all endpoints in this class start with /api/users
@CrossOrigin(origins = "*")  // CORS: allows requests from any origin (needed for frontend on different port)
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping  // Handles HTTP POST requests to /api/users
    public User createUser(@RequestBody User user) {  // @RequestBody converts JSON request body to User object
        return repository.save(user);  // INSERT INTO users
    }

    @GetMapping("/{id}")  // Handles GET /api/users/{id}
    public ResponseEntity<?> getUserById(@PathVariable Long id) {  // @PathVariable extracts {id} from URL
        Optional<User> optionalUser = repository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        return ResponseEntity.ok(optionalUser.get());
    }

    @GetMapping  // Handles HTTP GET requests to /api/users
    public List<User> getAllUsers() {
        return repository.findAll();  // SELECT * FROM users
    }
}
