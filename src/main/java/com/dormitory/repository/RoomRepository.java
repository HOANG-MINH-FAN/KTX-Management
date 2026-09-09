package com.dormitory.repository;

import com.dormitory.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    @Query("SELECT SUM(r.capacity - r.occupied) FROM Room r")
    Integer getTotalEmptyPlaces();
}

