<div align="center">

# 🎹 PHOSL Backend
### Piano House of Sri Lanka — Management System (Backend)

![Java](https://img.shields.io/badge/Java-11-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-black?logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/API%20Docs-Swagger-85EA2D?logo=swagger&logoColor=black)

Backend service for managing inventory, sales, customers, recommenders, commissions, workers, repairs and analytics for **Piano House of Sri Lanka (PHOSL)**.

</div>

---

# ✨ Overview

PHOSL Backend is a secure Spring Boot REST API built to power a complete piano business management system.

It supports:

- 🔐 Role-based authentication (JWT)
- 🎹 Piano inventory management
- 👤 Customer management
- 🧾 Sales & purchase tracking
- 🤝 Recommender & commission system
- 🧑‍🔧 Worker management
- 🛠️ Repairs
- 📍 Locations
- 💳 Installments
- 📊 Analytics dashboard APIs
- 📖 Swagger API documentation

---

# 🧱 Tech Stack

- **Java 11**
- **Spring Boot 2.7.18**
- **Spring Security**
- **JWT Authentication**
- **Spring Data JPA**
- **MySQL**
- **SpringDoc OpenAPI (Swagger UI)**
- **Maven**

---

# 📁 Project Structure

The Spring Boot project is located inside:

```
PHOSL DB/backend
```

Database scripts are located in:

```
PHOSL DB/create.SQL
PHOSL DB/insert.sql
```

---

# 🚀 Getting Started

## 1️⃣ Clone the Repository

```bash
git clone https://github.com/Aeshan-Rosa/PHOSL-backend.git
cd PHOSL-backend/"PHOSL DB"/backend
```

---

## 2️⃣ Create the Database

Open MySQL Workbench and run:

- `create.SQL`
- `insert.sql`

This will create:

```
piano_management
```

---

## 3️⃣ Configure Database

Open:

```
src/main/resources/application.yml
```

Update:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/piano_management
    username: root
    password: root
```

Change credentials if needed.

⚠️ Never commit production passwords or secrets.

---

## 4️⃣ Run the Application

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or using Maven:

```bash
mvn spring-boot:run
```

Application runs at:

```
http://localhost:8080
```

---

# 🧪 API Documentation (Swagger)

After running the server, open:

```
http://localhost:8080/swagger-ui/index.html
```

Here you can:
- Test all APIs
- Authorize with JWT
- View request/response models

---

# 🔐 Security

- JWT-based authentication
- Role-based access control
- Protected endpoints
- Configurable secret key

For production:
- Use environment variables
- Use HTTPS
- Secure database credentials

---

# 📌 Future Improvements

- Unit & Integration Testing
- Docker Setup
- CI/CD Pipeline
- Production deployment guide
- Frontend integration documentation

---

# 👨‍💻 Author

**Aeshan Rosa**  
Computer Science Undergraduate  
University of Westminster (IIT Colombo)

---

<div align="center">

🎹 Built for Piano House of Sri Lanka  
Made with Spring Boot & ☕

</div>
