# flight-crew-management-system

## ✈️ About the Project
The **Flight Crew Management** system is an advanced web platform designed to optimize and manage airline crew schedules. The primary goal of the application is to ensure the safety of flight operations through strict compliance with **FTL (Flight Time Limitations)** and the monitoring of crew rest periods.

The system automates the process of assigning pilots and cabin crew to specific duties, taking into account dynamically changing hourly limits within specific time windows.

## 🚀 Key Features

### 1. Flight Time Limitations (FTL) Management
* **Rolling Limits:** The system does not store fixed, static hours. Flight time is calculated dynamically (on-the-fly) for 20-day and 365-day rolling windows backward from the start date of any given duty.
* **Safety Locks:** The system prevents the assignment of a crew member if the new duty would cause them to exceed 90 hours of flight time in 20 days or 900 hours in a calendar year.
* **Early Warning System:** If a crew member is within 5 hours of reaching their FTL limit, the Scheduler receives a warning during assignment, and the crew member sees a prominent alert on their profile.

### 2. Rest Period and Collision Logic
* **12-Hour Rest Rule:** The system automatically checks the time gap between the end of the previous duty and the start of a new one. If the gap is less than 12 hours, the assignment is blocked.
* **Overlap Detection:** It is impossible to assign a crew member to two duties that overlap in time.

### 3. Acknowledge System Workflow
* **PENDING Status:** Once assigned by the Scheduler, the duty appears in the crew member's schedule as "Pending".
* **Acceptance/Rejection:** The crew member must consciously accept or reject the duty.
* **Incapacity:** If a duty is rejected, the crew member must provide a reason (e.g., sickness/incapacity). The system increments an `Incapacity Counter` visible in the operational statistics.

### 4. User Roles
* **Admin:** Full management of users and system settings.
* **Scheduler:** Creates duties from available flights, filters the flight network, assigns crew members, and monitors duty statuses (Pending/Accepted/Rejected).
* **Crewmember:** Views their own schedule, monitors personal FTL limits, and accepts/rejects assigned duties.

## 🛠️ Tech Stack

### Backend:
* **Java 21** (Amazon Corretto / Eclipse Temurin)
* **Spring Boot 3.x** (Data JPA, Security, Web)
* **JWT (JSON Web Token):** Secure, stateless authentication.
* **PostgreSQL / H2:** Relational database management.
* **Lombok:** Boilerplate code reduction.

### Frontend:
* **React.js**
* **React Router:** Internal application navigation.
* **Inter UI / Font Awesome:** Typography and iconography.
* **CSS-in-JS / Flexbox:** Responsive and modern user interface.

## 📂 Project Structure Highlights

### Core Business Logic (Backend)
* `assignUserToDuty`: The main validation engine (FTL calculations, rest periods, overlaps).
* `getMyStats`: Dynamic rolling statistics processor.
* `reportIncapacity`: "Soft Delete" mechanism maintaining an audit trail for rejected duties.

### Filtering and Bulk Import
* Mass flight insertion via the `/flights/bulk` endpoint (Postman friendly).
* Advanced flight filtering on the frontend (by flight number, departure airport, and date sorting).

## ⚙️ Getting Started

### Prerequisites:
* Docker & Docker Compose (Recommended)
* JDK 21 & Maven (For local build)

### Method 1: Docker Compose (Fastest)
1. Clone the repository.
2. In the root directory, run:
   ```bash
   docker-compose up --build
