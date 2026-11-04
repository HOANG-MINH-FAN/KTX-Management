-- =====================================================================
-- DỮ LIỆU MẪU DÙNG CHUNG CHO MODULE TRAO ĐỔI ĐỒ (PHẦN 1/2)
-- Gồm: 10 sinh viên mới (sv004–sv013) + 51 tin đăng + 24 yêu cầu giao dịch
--      + 7 đánh giá.
--
-- ĐIỀU KIỆN TRƯỚC KHI CHẠY
--   1. Đã git pull bản code mới nhất (có phần Người 3: yêu cầu giao dịch).
--   2. Đã chạy web ít nhất 1 lần để Hibernate tự tạo các cột mới của bảng
--      exchange_requests (transaction_type, owner_confirmed, ...).
--   3. Tài khoản sv001, sv002, sv003 đã có (project tự tạo khi chạy lần đầu).
--
-- MẬT KHẨU: các sinh viên mới dùng chung mật khẩu của sv001  ->  sv@123456
-- File chạy lại nhiều lần không tạo trùng dữ liệu (có kiểm tra tồn tại).
-- ẢNH: tin mẫu dùng ảnh minh họa trong thư mục
--      src/main/resources/static/images/exchange/  (giải nén từ file
--      exchange_sample_images.zip vào thư mục gốc project, rồi git add).
--      Nếu đã chạy file này từ trước (khi chưa có ảnh), chỉ cần chạy lại:
--      mục 6 cuối file sẽ điền đường dẫn ảnh cho các tin đã có.
-- Phần 2 (yêu thích + báo cáo) nằm ở file exchange_sample_data_part4.sql.
-- =====================================================================
USE dormitory_db;

-- ---------------------------------------------------------------------
-- (TÙY CHỌN) XÓA SẠCH DỮ LIỆU TRAO ĐỔI ĐỒ CŨ TRƯỚC KHI NẠP DỮ LIỆU MẪU.
-- Bỏ dấu -- ở 3 dòng dưới nếu muốn bắt đầu lại từ đầu. Dữ liệu bị xóa
-- vĩnh viễn, chỉ nên làm trên DB cá nhân của bạn.
-- ---------------------------------------------------------------------
-- DELETE FROM exchange_reviews;
-- DELETE FROM exchange_requests;
-- DELETE FROM exchange_items;

-- ---------------------------------------------------------------------
-- 1. SINH VIÊN (sao chép password_hash của sv001 nên cùng mật khẩu sv@123456)
-- ---------------------------------------------------------------------
INSERT INTO users
    (username, password_hash, role, enabled, student_code, full_name, phone, email,
     faculty, date_of_birth, gender, hometown, created_at, updated_at)
SELECT s.username, base.password_hash, 'STUDENT', 1, s.student_code, s.full_name, s.phone, s.email,
       s.faculty, s.dob, s.gender, s.hometown, NOW(), NOW()
FROM (
    SELECT 'sv004' AS username, 'SV2024004' AS student_code, 'Phạm Minh Đức' AS full_name, '0901234564' AS phone, 'minhduc@email.com' AS email, 'Điện tử viễn thông' AS faculty, '2004-03-15' AS dob, 'MALE' AS gender, 'Hải Phòng' AS hometown
    UNION ALL SELECT 'sv005', 'SV2024005', 'Hoàng Thu Hà', '0901234565', 'thuha@email.com', 'Ngoại ngữ', '2004-07-21', 'FEMALE', 'Nam Định'
    UNION ALL SELECT 'sv006', 'SV2024006', 'Vũ Hải Nam', '0901234566', 'hainam@email.com', 'Cơ khí', '2003-11-02', 'MALE', 'Thái Bình'
    UNION ALL SELECT 'sv007', 'SV2024007', 'Đỗ Khánh Linh', '0901234567', 'khanhlinh@email.com', 'Quản trị kinh doanh', '2004-01-30', 'FEMALE', 'Hà Nam'
    UNION ALL SELECT 'sv008', 'SV2024008', 'Ngô Quang Huy', '0901234568', 'quanghuy@email.com', 'Công nghệ thông tin', '2003-09-09', 'MALE', 'Bắc Ninh'
    UNION ALL SELECT 'sv009', 'SV2024009', 'Bùi Phương Thảo', '0901234569', 'phuongthao@email.com', 'Kế toán', '2004-05-18', 'FEMALE', 'Vĩnh Phúc'
    UNION ALL SELECT 'sv010', 'SV2024010', 'Đặng Tuấn Kiệt', '0901234570', 'tuankiet@email.com', 'Điện - Điện tử', '2003-12-25', 'MALE', 'Thanh Hóa'
    UNION ALL SELECT 'sv011', 'SV2024011', 'Lý Ngọc Mai', '0901234571', 'ngocmai@email.com', 'Marketing', '2004-04-04', 'FEMALE', 'Hưng Yên'
    UNION ALL SELECT 'sv012', 'SV2024012', 'Trịnh Văn Long', '0901234572', 'vanlong@email.com', 'An toàn thông tin', '2003-08-14', 'MALE', 'Nghệ An'
    UNION ALL SELECT 'sv013', 'SV2024013', 'Phan Bảo Ngọc', '0901234573', 'baongoc@email.com', 'Truyền thông đa phương tiện', '2004-02-27', 'FEMALE', 'Quảng Ninh'
) s
JOIN users base ON base.username = 'sv001'
WHERE NOT EXISTS (
    SELECT 1 FROM users x WHERE x.username = s.username OR x.student_code = s.student_code
);

