package com.dormitory.security;

import com.dormitory.entity.Admin;
import com.dormitory.entity.Student;
import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.entity.enums.RegistrationStatus;
import com.dormitory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================
 * DataInitializer — Khởi tạo dữ liệu mẫu khi ứng dụng khởi động
 * ============================================================
 * Chỉ chạy khi bảng users trống (lần đầu tiên).
 * Đảm bảo password được mã hóa BCrypt đúng cách thay vì hardcode.
 *
 * Tài khoản mẫu tạo ra:
 *   ADMIN:
 *     username: admin       / password: admin@123
 *   STUDENT:
 *     username: sv001       / password: sv@123456  (Nguyễn Văn An)
 *     username: sv002       / password: sv@123456  (Trần Thị Bình)
 *     username: sv003       / password: sv@123456  (Lê Quốc Cường)
 *
 * Dữ liệu nghiệp vụ mẫu:
 *   - sv001 có đơn đăng ký PENDING
 *   - sv002 có hợp đồng ACTIVE
 *   - Một vi phạm mẫu cho sv002
 * ============================================================
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository         userRepository;
    private final StudentRepository      studentRepository;
    private final AdminRepository        adminRepository;
    private final RoomRepository         roomRepository;
    private final RegistrationRepository registrationRepository;
    private final ContractRepository     contractRepository;
    private final ViolationRepository    violationRepository;
    private final PasswordEncoder        passwordEncoder;

    public DataInitializer(UserRepository         userRepository,
                           StudentRepository      studentRepository,
                           AdminRepository        adminRepository,
                           RoomRepository         roomRepository,
                           RegistrationRepository registrationRepository,
                           ContractRepository     contractRepository,
                           ViolationRepository    violationRepository,
                           PasswordEncoder        passwordEncoder) {
        this.userRepository         = userRepository;
        this.studentRepository      = studentRepository;
        this.adminRepository        = adminRepository;
        this.roomRepository         = roomRepository;
        this.registrationRepository = registrationRepository;
        this.contractRepository     = contractRepository;
        this.violationRepository    = violationRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Chỉ seed nếu chưa có dữ liệu
        if (userRepository.count() > 0) {
            log.info("DataInitializer: Bảng users đã có dữ liệu, bỏ qua seed.");
            return;
        }

        log.info("DataInitializer: Bắt đầu tạo dữ liệu mẫu...");

        // ============================================================
        // 1. Tạo tài khoản ADMIN
        // ============================================================
        Admin admin = new Admin("admin", passwordEncoder.encode("admin@123"));
        adminRepository.save(admin);
        log.info("  → Tạo Admin: admin / admin@123");

        // ============================================================
        // 2. Tạo tài khoản STUDENT
        // ============================================================
        String hashedPassword = passwordEncoder.encode("sv@123456");

        Student sv1 = new Student("sv001", hashedPassword, "SV2024001", "Nguyễn Văn An");
        sv1.setPhone("0901234561");
        sv1.setEmail("nguyenvanan@email.com");
        sv1.setFaculty("Công nghệ thông tin");
        sv1.setGender("MALE");
        sv1.setDateOfBirth(LocalDate.of(2002, 5, 15));
        sv1.setHometown("Hà Nội");

        Student sv2 = new Student("sv002", hashedPassword, "SV2024002", "Trần Thị Bình");
        sv2.setPhone("0901234562");
        sv2.setEmail("tranthib@email.com");
        sv2.setFaculty("Kinh tế");
        sv2.setGender("FEMALE");
        sv2.setDateOfBirth(LocalDate.of(2003, 8, 20));
        sv2.setHometown("Hồ Chí Minh");

        Student sv3 = new Student("sv003", hashedPassword, "SV2024003", "Lê Quốc Cường");
        sv3.setPhone("0901234563");
        sv3.setEmail("lequoccuong@email.com");
        sv3.setFaculty("Kỹ thuật xây dựng");
        sv3.setGender("MALE");
        sv3.setDateOfBirth(LocalDate.of(2001, 12, 3));
        sv3.setHometown("Đà Nẵng");

        studentRepository.save(sv1);
        studentRepository.save(sv2);
        studentRepository.save(sv3);
        log.info("  → Tạo 3 sinh viên mẫu (sv001, sv002, sv003 / sv@123456)");

        // ============================================================
        // 3. Tạo đơn đăng ký mẫu cho sv001 (PENDING)
        // ============================================================
        roomRepository.findAll().stream()
                .filter(r -> r.isAvailableForOccupancy())
                .findFirst()
                .ifPresent(room -> {
                    var reg = new com.dormitory.entity.Registration(
                            sv1, room, LocalDate.now().plusDays(7)
                    );
                    reg.setNote("Tôi muốn đăng ký phòng để tiện đi học.");
                    registrationRepository.save(reg);
                    log.info("  → Tạo đơn đăng ký PENDING cho sv001, phòng: {}", room.getFullRoomName());
                });

        // ============================================================
        // 4. Tạo hợp đồng mẫu cho sv002 (ACTIVE)
        // ============================================================
        roomRepository.findAll().stream()
                .filter(r -> r.isAvailableForOccupancy() && r.getRoomType().name().equals("FEMALE"))
                .findFirst()
                .ifPresent(room -> {
                    var contract = new com.dormitory.entity.Contract(
                            sv2, room,
                            LocalDate.now().minusMonths(2),
                            LocalDate.now().plusMonths(4),
                            room.getRoomFeePerMonth()
                    );
                    contractRepository.save(contract);
                    // Tăng occupied
                    room.incrementOccupied();
                    roomRepository.save(room);
                    log.info("  → Tạo hợp đồng ACTIVE cho sv002, phòng: {}", room.getFullRoomName());
                });

        // ============================================================
        // 5. Tạo vi phạm mẫu cho sv002
        // ============================================================
        var violation = new com.dormitory.entity.Violation();
        violation.setStudent(sv2);
        violation.setDescription("Gây ồn ào sau 22h, ảnh hưởng đến các phòng xung quanh");
        violation.setViolationDate(LocalDate.now().minusDays(10));
        violation.setFineAmount(new BigDecimal("200000"));
        violationRepository.save(violation);
        log.info("  → Tạo vi phạm mẫu cho sv002 (200,000đ)");

        log.info("DataInitializer: Hoàn tất khởi tạo dữ liệu mẫu.");
    }
}
