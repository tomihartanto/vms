# VMS - Vessel Monitoring System

> **Portfolio Showcase** — This repository contains selected code samples from a Vessel Monitoring System I built as a freelance project. Some files are included as stubs (`// Full implementation available upon request`) to protect the client's intellectual property.

A web-based Vessel Monitoring System for tracking ship positions, monitoring fuel consumption, and generating reports. Built with Spring Boot, Thymeleaf, MyBatis, and MySQL.

## Highlighted Code

The following files contain full implementation and are the best examples of my work:

| File                                           | Description                                                                                                                       |
| ---------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `service/EmailProcessorService.java`           | Core email parsing engine — regex-based telemetry extraction, DMS-to-decimal coordinate conversion, Windy API weather integration |
| `service/VesselService.java`                   | Business logic — complex fuel consumption calculations, running hours, progressive reports with multiple formula modes            |
| `controllers/rest/VesselRestController.java`   | REST API — vessel CRUD, GeoJSON position data, route queries                                                                      |
| `controllers/web/MonitoringWebController.java` | Web controller — vessel monitoring page with session auth                                                                         |
| `domain/Vessel.java`                           | Domain model — vessel entity with tracking config                                                                                 |
| `domain/VesselLocation.java`                   | Domain model — location data with meteorology fields                                                                              |
| `util/LatLngConverter.java`                    | Utility — DMS ↔ decimal coordinate conversion                                                                                     |
| `util/Util.java`                               | Utility — MD5 hashing, JSON helpers, response builders                                                                            |
| `templates/monitoring.html`                    | Frontend — Leaflet.js interactive map with real-time tracking, route visualization                                                |

Other files are included as stubs showing the project structure. Full implementation is available for review upon request.

## Features

- **Email-Based Data Ingestion** — Automated IMAP email download and parsing for SAT/GSM vessel telemetry data (fuel, RPM, genset, shaft, pitch, etc.)
- **Reporting** — Vessel fuel monitoring reports and daily summary reports with PDF/XLSX export (JasperReports)
- **Email Subscription** — Automated daily report generation and delivery to subscribers via SMTP
- **Vessel Management** — CRUD operations for vessel configuration, data types, and formula settings
- **User Management** — Role-based access control (Admin, Operator, Customer, Monitor Only) with vessel allocation

## Tech Stack

| Component          | Technology                                     |
| ------------------ | ---------------------------------------------- |
| Backend            | Spring Boot 3.4.3, Java 17                     |
| Template Engine    | Thymeleaf                                      |
| ORM                | MyBatis 3.0.4                                  |
| Database           | MySQL                                          |
| Reporting          | DynamicJasper, DynamicReports, Apache POI      |
| Email              | Jakarta Mail (IMAP/SMTP)                       |
| Browser Automation | Selenium 4.29, WebDriverManager                |
| Logging            | Log4j2 with LMAX Disruptor                     |
| Frontend           | Bootstrap, Leaflet.js, Axios, Font Awesome Pro |

## Project Structure

```
src/main/java/com/asset/VMS/
├── controllers/
│   ├── web/          # Thymeleaf page controllers
│   └── rest/         # REST API controllers
├── service/          # Business logic & scheduled tasks
├── domain/           # Entity classes
├── mapper/           # MyBatis mapper interfaces
└── util/             # Utility classes
```

## Prerequisites

- **Java 17+** (JDK 17 atau lebih baru)
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**

## Getting Started

### 1. Clone Repository

```bash
git clone https://github.com/tomihartanto/vms.git
cd vms
```

### 2. Setup Database

Buat database MySQL:

```sql
CREATE DATABASE ofm;
```

> **Catatan:** Skema database tidak disertakan di repository ini. Silakan buat tabel sesuai kebutuhan atau hubungi author untuk file dump.

### 3. Konfigurasi Application

Salin file contoh konfigurasi:

```bash
cp application.properties.example src/main/resources/application.properties
```

Edit file `application.properties` sesuai environment:

```properties
# Server
server.port=8081
server.servlet.context-path=/vms

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ofm
spring.datasource.username=root
spring.datasource.password=your_password

# Email (IMAP - untuk download data kapal)
spring.mail.imap.host=your_imap_host
spring.mail.imap.username=your_email
spring.mail.imap.password=your_email_password

# Email (SMTP - untuk kirim laporan)
spring.mail.smtp.host=your_smtp_host
spring.mail.smtp.username=your_email
spring.mail.smtp.password=your_email_password
```

> **Penting:** File `application.properties` yang berisi credential asli **tidak disertakan** di repository. Gunakan `application.properties.example` sebagai template.

### 4. Build & Run

```bash
# Install dependencies & build
mvn clean install

# Jalankan aplikasi
mvn spring-boot:run
```

Atau jalankan langsung:

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

### 5. Akses Aplikasi

Buka browser dan akses:

```
http://localhost:8081/vms
```

## Build WAR untuk Deployment

```bash
mvn clean package
```

File WAR hasil build ada di `target/VMS-0.0.1-SNAPSHOT.war`. Deploy ke Tomcat atau server lainnya.

## Scheduled Tasks

| Task                | Schedule       | Description                             |
| ------------------- | -------------- | --------------------------------------- |
| Email Download      | Every 30s      | Download emails via IMAP                |
| SAT Processing      | Every 2 min    | Parse SAT vessel telemetry              |
| GSM Processing      | Every 2 min    | Parse GSM vessel telemetry              |
| Location Processing | Every 5 min    | Extract vessel GPS locations            |
| Daily Report        | Daily at 01:01 | Generate & email reports to subscribers |

## Troubleshooting

| Masalah                        | Solusi                                             |
| ------------------------------ | -------------------------------------------------- |
| Build gagal / dependency error | Jalankan `mvn clean install -U` untuk force update |
| Port 8081 sudah dipakai        | Ubah `server.port` di `application.properties`     |
| Koneksi database gagal         | Pastikan MySQL running dan credential benar        |
| Log error besar                | Cek file log di folder `logs/`                     |

## License

This project is proprietary software. See the [LICENSE](LICENSE) file for details.