-- ---------------------------------------------------------------------
-- 2. TIN ĐĂNG (51 tin: SELL / EXCHANGE / GIVEAWAY, đủ 6 danh mục)
-- ---------------------------------------------------------------------
INSERT INTO exchange_items
    (owner_id, title, description, category, exchange_type, item_condition,
     price, desired_item, image_path, status, created_at, updated_at)
SELECT u.id, t.title, t.description, t.category, t.exchange_type, t.item_condition,
       t.price, t.desired_item, t.image_path, t.status,
       NOW() - INTERVAL t.days_ago DAY, NOW() - INTERVAL t.days_ago DAY
FROM (
    SELECT 'sv001' AS username, 'Quạt mini USB' AS title, 'Còn mới 90%, có 3 mức gió, pin dùng được khoảng 6 tiếng.' AS description, 'Thiết bị điện tử' AS category, 'SELL' AS exchange_type, 'LIKE_NEW' AS item_condition, 50000 AS price, NULL AS desired_item, '/images/exchange/quat-mini-usb.jpg' AS image_path, 'AVAILABLE' AS status, 2 AS days_ago
    UNION ALL SELECT 'sv001', 'Giáo trình Cấu trúc dữ liệu và giải thuật', 'Có ghi chú bút chì, còn đủ trang, bìa hơi cũ.', 'Đồ dùng học tập - Sách', 'SELL', 'USED', 35000, NULL, '/images/exchange/giao-trinh-cau-truc-du-lieu-va-giai-thuat.jpg', 'AVAILABLE', 3
    UNION ALL SELECT 'sv001', 'Đèn bàn LED kẹp bàn', 'Đổi được 3 màu ánh sáng, kẹp chắc chắn vào mép bàn.', 'Đồ gia dụng - Phòng ở', 'GIVEAWAY', 'USED', NULL, NULL, '/images/exchange/den-ban-led-kep-ban.jpg', 'AVAILABLE', 4
    UNION ALL SELECT 'sv001', 'Bàn phím cơ Keychron K2', 'Switch brown, dùng khoảng 1 năm, có đèn nền.', 'Thiết bị điện tử', 'EXCHANGE', 'USED', NULL, 'Chuột không dây Logitech', '/images/exchange/ban-phim-co-keychron-k2.jpg', 'AVAILABLE', 5
    UNION ALL SELECT 'sv001', 'Giáo trình Lập trình hướng đối tượng Java', 'Còn mới, rất ít ghi chú.', 'Đồ dùng học tập - Sách', 'SELL', 'LIKE_NEW', 40000, NULL, '/images/exchange/giao-trinh-lap-trinh-huong-doi-tuong-java.jpg', 'SOLD', 20
    UNION ALL SELECT 'sv001', 'Bộ cờ vua gỗ', 'Đủ quân, bàn cờ gấp gọn.', 'Thể thao - Giải trí', 'GIVEAWAY', 'USED', NULL, NULL, '/images/exchange/bo-co-vua-go.jpg', 'EXCHANGED', 25
    UNION ALL SELECT 'sv002', 'Áo khoác gió size M', 'Mặc 2 lần, đã giặt sạch, chống nước nhẹ.', 'Quần áo - Phụ kiện', 'SELL', 'LIKE_NEW', 120000, NULL, '/images/exchange/ao-khoac-gio-size-m.jpg', 'AVAILABLE', 1
    UNION ALL SELECT 'sv002', 'Vợt cầu lông Yonex Astrox', 'Còn căng dây tốt, kèm bao vợt.', 'Thể thao - Giải trí', 'SELL', 'USED', 200000, NULL, '/images/exchange/vot-cau-long-yonex-astrox.jpg', 'AVAILABLE', 6
    UNION ALL SELECT 'sv002', 'Sách luyện thi TOEIC 900', 'Bộ 2 cuốn, chưa viết vào.', 'Đồ dùng học tập - Sách', 'GIVEAWAY', 'LIKE_NEW', NULL, NULL, '/images/exchange/sach-luyen-thi-toeic-900.jpg', 'AVAILABLE', 7
    UNION ALL SELECT 'sv002', 'Ổ cắm điện 4 lỗ dây dài 3m', 'Có công tắc riêng, dùng ổn định.', 'Đồ gia dụng - Phòng ở', 'EXCHANGE', 'USED', NULL, 'Quạt đứng nhỏ', '/images/exchange/o-cam-dien-4-lo-day-dai-3m.jpg', 'AVAILABLE', 8
    UNION ALL SELECT 'sv002', 'Máy tính Casio fx-580VN X', 'Dùng 1 học kỳ, còn hộp và nắp bảo vệ.', 'Đồ dùng học tập - Sách', 'SELL', 'LIKE_NEW', 250000, NULL, '/images/exchange/may-tinh-casio-fx-580vn-x.jpg', 'PENDING', 9
    UNION ALL SELECT 'sv003', 'Tai nghe có dây Sony', 'Âm thanh tốt, còn nguyên micro.', 'Thiết bị điện tử', 'SELL', 'USED', 60000, NULL, '/images/exchange/tai-nghe-co-day-sony.jpg', 'AVAILABLE', 2
    UNION ALL SELECT 'sv003', 'Balo laptop 15.6 inch chống nước', 'Nhiều ngăn, quai êm, dùng được 1 năm.', 'Quần áo - Phụ kiện', 'SELL', 'LIKE_NEW', 150000, NULL, '/images/exchange/balo-laptop-15-6-inch-chong-nuoc.jpg', 'AVAILABLE', 3
    UNION ALL SELECT 'sv003', 'Bóng đá size 5', 'Còn căng, dùng ít.', 'Thể thao - Giải trí', 'EXCHANGE', 'USED', NULL, 'Bóng rổ size 7', '/images/exchange/bong-da-size-5.jpg', 'AVAILABLE', 11
    UNION ALL SELECT 'sv003', 'Nồi cơm điện mini 1 lít', 'Nấu được cho 1–2 người, hoạt động tốt.', 'Đồ gia dụng - Phòng ở', 'SELL', 'USED', 180000, NULL, '/images/exchange/noi-com-dien-mini-1-lit.jpg', 'AVAILABLE', 12
    UNION ALL SELECT 'sv003', 'Loa Bluetooth JBL Go', 'Pin tốt, âm bass ổn, kèm cáp sạc.', 'Thiết bị điện tử', 'SELL', 'LIKE_NEW', 300000, NULL, '/images/exchange/loa-bluetooth-jbl-go.jpg', 'SOLD', 30
    UNION ALL SELECT 'sv003', 'Thảm tập yoga 6mm', 'Chống trượt, đã vệ sinh sạch.', 'Thể thao - Giải trí', 'GIVEAWAY', 'USED', NULL, NULL, '/images/exchange/tham-tap-yoga-6mm.jpg', 'AVAILABLE', 13
    UNION ALL SELECT 'sv004', 'Bộ kit Arduino Uno R3', 'Đủ cảm biến cơ bản, dây cắm và breadboard.', 'Thiết bị điện tử', 'SELL', 'LIKE_NEW', 220000, NULL, '/images/exchange/bo-kit-arduino-uno-r3.jpg', 'AVAILABLE', 4
    UNION ALL SELECT 'sv004', 'Chuột không dây Logitech M331', 'Click êm, dùng pin AA, còn đầu thu USB.', 'Thiết bị điện tử', 'EXCHANGE', 'LIKE_NEW', NULL, 'Bàn di chuột cỡ lớn', '/images/exchange/chuot-khong-day-logitech-m331.jpg', 'AVAILABLE', 5
    UNION ALL SELECT 'sv004', 'Sách Mạch điện tử cơ bản', 'Có đánh dấu một số chương quan trọng.', 'Đồ dùng học tập - Sách', 'GIVEAWAY', 'USED', NULL, NULL, '/images/exchange/sach-mach-dien-tu-co-ban.jpg', 'AVAILABLE', 14
    UNION ALL SELECT 'sv004', 'Giày chạy bộ size 42', 'Đế còn tốt, đã vệ sinh.', 'Thể thao - Giải trí', 'SELL', 'USED', 280000, NULL, '/images/exchange/giay-chay-bo-size-42.jpg', 'CLOSED', 16
    UNION ALL SELECT 'sv005', 'Từ điển Anh - Việt Oxford', 'Bản bìa cứng, còn đẹp.', 'Đồ dùng học tập - Sách', 'SELL', 'USED', 90000, NULL, '/images/exchange/tu-dien-anh-viet-oxford.jpg', 'AVAILABLE', 6
    UNION ALL SELECT 'sv005', 'Gương trang điểm để bàn', 'Gương tròn có chân đế, không trầy xước.', 'Đồ gia dụng - Phòng ở', 'GIVEAWAY', 'LIKE_NEW', NULL, NULL, '/images/exchange/guong-trang-diem-de-ban.jpg', 'AVAILABLE', 7
    UNION ALL SELECT 'sv005', 'Túi tote vải canvas', 'Mới 100%, chưa dùng, in hình đơn giản.', 'Quần áo - Phụ kiện', 'EXCHANGE', 'NEW', NULL, 'Ví nữ nhỏ', '/images/exchange/tui-tote-vai-canvas.jpg', 'AVAILABLE', 9
    UNION ALL SELECT 'sv005', 'Đàn ukulele soprano', 'Kèm bao đàn và bộ dây dự phòng.', 'Thể thao - Giải trí', 'SELL', 'USED', 350000, NULL, '/images/exchange/dan-ukulele-soprano.jpg', 'AVAILABLE', 18
    UNION ALL SELECT 'sv006', 'Bộ dụng cụ sửa chữa mini', 'Tua vít, kìm nhỏ, kèm hộp đựng.', 'Khác', 'SELL', 'LIKE_NEW', 130000, NULL, '/images/exchange/bo-dung-cu-sua-chua-mini.jpg', 'AVAILABLE', 8
    UNION ALL SELECT 'sv006', 'Quạt đứng Senko', 'Chạy êm, 3 tốc độ, chiều cao điều chỉnh được.', 'Đồ gia dụng - Phòng ở', 'EXCHANGE', 'USED', NULL, 'Ổ cắm điện dây dài', '/images/exchange/quat-dung-senko.jpg', 'AVAILABLE', 12
    UNION ALL SELECT 'sv006', 'Mũ bảo hiểm 3/4', 'Đạt chuẩn, quai còn chắc, ít sử dụng.', 'Khác', 'SELL', 'LIKE_NEW', 110000, NULL, '/images/exchange/mu-bao-hiem-3-4.jpg', 'AVAILABLE', 5
    UNION ALL SELECT 'sv006', 'Ván trượt skateboard', 'Bánh còn tốt, kèm bộ dụng cụ siết.', 'Thể thao - Giải trí', 'EXCHANGE', 'USED', NULL, 'Giày thể thao size 41', '/images/exchange/van-truot-skateboard.jpg', 'PENDING', 11
    UNION ALL SELECT 'sv007', 'Giáo trình Marketing căn bản', 'Có ghi chú, còn đủ trang.', 'Đồ dùng học tập - Sách', 'SELL', 'USED', 30000, NULL, '/images/exchange/giao-trinh-marketing-can-ban.jpg', 'AVAILABLE', 7
    UNION ALL SELECT 'sv007', 'Bình giữ nhiệt 500ml', 'Giữ nóng khoảng 8 tiếng, không móp.', 'Đồ gia dụng - Phòng ở', 'SELL', 'LIKE_NEW', 70000, NULL, '/images/exchange/binh-giu-nhiet-500ml.jpg', 'AVAILABLE', 3
    UNION ALL SELECT 'sv007', 'Set kẹp tóc và phụ kiện', '10 món mới, nhiều màu sắc.', 'Quần áo - Phụ kiện', 'GIVEAWAY', 'NEW', NULL, NULL, '/images/exchange/set-kep-toc-va-phu-kien.jpg', 'AVAILABLE', 4
    UNION ALL SELECT 'sv007', 'Kính chống ánh sáng xanh', 'Gọng nhẹ, tròng sạch, kèm hộp.', 'Quần áo - Phụ kiện', 'SELL', 'LIKE_NEW', 95000, NULL, '/images/exchange/kinh-chong-anh-sang-xanh.jpg', 'SOLD', 22
    UNION ALL SELECT 'sv008', 'Màn hình Dell 22 inch', 'Full HD, không điểm chết, kèm dây HDMI.', 'Thiết bị điện tử', 'SELL', 'USED', 1200000, NULL, '/images/exchange/man-hinh-dell-22-inch.jpg', 'AVAILABLE', 6
    UNION ALL SELECT 'sv008', 'Giá đỡ laptop nhôm', 'Gấp gọn, nâng được 3 độ cao.', 'Thiết bị điện tử', 'SELL', 'LIKE_NEW', 120000, NULL, '/images/exchange/gia-do-laptop-nhom.jpg', 'AVAILABLE', 10
    UNION ALL SELECT 'sv008', 'Sách Clean Code bản tiếng Anh', 'Còn rất mới, không viết vẽ.', 'Đồ dùng học tập - Sách', 'EXCHANGE', 'LIKE_NEW', NULL, 'Sách Design Patterns', '/images/exchange/sach-clean-code-ban-tieng-anh.jpg', 'AVAILABLE', 13
    UNION ALL SELECT 'sv009', 'Giáo trình Nguyên lý kế toán', 'Bản mới nhất, ít ghi chú.', 'Đồ dùng học tập - Sách', 'SELL', 'USED', 28000, NULL, '/images/exchange/giao-trinh-nguyen-ly-ke-toan.jpg', 'AVAILABLE', 9
    UNION ALL SELECT 'sv009', 'Máy tính bỏ túi Casio fx-570ES', 'Hoạt động tốt, phím nhạy.', 'Đồ dùng học tập - Sách', 'SELL', 'USED', 120000, NULL, '/images/exchange/may-tinh-bo-tui-casio-fx-570es.jpg', 'AVAILABLE', 15
    UNION ALL SELECT 'sv009', 'Áo sơ mi trắng size S', 'Chất vải mát, đã giặt ủi.', 'Quần áo - Phụ kiện', 'GIVEAWAY', 'LIKE_NEW', NULL, NULL, '/images/exchange/ao-so-mi-trang-size-s.jpg', 'AVAILABLE', 5
    UNION ALL SELECT 'sv010', 'Đồng hồ vạn năng', 'Đo được điện áp, dòng điện, điện trở.', 'Thiết bị điện tử', 'SELL', 'USED', 140000, NULL, '/images/exchange/dong-ho-van-nang.jpg', 'AVAILABLE', 8
    UNION ALL SELECT 'sv010', 'Mỏ hàn thiếc 60W', 'Kèm đế để mỏ hàn và cuộn thiếc nhỏ.', 'Thiết bị điện tử', 'SELL', 'LIKE_NEW', 85000, NULL, '/images/exchange/mo-han-thiec-60w.jpg', 'AVAILABLE', 16
    UNION ALL SELECT 'sv010', 'Đồng hồ treo tường', 'Mặt tròn đơn giản, chạy êm.', 'Đồ gia dụng - Phòng ở', 'SELL', 'NEW', 80000, NULL, '/images/exchange/dong-ho-treo-tuong.jpg', 'AVAILABLE', 15
    UNION ALL SELECT 'sv011', 'Máy sấy tóc mini', 'Gọn nhẹ, 2 mức nhiệt, phù hợp ký túc xá.', 'Đồ gia dụng - Phòng ở', 'EXCHANGE', 'LIKE_NEW', NULL, 'Máy cuốn tóc mini', '/images/exchange/may-say-toc-mini.jpg', 'AVAILABLE', 9
    UNION ALL SELECT 'sv011', 'Sổ tay planner 2026', 'Mới 100%, chưa ghi chép.', 'Khác', 'SELL', 'NEW', 55000, NULL, '/images/exchange/so-tay-planner-2026.jpg', 'AVAILABLE', 2
    UNION ALL SELECT 'sv011', 'Bộ chăm sóc da mini', 'Hàng chưa mở, hạn dùng còn dài.', 'Khác', 'GIVEAWAY', 'NEW', NULL, NULL, '/images/exchange/bo-cham-soc-da-mini.jpg', 'AVAILABLE', 3
    UNION ALL SELECT 'sv012', 'Ổ cứng SSD 256GB', 'Đã kiểm tra sức khỏe ổ, hoạt động tốt.', 'Thiết bị điện tử', 'SELL', 'USED', 350000, NULL, '/images/exchange/o-cung-ssd-256gb.jpg', 'AVAILABLE', 7
    UNION ALL SELECT 'sv012', 'Sách An toàn mạng', 'Có đánh dấu và ghi chú nhẹ.', 'Đồ dùng học tập - Sách', 'GIVEAWAY', 'USED', NULL, NULL, '/images/exchange/sach-an-toan-mang.jpg', 'AVAILABLE', 12
    UNION ALL SELECT 'sv012', 'Webcam Full HD', 'Có micro tích hợp, cắm USB là dùng.', 'Thiết bị điện tử', 'EXCHANGE', 'LIKE_NEW', NULL, 'Micro thu âm USB', '/images/exchange/webcam-full-hd.jpg', 'AVAILABLE', 4
    UNION ALL SELECT 'sv013', 'Máy ảnh film Pentax', 'Còn hoạt động, kèm bao da.', 'Thể thao - Giải trí', 'SELL', 'USED', 900000, NULL, '/images/exchange/may-anh-film-pentax.jpg', 'AVAILABLE', 20
    UNION ALL SELECT 'sv013', 'Tripod điện thoại', 'Chân gập gọn, có remote Bluetooth.', 'Thiết bị điện tử', 'SELL', 'LIKE_NEW', 75000, NULL, '/images/exchange/tripod-dien-thoai.jpg', 'AVAILABLE', 10
    UNION ALL SELECT 'sv013', 'Bộ board game Uno', 'Đủ bài, hộp hơi cũ.', 'Thể thao - Giải trí', 'GIVEAWAY', 'USED', NULL, NULL, '/images/exchange/bo-board-game-uno.jpg', 'AVAILABLE', 6
) t
JOIN users u ON u.username = t.username
WHERE NOT EXISTS (
    SELECT 1 FROM exchange_items i WHERE i.title = t.title AND i.owner_id = u.id
);

