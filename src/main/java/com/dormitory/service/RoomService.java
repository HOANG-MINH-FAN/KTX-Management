package com.dormitory.service;

import com.dormitory.entity.Room;
import com.dormitory.entity.enums.RoomStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho phòng ở.
 */
public interface RoomService {

    List<Room> findAll();

    Optional<Room> findById(Long id);

    /** Lấy phòng theo tòa nhà. */
    List<Room> findByBuildingId(Long buildingId);

    /** Lấy phòng còn chỗ trống. */
    List<Room> findAvailableRooms();

    Room save(Room room);

    void deleteById(Long id);

    /** Tổng số chỗ trống toàn KTX. */
    int getTotalEmptyPlaces();

    /** Cập nhật trạng thái phòng. */
    void updateRoomStatus(Long roomId, RoomStatus status);
}
