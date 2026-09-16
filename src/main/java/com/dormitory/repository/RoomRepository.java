package com.dormitory.repository;

import com.dormitory.entity.Room;
import com.dormitory.entity.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Room entity.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Lấy tất cả phòng theo tòa nhà.
     */
    List<Room> findByBuildingId(Long buildingId);

    /**
     * Lấy phòng theo trạng thái (AVAILABLE, FULL, MAINTENANCE).
     */
    List<Room> findByStatus(RoomStatus status);

    /**
     * Lấy phòng còn chỗ trống (AVAILABLE).
     */
    List<Room> findByStatusOrderByBuildingNameAscRoomNumberAsc(RoomStatus status);

    /**
     * Tổng số chỗ trống trong toàn bộ KTX.
     */
    @Query("SELECT COALESCE(SUM(r.capacity - r.occupied), 0) FROM Room r WHERE r.status != 'MAINTENANCE'")
    Integer getTotalEmptyPlaces();

    /**
     * Đếm số phòng theo trạng thái.
     */
    long countByStatus(RoomStatus status);

    /**
     * Kiểm tra phòng có trong tòa nhà với số phòng đó không (tránh trùng).
     */
    boolean existsByBuildingIdAndRoomNumber(Long buildingId, String roomNumber);
}
