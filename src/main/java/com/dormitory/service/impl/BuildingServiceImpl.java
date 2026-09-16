package com.dormitory.service.impl;

import com.dormitory.entity.Building;
import com.dormitory.repository.BuildingRepository;
import com.dormitory.service.BuildingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation của BuildingService.
 * @Service → Spring tự đăng ký bean vào ApplicationContext.
 * @Transactional → Mọi thao tác ghi đều trong transaction.
 */
@Service
@Transactional
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingServiceImpl(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Building> findById(Long id) {
        return buildingRepository.findById(id);
    }

    @Override
    public Building save(Building building) {
        return buildingRepository.save(building);
    }

    @Override
    public void deleteById(Long id) {
        buildingRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return buildingRepository.existsByName(name);
    }
}
