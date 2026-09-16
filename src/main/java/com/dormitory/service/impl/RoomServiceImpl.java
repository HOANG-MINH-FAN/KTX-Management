package com.dormitory.service.impl;

import com.dormitory.entity.Room;
import com.dormitory.entity.enums.RoomStatus;
import com.dormitory.repository.RoomRepository;
import com.dormitory.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByBuildingId(Long buildingId) {
        return roomRepository.findByBuildingId(buildingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAvailableRooms() {
        return roomRepository.findByStatusOrderByBuildingNameAscRoomNumberAsc(RoomStatus.AVAILABLE);
    }

    @Override
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalEmptyPlaces() {
        Integer result = roomRepository.getTotalEmptyPlaces();
        return result != null ? result : 0;
    }

    @Override
    public void updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ID: " + roomId));
        room.setStatus(status);
        roomRepository.save(room);
    }
}
