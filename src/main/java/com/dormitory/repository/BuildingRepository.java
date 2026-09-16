package com.dormitory.repository;

import com.dormitory.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Building entity.
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    Optional<Building> findByName(String name);

    boolean existsByName(String name);
}
