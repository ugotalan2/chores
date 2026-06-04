package com.chores.api.repository;

import com.chores.api.entity.Bathroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BathroomRepository extends JpaRepository<Bathroom, UUID> {

    // Find all the active bathrooms
    List<Bathroom> findByIsActiveTrue();
}