-- ---------------------------------------------------------------------
-- 3. YÊU CẦU GIAO DỊCH (đủ trạng thái PENDING / ACCEPTED / REJECTED /
--    CANCELLED / COMPLETED; trạng thái tin khớp với trạng thái giao dịch)
-- ---------------------------------------------------------------------
INSERT INTO exchange_requests
    (item_id, requester_id, message, transaction_type, offered_item, reject_reason, status,
     owner_confirmed, requester_confirmed, created_at, updated_at, processed_at, completed_at)
SELECT i.id, r.id, t.message, i.exchange_type, t.offered_item, t.reject_reason, t.status,
       t.owner_confirmed, t.requester_confirmed,
       NOW() - INTERVAL t.created_ago DAY,
       NOW() - INTERVAL t.updated_ago DAY,
       CASE WHEN t.processed_ago IS NULL THEN NULL ELSE NOW() - INTERVAL t.processed_ago DAY END,
       CASE WHEN t.completed_ago IS NULL THEN NULL ELSE NOW() - INTERVAL t.completed_ago DAY END
FROM (
    SELECT 'Giáo trình Lập trình hướng đối tượng Java' AS item_title, 'sv002' AS requester, 'Mình muốn mua cuốn này để học kỳ sau, bạn còn không?' AS message, NULL AS offered_item, NULL AS reject_reason, 'COMPLETED' AS status, 1 AS owner_confirmed, 1 AS requester_confirmed, 19 AS created_ago, 17 AS updated_ago, 18 AS processed_ago, 17 AS completed_ago
    UNION ALL SELECT 'Bộ cờ vua gỗ', 'sv005', 'Mình muốn nhận bộ cờ này để chơi cùng bạn cùng phòng.', NULL, NULL, 'COMPLETED', 1, 1, 24, 22, 23, 22
    UNION ALL SELECT 'Loa Bluetooth JBL Go', 'sv006', 'Mình muốn mua loa này, có thể gặp ở sảnh tòa nhà không?', NULL, NULL, 'COMPLETED', 1, 1, 29, 27, 28, 27
    UNION ALL SELECT 'Kính chống ánh sáng xanh', 'sv003', 'Mình muốn mua, bạn có thể bớt chút được không?', NULL, NULL, 'COMPLETED', 1, 1, 21, 19, 20, 19
    UNION ALL SELECT 'Máy tính Casio fx-580VN X', 'sv004', 'Mình cần máy này cho môn xác suất thống kê.', NULL, NULL, 'ACCEPTED', 1, 0, 8, 7, 7, NULL
    UNION ALL SELECT 'Máy tính Casio fx-580VN X', 'sv009', 'Mình cũng cần máy này, bạn xem giúp mình nhé.', NULL, 'Tin đăng đã được chủ tin chấp nhận giao dịch với một sinh viên khác.', 'REJECTED', 0, 0, 8, 7, 7, NULL
    UNION ALL SELECT 'Ván trượt skateboard', 'sv001', 'Mình có đôi giày thể thao size 41 còn mới, đổi được không?', 'Giày thể thao size 41', NULL, 'ACCEPTED', 0, 0, 10, 9, 9, NULL
    UNION ALL SELECT 'Ván trượt skateboard', 'sv007', 'Mình muốn đổi bằng đôi giày Converse.', 'Giày Converse size 41', 'Tin đăng đã được chủ tin chấp nhận giao dịch với một sinh viên khác.', 'REJECTED', 0, 0, 10, 9, 9, NULL
    UNION ALL SELECT 'Quạt mini USB', 'sv003', 'Mình muốn mua quạt này, bạn còn không?', NULL, NULL, 'PENDING', 0, 0, 1, 1, NULL, NULL
    UNION ALL SELECT 'Quạt mini USB', 'sv005', 'Cho mình xin quạt này nhé, mình lấy trong tuần này.', NULL, NULL, 'PENDING', 0, 0, 1, 1, NULL, NULL
    UNION ALL SELECT 'Quạt mini USB', 'sv008', 'Mình muốn mua, có thể giao ở tòa A không?', NULL, NULL, 'PENDING', 0, 0, 1, 1, NULL, NULL
    UNION ALL SELECT 'Áo khoác gió size M', 'sv007', 'Áo còn không bạn? Mình muốn thử trước khi mua.', NULL, NULL, 'PENDING', 0, 0, 0, 0, NULL, NULL
    UNION ALL SELECT 'Đèn bàn LED kẹp bàn', 'sv004', 'Mình đang cần đèn bàn để học bài buổi tối.', NULL, NULL, 'PENDING', 0, 0, 3, 3, NULL, NULL
    UNION ALL SELECT 'Bàn phím cơ Keychron K2', 'sv004', 'Mình đổi bằng chuột Logitech M331 còn mới nhé.', 'Chuột không dây Logitech M331', NULL, 'PENDING', 0, 0, 4, 4, NULL, NULL
    UNION ALL SELECT 'Bộ kit Arduino Uno R3', 'sv006', 'Mình muốn mua bộ kit này cho đồ án môn vi điều khiển.', NULL, NULL, 'PENDING', 0, 0, 3, 3, NULL, NULL
    UNION ALL SELECT 'Đàn ukulele soprano', 'sv002', 'Mình muốn mua đàn để tập chơi, bạn còn không?', NULL, NULL, 'PENDING', 0, 0, 17, 17, NULL, NULL
    UNION ALL SELECT 'Sách luyện thi TOEIC 900', 'sv008', 'Mình xin nhận bộ sách này để ôn thi chứng chỉ.', NULL, NULL, 'PENDING', 0, 0, 6, 6, NULL, NULL
    UNION ALL SELECT 'Màn hình Dell 22 inch', 'sv012', 'Mình muốn mua màn hình, có thể xem trực tiếp được không?', NULL, NULL, 'PENDING', 0, 0, 5, 5, NULL, NULL
    UNION ALL SELECT 'Máy sấy tóc mini', 'sv013', 'Mình đổi bằng máy cuốn tóc mini còn mới nhé.', 'Máy cuốn tóc mini', NULL, 'PENDING', 0, 0, 8, 8, NULL, NULL
    UNION ALL SELECT 'Vợt cầu lông Yonex Astrox', 'sv003', 'Mình muốn mua vợt này, giá có thương lượng được không?', NULL, 'Mình muốn giữ lại vợt này, xin lỗi bạn nhé.', 'REJECTED', 0, 0, 5, 4, 4, NULL
    UNION ALL SELECT 'Nồi cơm điện mini 1 lít', 'sv007', 'Mình muốn mua nồi cơm này cho phòng.', NULL, 'Đã có người đặt trước.', 'REJECTED', 0, 0, 11, 10, 10, NULL
    UNION ALL SELECT 'Tai nghe có dây Sony', 'sv004', 'Mình muốn mua tai nghe này.', NULL, NULL, 'CANCELLED', 0, 0, 1, 0, 0, NULL
    UNION ALL SELECT 'Balo laptop 15.6 inch chống nước', 'sv001', 'Mình muốn mua balo, nhưng đã tìm được cái khác.', NULL, NULL, 'CANCELLED', 0, 0, 2, 1, 1, NULL
    UNION ALL SELECT 'Sổ tay planner 2026', 'sv010', 'Mình muốn mua sổ này.', NULL, NULL, 'CANCELLED', 0, 0, 1, 0, 0, NULL
) t
JOIN exchange_items i ON i.title = t.item_title
JOIN users r ON r.username = t.requester
WHERE NOT EXISTS (
    SELECT 1 FROM exchange_requests x WHERE x.item_id = i.id AND x.requester_id = r.id
);

