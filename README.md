# 🏢 Landlord Insurance Backend

This is the backend service for the **Landlord Insurance Application**, built using Spring Boot. It provides REST APIs for managing quotes, clients, brokers, and insurance coverages.  

The backend works in conjunction with the Angular frontend application.

---

## 🚀 Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA (Hibernate)
- Spring Security (JWT-based authentication)
- MySQL

---

## 📁 Project Structure
```
src/main/java/com/capstone/landlordInsurance
│
├── config                              # Security & application configuration
├── controller                          # REST controllers (API endpoints)
├── dto                                 # Data Transfer Objects
├── entity                              # JPA entity classes
├── repository                          # Database access layer (JPA repositories)
├── service                             # Business logic layer
├── utils                               # Utility/helper classes
└── LandlordInsuranceApplication.java   # Main class
```

---

## ▶️ How to Run the Project

### 1️⃣ Prerequisites

Make sure you have installed:

- Java 21
- MySQL

### 2️⃣ Clone the Repository

```bash
git clone <your-repo-url>
cd landlord-insurance-backend
```

### 3️⃣ Configure Database

Create a MySQL database:
```
CREATE DATABASE broker_portal;
```

### 4️⃣ Run the Application

Run the main class:
```
LandlordInsuranceApplication.java
```

---

## 👩‍💻 Author

Kritika Prakash
