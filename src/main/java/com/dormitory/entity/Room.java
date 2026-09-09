package com.dormitory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")

public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String building;

    @Column(name = "room_number")
    private String roomNumber;

    private Integer floor;
    private Integer capacity;
    private Integer occupied;
    private String status;

    public void updateStatus() {
        if (occupied == null || capacity == null) {
            this.status = "AVAILABLE";
        } else if (occupied >= capacity) {
            this.status = "FULL";
        } else {
            this.status = "AVAILABLE";
        }
    }

    public Room() {}

    public Integer getId() { return id; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getOccupied() { return occupied; }
    public void setOccupied(Integer occupied) { this.occupied = occupied; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

