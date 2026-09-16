package com.dormitory.service;

import com.dormitory.entity.Building;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho tòa nhà.
 * Trừu tượng hóa (Abstraction) — tách contract khỏi implementation.
 */
public interface BuildingService {

    List<Building> findAll();

    Optional<Building> findById(Long id);

    Building save(Building building);

    void deleteById(Long id);

    boolean existsByName(String name);
}
