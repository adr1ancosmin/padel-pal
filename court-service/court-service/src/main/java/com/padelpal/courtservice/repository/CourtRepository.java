package com.padelpal.courtservice.repository;

import com.padelpal.courtservice.model.Court;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court, Long> {
}
