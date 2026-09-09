# 🏠 Dormitory Management System

Website quản lý ký túc xá sinh viên được xây dựng bằng Java Spring Boot, MySQL và Thymeleaf.

## 🛠 Công nghệ

- Java
- Spring Boot
- Spring Data JPA
- MySQL
- Thymeleaf
- Maven

## 🚀 Tải dự án lần đầu

```bash
git clone https://github.com/HOANG-MINH-FAN/KTX-Management.git
cd KTX-Management
```

## 🗄 Cài đặt Database

1. Mở MySQL Workbench.
2. Chạy file `dormitory_db.sql`.
3. Kiểm tra thông tin MySQL trong:

`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dormitory_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

## ▶️ Chạy chương trình

Trên Windows:

```bash
.\mvnw.cmd spring-boot:run
```

Sau đó truy cập:

```text
http://localhost:8080
```

## 🔄 Lấy và đẩy code

Trước khi làm:

```bash
git pull origin main
```

Sau khi làm xong:

```bash
git add .
git commit -m "Mô tả thay đổi"
git pull origin main
git push origin main
```

> ⚠️ Luôn `git pull` trước khi code và trước khi `git push` để hạn chế conflict.

## 📁 Cấu trúc chính

```text
src/
└── main/
    ├── java/com/dormitory/
    │   ├── controller/
    │   ├── entity/
    │   └── repository/
    └── resources/
        ├── templates/
        └── static/
```

## 👥 Quy tắc làm việc nhóm

- Không tự ý xóa hoặc sửa code của người khác.
- Mỗi thành viên phụ trách một chức năng.
- Commit ghi rõ nội dung thay đổi.
- Nếu xảy ra conflict, báo nhóm trước khi xử lý.
