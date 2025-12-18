package com.padelpal.userservice.repository;

import com.padelpal.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository provides: save(), findById(), findAll(), deleteById(), etc.
public interface UserRepository extends JpaRepository<User, Long> {
}
