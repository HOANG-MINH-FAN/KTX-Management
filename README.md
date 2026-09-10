# 🏠 QUẢN LÝ KÝ TÚC XÁ

Website quản lý ký túc xá sinh viên.

## 1. Công nghệ sử dụng

- Java
- Spring Boot
- MySQL
- Thymeleaf
- Maven

---

## 2. Lấy code về máy lần đầu

Mở Terminal tại thư mục muốn lưu project:

```bash
git clone https://github.com/USERNAME/KTX-Management.git
```

Sau đó mở thư mục project:

```bash
cd KTX-Management
```

---

## 3. Cài Database

### Bước 1
Mở **MySQL Workbench** và đăng nhập vào MySQL.

### Bước 2
Mở file:

```text
dormitory_db.sql
```

Sau đó chạy toàn bộ file để tạo Database.

### Bước 3
Mở file:

```text
src/main/resources/application.properties
```

Kiểm tra thông tin MySQL:

```properties
spring.datasource.username=root
spring.datasource.password=
```

- `username` thường là `root`.
- `password` phải nhập **mật khẩu MySQL của máy mình**.
- Mỗi thành viên có thể có mật khẩu MySQL khác nhau.


> ⚠️ Chỉ sửa mật khẩu trên máy của mình. Không push mật khẩu cá nhân lên GitHub.

---

## 4. Chạy Website

Mở Terminal trong thư mục project và chạy:

```bash
.\mvnw.cmd spring-boot:run
```

Nếu chạy thành công, mở trình duyệt:

```text
http://localhost:8080
```

---

## 5. Khi bắt đầu code

**Luôn lấy code mới nhất trước:**

```bash
git pull origin main
```

Sau đó mới bắt đầu code.

---

## 6. Khi code xong

### Bước 1: Kiểm tra code

Chạy thử website và đảm bảo không bị lỗi.

### Bước 2: Đưa code lên GitHub

```bash
git add .
```

```bash
git commit -m "Mô tả nội dung đã thay đổi"
```

```bash
git pull origin main
```

```bash
git push origin main
```

---

## 7. Lưu ý

- Không code khi chưa `git pull`.
- Không tự ý xóa hoặc sửa code của thành viên khác.
- Code xong phải chạy thử trước khi `git push`.
- Nếu Git báo **conflict**, không tự ý xóa phần code của người khác.
- Không push mật khẩu MySQL cá nhân lên GitHub.
- Khi commit nên ghi rõ nội dung đã thay đổi.

---

## 8. Các thư mục chính

```text
src/main/java/com/dormitory/
├── controller/    → Xử lý yêu cầu từ website
├── entity/        → Các đối tượng Sinh viên, Phòng,...
└── repository/    → Làm việc với Database

src/main/resources/
├── templates/     → Các trang HTML
└── static/        → CSS, hình ảnh,...
```

## ⭐ Quy tắc đơn giản

**Pull → Code → Test → Commit → Pull → Push**