-- ---------------------------------------------------------------------
-- 4. ĐÁNH GIÁ (chỉ cho các giao dịch COMPLETED)
-- ---------------------------------------------------------------------
INSERT INTO exchange_reviews (request_id, reviewer_id, reviewee_id, rating, comment, created_at)
SELECT q.id, rv.id, re.id, t.rating, t.comment, NOW() - INTERVAL t.days_ago DAY
FROM (
    SELECT 'Giáo trình Lập trình hướng đối tượng Java' AS item_title, 'sv002' AS requester, 'sv002' AS reviewer, 'sv001' AS reviewee, 5 AS rating, 'Người bán nhiệt tình, sách đúng như mô tả.' AS comment, 16 AS days_ago
    UNION ALL SELECT 'Giáo trình Lập trình hướng đối tượng Java', 'sv002', 'sv001', 'sv002', 5, 'Người mua lịch sự, thanh toán nhanh gọn.', 16
    UNION ALL SELECT 'Bộ cờ vua gỗ', 'sv005', 'sv005', 'sv001', 4, 'Cảm ơn bạn đã tặng, bộ cờ còn rất tốt.', 21
    UNION ALL SELECT 'Loa Bluetooth JBL Go', 'sv006', 'sv006', 'sv003', 5, 'Loa đúng mô tả, giao dịch nhanh.', 26
    UNION ALL SELECT 'Loa Bluetooth JBL Go', 'sv006', 'sv003', 'sv006', 4, 'Hẹn gặp đúng giờ, thân thiện.', 26
    UNION ALL SELECT 'Kính chống ánh sáng xanh', 'sv003', 'sv003', 'sv007', 3, 'Kính ổn nhưng giao hơi trễ so với hẹn.', 18
    UNION ALL SELECT 'Kính chống ánh sáng xanh', 'sv003', 'sv007', 'sv003', 5, 'Bạn mua dễ chịu, thanh toán ngay.', 18
) t
JOIN exchange_items i ON i.title = t.item_title
JOIN users req ON req.username = t.requester
JOIN exchange_requests q ON q.item_id = i.id AND q.requester_id = req.id
JOIN users rv ON rv.username = t.reviewer
JOIN users re ON re.username = t.reviewee
WHERE NOT EXISTS (
    SELECT 1 FROM exchange_reviews x WHERE x.request_id = q.id AND x.reviewer_id = rv.id
);

