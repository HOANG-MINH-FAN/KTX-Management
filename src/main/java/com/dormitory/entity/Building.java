package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * ENTITY: Building (Tòa nhà)
 * ============================================================
 * Tòa nhà ký túc xá. Mỗi tòa nhà có nhiều phòng (1-N với Room).
 *
 * Việc tách Building thành entity riêng (thay vì chỉ là cột
 * String building trong Room) cho phép:
 * - Quản lý thông tin tòa nhà tập trung (địa chỉ, số tầng, mô tả)
 * - Thống kê theo từng tòa nhà
 * - Mở rộng thêm thuộc tính tòa nhà dễ dàng
 * ============================================================
 */
@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên tòa nhà, VD: "Tòa A", "Nhà B".
     */
    @NotBlank(message = "Tên tòa nhà không được để trống")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Địa chỉ hoặc vị trí trong khuôn viên ký túc xá.
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Số tầng của tòa nhà.
     */
    @Positive(message = "Số tầng phải lớn hơn 0")
    @Column(name = "total_floors", nullable = false)
    private int totalFloors = 1;

    /**
     * Mô tả chung về tòa nhà.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Danh sách phòng trong tòa nhà (One-to-Many).
     * mappedBy = tên trường Building trong Room.
     * cascade PERSIST,MERGE: khi lưu Building sẽ cascade sang Room.
     * orphanRemoval: nếu xóa Room khỏi list này thì Room bị xóa.
     */
    @OneToMany(mappedBy = "building", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Room> rooms = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===================== Constructors =====================

    public Building() {}

    public Building(String name, String address, int totalFloors) {
        this.name        = name;
        this.address     = address;
        this.totalFloors = totalFloors;
    }

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getTotalFloors() { return totalFloors; }
    public void setTotalFloors(int totalFloors) { this.totalFloors = totalFloors; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Tính tổng số phòng trong tòa nhà.
     */
    public int getTotalRooms() {
        return rooms.size();
    }

    @Override
    public String toString() {
        return "Building{id=" + id + ", name='" + name + "', totalFloors=" + totalFloors + "}";
    }
}
