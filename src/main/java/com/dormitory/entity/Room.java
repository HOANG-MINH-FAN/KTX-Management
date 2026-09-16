package com.dormitory.entity;

import com.dormitory.entity.enums.RoomStatus;
import com.dormitory.entity.enums.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * ENTITY: Room (Phòng ở)
 * ============================================================
 * Phòng thuộc một tòa nhà (ManyToOne với Building).
 * Sử dụng Enum thay String cho status/type (type-safe, OOP).
 *
 * Nghiệp vụ quan trọng:
 * - occupied tăng khi Contract ACTIVE được tạo
 * - occupied giảm khi Contract TERMINATED/EXPIRED
 * - status tự cập nhật theo occupied/capacity
 * ============================================================
 */
@Entity
@Table(
    name = "rooms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_rooms_building_number",
            columnNames = {"building_id", "room_number"}
        )
    }
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tòa nhà chứa phòng này.
     * LAZY loading để tránh N+1 queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    /**
     * Số phòng, VD: "101", "202A".
     */
    @NotBlank(message = "Số phòng không được để trống")
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    /**
     * Tầng trong tòa nhà.
     */
    @Min(value = 1, message = "Tầng phải >= 1")
    @Column(name = "floor", nullable = false)
    private int floor = 1;

    /**
     * Sức chứa tối đa (số giường).
     */
    @Min(value = 1, message = "Sức chứa phải >= 1")
    @Column(name = "capacity", nullable = false)
    private int capacity = 4;

    /**
     * Số chỗ hiện đang có sinh viên ở.
     * Được cập nhật tự động khi tạo/kết thúc hợp đồng.
     */
    @Min(value = 0, message = "Số chỗ đã chiếm không được âm")
    @Column(name = "occupied", nullable = false)
    private int occupied = 0;

    /**
     * Loại phòng theo giới tính.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType = RoomType.MIXED;

    /**
     * Giá phòng theo tháng (VND).
     * Đây là giá tham chiếu khi tạo hợp đồng.
     */
    @DecimalMin(value = "0.0", message = "Tiền phòng không được âm")
    @Column(name = "room_fee_per_month", nullable = false, precision = 15, scale = 2)
    private BigDecimal roomFeePerMonth = BigDecimal.ZERO;

    /**
     * Trạng thái hiện tại của phòng.
     * Được cập nhật tự động qua updateStatus().
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===================== Business Logic =====================

    /**
     * Cập nhật trạng thái phòng dựa trên số chỗ đã chiếm.
     *
     * Nghiệp vụ:
     * - Nếu đang MAINTENANCE → không thay đổi (ưu tiên cao nhất)
     * - Nếu occupied >= capacity → FULL
     * - Ngược lại → AVAILABLE
     *
     * Gọi phương thức này sau mỗi lần thay đổi occupied.
     */
    public void updateStatus() {
        if (this.status == RoomStatus.MAINTENANCE) {
            return; // Phòng bảo trì không tự chuyển trạng thái
        }
        if (this.occupied >= this.capacity) {
            this.status = RoomStatus.FULL;
        } else {
            this.status = RoomStatus.AVAILABLE;
        }
    }

    /**
     * Tăng số chỗ đã chiếm (khi sinh viên vào ở).
     *
     * @throws IllegalStateException nếu phòng đã đầy hoặc đang bảo trì
     */
    public void incrementOccupied() {
        if (this.status == RoomStatus.MAINTENANCE) {
            throw new IllegalStateException("Phòng " + roomNumber + " đang bảo trì, không thể nhận sinh viên.");
        }
        if (this.occupied >= this.capacity) {
            throw new IllegalStateException("Phòng " + roomNumber + " đã đầy.");
        }
        this.occupied++;
        updateStatus();
    }

    /**
     * Giảm số chỗ đã chiếm (khi sinh viên rời đi).
     */
    public void decrementOccupied() {
        if (this.occupied > 0) {
            this.occupied--;
        }
        updateStatus();
    }

    /**
     * Tính số chỗ còn trống.
     */
    public int getAvailableSlots() {
        return Math.max(0, this.capacity - this.occupied);
    }

    /**
     * Kiểm tra phòng còn nhận được sinh viên không.
     */
    public boolean isAvailableForOccupancy() {
        return this.status == RoomStatus.AVAILABLE && this.occupied < this.capacity;
    }

    // ===================== Constructors =====================

    public Room() {}

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupied() { return occupied; }
    public void setOccupied(int occupied) {
        this.occupied = occupied;
        updateStatus();
    }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public BigDecimal getRoomFeePerMonth() { return roomFeePerMonth; }
    public void setRoomFeePerMonth(BigDecimal roomFeePerMonth) { this.roomFeePerMonth = roomFeePerMonth; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Tên phòng đầy đủ: "Tòa A - 101".
     */
    public String getFullRoomName() {
        String buildingName = (building != null) ? building.getName() : "?";
        return buildingName + " - " + roomNumber;
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", roomNumber='" + roomNumber
                + "', capacity=" + capacity + ", occupied=" + occupied
                + ", status=" + status + "}";
    }
}