-- ---------------------------------------------------------------------
-- 5. KIỂM TRA KẾT QUẢ
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS so_sinh_vien FROM users WHERE role = 'STUDENT';
SELECT status, COUNT(*) AS so_tin FROM exchange_items GROUP BY status;
SELECT status, COUNT(*) AS so_giao_dich FROM exchange_requests GROUP BY status;
SELECT COUNT(*) AS so_danh_gia FROM exchange_reviews;


-- ---------------------------------------------------------------------
-- 6. ĐIỀN ẢNH CHO CÁC TIN MẪU ĐÃ CÓ (chỉ tin chưa có ảnh; không ghi đè
--    ảnh do người dùng tự tải lên)
-- ---------------------------------------------------------------------

UPDATE exchange_items i
JOIN (
    SELECT 'Quạt mini USB' AS title, '/images/exchange/quat-mini-usb.jpg' AS image_path
    UNION ALL SELECT 'Giáo trình Cấu trúc dữ liệu và giải thuật', '/images/exchange/giao-trinh-cau-truc-du-lieu-va-giai-thuat.jpg'
    UNION ALL SELECT 'Đèn bàn LED kẹp bàn', '/images/exchange/den-ban-led-kep-ban.jpg'
    UNION ALL SELECT 'Bàn phím cơ Keychron K2', '/images/exchange/ban-phim-co-keychron-k2.jpg'
    UNION ALL SELECT 'Giáo trình Lập trình hướng đối tượng Java', '/images/exchange/giao-trinh-lap-trinh-huong-doi-tuong-java.jpg'
    UNION ALL SELECT 'Bộ cờ vua gỗ', '/images/exchange/bo-co-vua-go.jpg'
    UNION ALL SELECT 'Áo khoác gió size M', '/images/exchange/ao-khoac-gio-size-m.jpg'
    UNION ALL SELECT 'Vợt cầu lông Yonex Astrox', '/images/exchange/vot-cau-long-yonex-astrox.jpg'
    UNION ALL SELECT 'Sách luyện thi TOEIC 900', '/images/exchange/sach-luyen-thi-toeic-900.jpg'
    UNION ALL SELECT 'Ổ cắm điện 4 lỗ dây dài 3m', '/images/exchange/o-cam-dien-4-lo-day-dai-3m.jpg'
    UNION ALL SELECT 'Máy tính Casio fx-580VN X', '/images/exchange/may-tinh-casio-fx-580vn-x.jpg'
    UNION ALL SELECT 'Tai nghe có dây Sony', '/images/exchange/tai-nghe-co-day-sony.jpg'
    UNION ALL SELECT 'Balo laptop 15.6 inch chống nước', '/images/exchange/balo-laptop-15-6-inch-chong-nuoc.jpg'
    UNION ALL SELECT 'Bóng đá size 5', '/images/exchange/bong-da-size-5.jpg'
    UNION ALL SELECT 'Nồi cơm điện mini 1 lít', '/images/exchange/noi-com-dien-mini-1-lit.jpg'
    UNION ALL SELECT 'Loa Bluetooth JBL Go', '/images/exchange/loa-bluetooth-jbl-go.jpg'
    UNION ALL SELECT 'Thảm tập yoga 6mm', '/images/exchange/tham-tap-yoga-6mm.jpg'
    UNION ALL SELECT 'Bộ kit Arduino Uno R3', '/images/exchange/bo-kit-arduino-uno-r3.jpg'
    UNION ALL SELECT 'Chuột không dây Logitech M331', '/images/exchange/chuot-khong-day-logitech-m331.jpg'
    UNION ALL SELECT 'Sách Mạch điện tử cơ bản', '/images/exchange/sach-mach-dien-tu-co-ban.jpg'
    UNION ALL SELECT 'Giày chạy bộ size 42', '/images/exchange/giay-chay-bo-size-42.jpg'
    UNION ALL SELECT 'Từ điển Anh - Việt Oxford', '/images/exchange/tu-dien-anh-viet-oxford.jpg'
    UNION ALL SELECT 'Gương trang điểm để bàn', '/images/exchange/guong-trang-diem-de-ban.jpg'
    UNION ALL SELECT 'Túi tote vải canvas', '/images/exchange/tui-tote-vai-canvas.jpg'
    UNION ALL SELECT 'Đàn ukulele soprano', '/images/exchange/dan-ukulele-soprano.jpg'
    UNION ALL SELECT 'Bộ dụng cụ sửa chữa mini', '/images/exchange/bo-dung-cu-sua-chua-mini.jpg'
    UNION ALL SELECT 'Quạt đứng Senko', '/images/exchange/quat-dung-senko.jpg'
    UNION ALL SELECT 'Mũ bảo hiểm 3/4', '/images/exchange/mu-bao-hiem-3-4.jpg'
    UNION ALL SELECT 'Ván trượt skateboard', '/images/exchange/van-truot-skateboard.jpg'
    UNION ALL SELECT 'Giáo trình Marketing căn bản', '/images/exchange/giao-trinh-marketing-can-ban.jpg'
    UNION ALL SELECT 'Bình giữ nhiệt 500ml', '/images/exchange/binh-giu-nhiet-500ml.jpg'
    UNION ALL SELECT 'Set kẹp tóc và phụ kiện', '/images/exchange/set-kep-toc-va-phu-kien.jpg'
    UNION ALL SELECT 'Kính chống ánh sáng xanh', '/images/exchange/kinh-chong-anh-sang-xanh.jpg'
    UNION ALL SELECT 'Màn hình Dell 22 inch', '/images/exchange/man-hinh-dell-22-inch.jpg'
    UNION ALL SELECT 'Giá đỡ laptop nhôm', '/images/exchange/gia-do-laptop-nhom.jpg'
    UNION ALL SELECT 'Sách Clean Code bản tiếng Anh', '/images/exchange/sach-clean-code-ban-tieng-anh.jpg'
    UNION ALL SELECT 'Giáo trình Nguyên lý kế toán', '/images/exchange/giao-trinh-nguyen-ly-ke-toan.jpg'
    UNION ALL SELECT 'Máy tính bỏ túi Casio fx-570ES', '/images/exchange/may-tinh-bo-tui-casio-fx-570es.jpg'
    UNION ALL SELECT 'Áo sơ mi trắng size S', '/images/exchange/ao-so-mi-trang-size-s.jpg'
    UNION ALL SELECT 'Đồng hồ vạn năng', '/images/exchange/dong-ho-van-nang.jpg'
    UNION ALL SELECT 'Mỏ hàn thiếc 60W', '/images/exchange/mo-han-thiec-60w.jpg'
    UNION ALL SELECT 'Đồng hồ treo tường', '/images/exchange/dong-ho-treo-tuong.jpg'
    UNION ALL SELECT 'Máy sấy tóc mini', '/images/exchange/may-say-toc-mini.jpg'
    UNION ALL SELECT 'Sổ tay planner 2026', '/images/exchange/so-tay-planner-2026.jpg'
    UNION ALL SELECT 'Bộ chăm sóc da mini', '/images/exchange/bo-cham-soc-da-mini.jpg'
    UNION ALL SELECT 'Ổ cứng SSD 256GB', '/images/exchange/o-cung-ssd-256gb.jpg'
    UNION ALL SELECT 'Sách An toàn mạng', '/images/exchange/sach-an-toan-mang.jpg'
    UNION ALL SELECT 'Webcam Full HD', '/images/exchange/webcam-full-hd.jpg'
    UNION ALL SELECT 'Máy ảnh film Pentax', '/images/exchange/may-anh-film-pentax.jpg'
    UNION ALL SELECT 'Tripod điện thoại', '/images/exchange/tripod-dien-thoai.jpg'
    UNION ALL SELECT 'Bộ board game Uno', '/images/exchange/bo-board-game-uno.jpg'
) t ON t.title = i.title
SET i.image_path = t.image_path
WHERE i.image_path IS NULL OR i.image_path = '';


