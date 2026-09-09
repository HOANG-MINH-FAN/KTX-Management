# Dormitory Information System (ITEC313)

A Spring Boot web application for managing dormitory students, rooms, and room allocations.

## Tech Stack

- Java 17
- Spring Boot 4.0.1 (Spring Web MVC, Spring Data JPA, Thymeleaf)
- MySQL (via `mysql-connector-j`)
- Maven

## Prerequisites

- JDK 17+
- Maven (or use the included `mvnw` / `mvnw.cmd` wrapper)
- MySQL Server 8+ running locally
- A MySQL client (MySQL Workbench, phpMyAdmin, or the `mysql` CLI)

## Database Setup

The application does **not** auto-create its schema
(`spring.jpa.hibernate.ddl-auto=none`), so the database must be created manually
before the first run.

1. Make sure your MySQL server is running.
2. Run the included `dormitory_db.sql` script to create the database and tables:

   ```bash
   mysql -u root -p < dormitory_db.sql
   ```

   Or, in phpMyAdmin: **Import** → select `dormitory_db.sql` → **Go**.

   This creates the `dormitory_db` database along with the following tables:

   | Table | Purpose |
   |---|---|
   | `admins` | Admin login accounts |
   | `students` | Student records |
   | `rooms` | Dormitory rooms and capacity |
   | `allocations` | Links students to rooms (check-in date, status) |

   The script also inserts a default admin account and a couple of sample rows so you have something to log in with and look at right away.

3. **Default login:**
   - Username: `admin`
   - Password: `admin123`

   Change this after first login, or edit the row directly in the `admins` table.

## Application Configuration

Database connection settings are in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dormitory_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

server.port=8080
```

Update `spring.datasource.username` / `spring.datasource.password` to match your local MySQL credentials before running the app.

## Running the Application

```bash
# from the project root (where pom.xml lives)
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The app will start on **http://localhost:8080**.

## Project Structure

```
src/main/java/com/dormitory/
├── DormitoryApplication.java     # Spring Boot entry point
├── controller/                   # MVC controllers (Admin, Student, Room, Allocation, Home)
├── entity/                       # JPA entities (Admin, Student, Room, Allocation)
└── repository/                   # Spring Data JPA repositories

src/main/resources/
├── application.properties        # DB connection config
├── templates/                    # Thymeleaf HTML views
└── static/                       # CSS and images

dormitory_db.sql                  # Database schema + seed data (this file)
```

## Notes

- Passwords in the `admins` table are currently stored and compared as plain text (see `AdminController`). This is fine for a class project/demo, but should not be used as-is in production — consider hashing passwords (e.g. with BCrypt) if you extend this further.
- If you change entity field mappings in the Java code, remember to keep `dormitory_db.sql` in sync since the schema is managed manually.
