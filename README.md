# Finance Manager
### IT Capstone Project - Bilingual Personal Finance Tracker

A professional web-based financial management system built with Spring Boot, featuring full bilingual support (English/Turkish) for personal and family finance tracking.

## Live Demo

**Deployed Application:** [https://finance-manager.up.railway.app](https://finance-manager.up.railway.app/)

## Overview

Finance Manager is a comprehensive IT Capstone Project that helps individuals and families track their personal finances with support for both English and Turkish languages. The application provides transaction management, recurring bill tracking, interactive calendar views, and real-time financial dashboards, all available in the user's preferred language.

## Key Features

### Core Functionality
* **Transaction Management**: Track income and expenses with detailed categorization
* **Recurring Bills**: Automated monthly bill tracking with smart date handling
* **Scheduled Transactions**: One-time transactions with specific due dates
* **Financial Dashboard**: Real-time summary of income, expenses, and balance
* **Interactive Calendar**: Visual bill calendar with list and calendar views
* **Smart Filtering**: Filter transactions by type (income/expense) and category
* **Payment Tracking**: Mark bills as paid with automatic next-due-date calculation

### Bilingual Support (NEW!)
* **Dual Language UI**: Complete English and Turkish translations
* **Easy Language Switching**: Dropdown selector on all pages
* **Persistent Preference**: Language choice saved per user in database
* **Smart Date Handling**: Handles month-end edge cases for recurring bills

### Security & Quality
* **Secure Authentication**: BCrypt password encryption
* **Authorization**: User data isolation with ownership validation
* **Input Validation**: Comprehensive validation with custom exceptions
* **Professional Documentation**: Full Javadoc and technical specs
* **Clean Architecture**: MVC pattern with layered design

## Technology Stack

**Backend**
* Java 17
* Spring Boot 3.5.6
* Spring Security 6 (Form-based authentication, BCrypt, CSRF protection)
* Spring Data JPA with Hibernate
* PostgreSQL (production)
* H2 Database (development)
* Spring i18n for internationalization
* Maven for dependency management

**Frontend**
* Thymeleaf template engine
* Bootstrap 5.3.0 (responsive design)
* Bootstrap Icons 1.10.0
* Vanilla JavaScript

**Deployment**
* Railway platform
* Maven build automation

## Getting Started

### Prerequisites
* JDK 17 or higher
* Maven 3.6+ (or use included Maven wrapper)
* PostgreSQL 12+ (for production) or use H2 (development)

### Quick Start
```bash
# Clone the repository
git clone https://github.com/ardaaboz/finance-manager.git
cd finance-manager

# Run with Maven (uses H2 in-memory database)
./mvnw spring-boot:run

# Or run with your IDE
# Open project Run FinanceManagerApplication.java
```

**Access the application:** http://localhost:8080

**Test Credentials:**
* Username: `john` | Password: `password123`
* Username: `mary` | Password: `password456`

### Language Switching
* Use the language dropdown at the top of any page
* Select "English" or "Türkçe"
* Your preference is automatically saved when logged in

## Database Schema

### Users Table
* `id`: Primary key (auto-generated)
* `username`: Unique username (3-50 characters)
* `email`: Unique email address
* `password`: BCrypt encrypted password
* `preferred_language`: User's UI language preference ("en" or "tr")

### Transactions Table
* `id`: Primary key (auto-generated)
* `user_id`: Foreign key to users table
* `description`: Transaction description
* `amount`: Transaction amount (positive number)
* `type`: "INCOME" or "EXPENSE"
* `category`: Transaction category (Salary, Food, Rent, etc.)
* `is_recurring`: Boolean for monthly recurring bills
* `day_of_month`: Day (1-31) for recurring bills
* `next_due_date`: Calculated next due date for recurring bills
* `due_date`: Specific due date for one-time scheduled transactions
* `is_paid`: Payment status
* `created_date`: Timestamp of creation

## Project Structure
```
finance-manager/
├── src/main/java/com/example/financemanager/
│ ├── config/ # Configuration classes (Security, i18n, Exception Handler)
│ ├── controllers/ # HTTP request handlers (5 controllers)
│ ├── services/ # Business logic layer (UserService, TransactionService)
│ ├── repositories/ # Data access layer (JPA repositories)
│ ├── entities/ # Domain models (User, Transaction)
│ └── exceptions/ # Custom exception classes
├── src/main/resources/
│ ├── templates/ # Thymeleaf HTML templates (9 pages)
│ ├── messages_en.properties # English translations
│ ├── messages_tr.properties # Turkish translations
│ ├── application.properties # Development config
│ └── application-prod.properties # Production config
├── TECHNICAL_SPECIFICATION.md
├── DEVELOPMENT_GUIDE.md
├── PITFALLS_AND_CHALLENGES.md
└── pom.xml
```

## Documentation

 **Comprehensive technical documentation is available:**

* **[TECHNICAL_SPECIFICATION.md](TECHNICAL_SPECIFICATION.md)** - Complete architecture, database schema, API endpoints, security implementation, and i18n details
* **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** - Setup instructions, development workflow, testing, deployment, and troubleshooting
* **[PITFALLS_AND_CHALLENGES.md](PITFALLS_AND_CHALLENGES.md)** - Common issues, edge cases, security considerations, and best practices

## Security Features

* **Password Security**: BCrypt encryption with 10 rounds
* **CSRF Protection**: Enabled on all state-changing operations
* **User Isolation**: Authorization checks ensure users can only access their own data
* **Input Validation**: Jakarta Validation annotations on all user inputs
* **Custom Exceptions**: Proper error handling with user-friendly messages
* **Audit Trail**: Created dates tracked for all transactions
