# Finance Manager

A web-based financial management system for small businesses built with Spring Boot and PostgreSQL.

## Live Demo

**Deployed Application:** [https://finance-manager.up.railway.app](https://finance-manager.up.railway.app/)

## Overview

Finance Manager helps small businesses track income, expenses, and recurring bills. Features include transaction management, an interactive calendar for bill payments, and real time financial dashboards.

## Features

* Track income and expenses with categories
* Manage recurring monthly bills
* Interactive calendar view for payments
* Mark bills as paid from calendar
* Filter transactions by type and category
* Secure user authentication
* Responsive design for all devices

## Technology Stack

**Backend**
* Spring Boot 3.5.6
* Spring Security 6
* Spring Data JPA
* PostgreSQL (production)
* H2 Database (development)
* Java 17

**Frontend**
* Thymeleaf
* Bootstrap 5
* JavaScript

**Deployment**
* Railway
* Maven

## Getting Started

Clone and run:
```bash
git clone https://github.com/ardaaboz/finance-manager.git
cd finance-manager
./mvnw spring-boot:run
```

Access at http://localhost:8080

**Test Credentials:**
* Username: john | Password: password123
* Username: mary | Password: password456

## Database Schema

**Users:** id, username, email, password

**Transactions:** id, user_id, description, amount, type, category, is_recurring, day_of_month, next_due_date, due_date, is_paid, created_date

## Project Structure
```
finance-manager/
├── src/main/java/com/example/financemanager/
│   ├── controllers/
│   ├── services/
│   ├── repositories/
│   └── entities/
├── src/main/resources/
│   ├── templates/
│   └── application.properties
└── pom.xml
```

## Security

* BCrypt password encryption
* CSRF protection
* Session management
* User data isolation
