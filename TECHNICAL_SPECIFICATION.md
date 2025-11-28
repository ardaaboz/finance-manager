# Technical Specification - Finance Manager
## IT Capstone Project

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Database Schema](#database-schema)
5. [API Endpoints](#api-endpoints)
6. [Security Implementation](#security-implementation)
7. [Internationalization (i18n)](#internationalization-i18n)
8. [Core Features](#core-features)

---

## Project Overview

**Finance Manager** is a full-stack web application developed as an IT Capstone Project. The application enables users to track their personal finances by managing income, expenses, recurring bills, and one-time transactions with due dates.

### Key Features
- User authentication and authorization
- Income and expense tracking
- Recurring bill management
- One-time transactions with due dates
- Financial summary dashboard
- Bill calendar view
- Transaction filtering and categorization
- **Bilingual support (English/Turkish)** with user-specific language preferences
- Responsive Bootstrap UI

### Project Context
- **Type**: IT Capstone Project
- **Purpose**: Personal finance management for individuals and families
- **Target Users**: Turkish and English-speaking users needing budget management
- **Deployment**: Railway platform with PostgreSQL database

---

## Architecture

### Architectural Pattern
The application follows the **MVC (Model-View-Controller)** pattern with a layered architecture:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (Thymeleaf Templates + Bootstrap)    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Controller Layer                │
│  (AuthController, TransactionController,│
│   DashboardController, BillController,  │
│   LanguageController)                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Service Layer                  │
│   (UserService, TransactionService)     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Repository Layer (JPA)            │
│  (UserRepository, TransactionRepository)│
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Database Layer                  │
│    (H2 for dev, PostgreSQL for prod)    │
└─────────────────────────────────────────┘
```

### Package Structure
```
com.example.financemanager/
├── config/              # Configuration classes
│   ├── DataInitializer.java
│   ├── SecurityConfig.java
│   ├── LocaleConfig.java
│   └── GlobalExceptionHandler.java
├── controllers/         # HTTP request handlers
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── TransactionController.java
│   ├── BillController.java
│   ├── HomeController.java
│   └── LanguageController.java
├── entities/            # JPA entities/domain models
│   ├── User.java
│   └── Transaction.java
├── repositories/        # Data access layer
│   ├── UserRepository.java
│   └── TransactionRepository.java
├── services/            # Business logic layer
│   ├── UserService.java
│   └── TransactionService.java
└── exceptions/          # Custom exceptions
    ├── UserNotFoundException.java
    ├── TransactionNotFoundException.java
    ├── UnauthorizedAccessException.java
    └── UserAlreadyExistsException.java
```

---

## Technology Stack

### Backend Technologies
| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.5.6 | Application framework |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.x | ORM and data persistence |
| **Hibernate** | 6.x | JPA implementation |
| **H2 Database** | Runtime | Development database |
| **PostgreSQL** | Latest | Production database |
| **Maven** | 3.x | Build tool and dependency management |
| **SLF4J + Logback** | Latest | Logging framework |

### Frontend Technologies
| Technology | Version | Purpose |
|-----------|---------|---------|
| **Thymeleaf** | 3.x | Server-side template engine |
| **Bootstrap** | 5.3.0 | CSS framework |
| **Bootstrap Icons** | 1.10.0 | Icon library |
| **HTML5** | - | Markup |
| **CSS3** | - | Styling |
| **JavaScript** | ES6+ | Client-side scripting |

### Development & Deployment
| Tool | Purpose |
|------|---------|
| **Spring Boot DevTools** | Hot reload during development |
| **Spring Boot Actuator** | Application monitoring |
| **Railway** | Cloud deployment platform |
| **Git** | Version control |

### Security Technologies
- **BCrypt** - Password hashing (strength: 10 rounds)
- **CSRF Protection** - Cross-site request forgery prevention
- **Session Management** - Stateful authentication with max 1 session per user
- **Jakarta Validation** - Input validation with annotations

---

## Database Schema

### User Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    preferred_language VARCHAR(2) DEFAULT 'en',
    CONSTRAINT users_username_unique UNIQUE (username),
    CONSTRAINT users_email_unique UNIQUE (email)
);
```

**Fields:**
- `id`: Auto-generated primary key
- `username`: Unique username (3-50 characters)
- `email`: Unique email address
- `password`: BCrypt-encrypted password
- `preferred_language`: User's preferred UI language ("en" or "tr")

**Indexes:**
- Primary key on `id`
- Unique index on `username`
- Unique index on `email`

### Transaction Table
```sql
CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- 'INCOME' or 'EXPENSE'
    category VARCHAR(100) NOT NULL,
    created_date DATE,
    due_date DATE,
    is_recurring BOOLEAN DEFAULT FALSE,
    day_of_month INTEGER,
    next_due_date DATE,
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Fields:**
- `id`: Auto-generated primary key
- `user_id`: Foreign key to users table
- `description`: Transaction description
- `amount`: Transaction amount (positive for both income and expenses)
- `type`: "INCOME" or "EXPENSE"
- `category`: Transaction category (Salary, Food, Rent, etc.)
- `created_date`: Date transaction was created
- `due_date`: For one-time transactions with specific due dates
- `is_recurring`: Whether this is a recurring transaction
- `day_of_month`: For recurring bills (1-31)
- `next_due_date`: Next occurrence date for recurring bills
- `is_paid`: Payment status for bills

**Relationships:**
- Many-to-One: Transaction → User
- Cascade DELETE: When user is deleted, all their transactions are deleted

**Recommended Indexes for Production:**
```sql
CREATE INDEX idx_transaction_user_id ON transaction(user_id);
CREATE INDEX idx_transaction_type ON transaction(type);
CREATE INDEX idx_transaction_category ON transaction(category);
CREATE INDEX idx_transaction_next_due_date ON transaction(next_due_date);
CREATE INDEX idx_transaction_is_recurring ON transaction(is_recurring);
```

### Entity-Relationship Diagram
```
┌─────────────────┐           ┌──────────────────┐
│      User       │           │   Transaction    │
├─────────────────┤           ├──────────────────┤
│ id (PK)         │───────┐   │ id (PK)          │
│ username        │       └──<│ user_id (FK)     │
│ email           │           │ description      │
│ password        │           │ amount           │
│ preferred_lang  │           │ type             │
└─────────────────┘           │ category         │
                              │ created_date     │
                              │ due_date         │
                              │ is_recurring     │
                              │ day_of_month     │
                              │ next_due_date    │
                              │ is_paid          │
                              └──────────────────┘
```

---

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | `/` | Root - redirects to dashboard or login | No |
| GET | `/login` | Display login page | No |
| POST | `/login` | Process login (handled by Spring Security) | No |
| GET | `/register` | Display registration page | No |
| POST | `/register` | Create new user account | No |
| POST | `/logout` | Log out user | Yes |

### Dashboard Endpoints

| Method | Endpoint | Parameters | Description | Auth Required |
|--------|----------|------------|-------------|---------------|
| GET | `/dashboard` | `type`, `category` (optional) | Main dashboard with financial summary and transactions | Yes |

**Query Parameters:**
- `type` - Filter by transaction type ("INCOME" or "EXPENSE")
- `category` - Filter by category (Salary, Food, Rent, etc.)

### Transaction Endpoints

| Method | Endpoint | Parameters | Description | Auth Required |
|--------|----------|------------|-------------|---------------|
| GET | `/add-transaction` | - | Display add transaction form | Yes |
| POST | `/add-transaction` | Form data | Create new transaction | Yes |
| GET | `/edit-transaction` | `id` | Display edit transaction form | Yes |
| POST | `/edit-transaction` | Form data | Update existing transaction | Yes |
| GET | `/delete-transaction` | `id` | Delete a transaction | Yes |

**Form Parameters for Add/Edit:**
- `description` (required): String description
- `amount` (required): Positive number
- `type` (required): "INCOME" or "EXPENSE"
- `category` (required): Category name
- `isRecurring` (optional): Boolean
- `dayOfMonth` (optional): Integer 1-31
- `dueDate` (optional): LocalDate in ISO format

### Bills Endpoints

| Method | Endpoint | Parameters | Description | Auth Required |
|--------|----------|------------|-------------|---------------|
| GET | `/bills` | `view`, `filter`, `year`, `month` | Display bills page | Yes |
| POST | `/mark-paid` | `id` | Mark bill as paid | Yes |
| POST | `/quick-add-transaction` | Form data | Quick add from bills page | Yes |

**Query Parameters:**
- `view` - "list" or "calendar" (default: list)
- `filter` - "all", "upcoming7", "upcoming30", "overdue", "paid", "unpaid"
- `year` - Year for calendar view (Integer)
- `month` - Month for calendar view (1-12)

### Language Endpoints

| Method | Endpoint | Parameters | Description | Auth Required |
|--------|----------|------------|-------------|---------------|
| POST | `/change-language` | `lang` | Change UI language | No (but persists if logged in) |

**Parameters:**
- `lang` - "en" for English or "tr" for Turkish

---

## Security Implementation

### Authentication
- **Type**: Form-based authentication
- **Session Management**: Stateful with HTTP sessions
- **Password Storage**: BCrypt hashing with default strength (10 rounds)
- **Login URL**: `/login`
- **Logout URL**: `/logout`
- **Default Success URL**: `/dashboard`

### Authorization
- **User Isolation**: Users can only access their own transactions
- **Service-Level Checks**: TransactionService validates ownership before operations
- **Exception Handling**: UnauthorizedAccessException thrown for unauthorized access

### Security Configuration Highlights
```java
// From SecurityConfig.java
- CSRF Protection: Enabled (except for H2 console in development)
- Session Management: Maximum 1 concurrent session per user
- Password Encoding: BCrypt
- H2 Console: Accessible only in development mode
```

### Input Validation
- **Jakarta Validation**: Annotations on controller parameters
- **@NotBlank**: Username, email, password, description
- **@Email**: Email format validation
- **@Size**: Username (3-50 chars), Password (min 6 chars)
- **@Positive**: Transaction amounts

### Security Best Practices Implemented
✅ Password encryption with BCrypt
✅ CSRF protection
✅ SQL injection prevention (JPA/Hibernate)
✅ XSS prevention (Thymeleaf auto-escaping)
✅ Session fixation protection (Spring Security default)
✅ Authorization checks in service layer
✅ Input validation
✅ Custom exception handling

### Security Considerations for Production
⚠️ **Recommendations:**
- Enable HTTPS/TLS
- Implement rate limiting for login attempts
- Add CAPTCHA to registration
- Implement password complexity requirements
- Add account lockout after failed login attempts
- Enable database SSL connections
- Set secure session cookie flags

---

## Internationalization (i18n)

### Implementation Details
- **Framework**: Spring i18n with Thymeleaf integration
- **Languages Supported**: English (en), Turkish (tr)
- **Default Language**: English
- **Message Files**:
  - `messages_en.properties` - English messages
  - `messages_tr.properties` - Turkish messages

### Locale Resolution
- **Type**: Session-based locale storage
- **Fallback**: If no locale in session, defaults to English
- **User Preference**: Stored in database (`users.preferred_language`)
- **Switching**: Via POST request to `/change-language?lang=<code>`

### Configuration
```java
// From LocaleConfig.java
- LocaleResolver: SessionLocaleResolver
- Default Locale: Locale.ENGLISH
- Locale Change Parameter: "lang"
- Message Encoding: UTF-8 (for Turkish characters: ğ, ü, ş, ı, ö, ç)
- Cache Duration: 3600 seconds (1 hour)
```

### Usage in Templates
```html
<!-- Thymeleaf message resolution -->
<h1 th:text="#{dashboard.title}">Dashboard</h1>
<label th:text="#{transaction.amount}">Amount</label>
```

### Supported Message Categories
- Navigation elements
- Form labels and buttons
- Validation messages
- Error messages
- Success messages
- Transaction types and categories
- Month names
- Currency symbols

### Workflow
1. User selects language from dropdown
2. POST request sent to `/change-language` with `lang` parameter
3. Session locale updated
4. If user is logged in, `preferred_language` field updated in database
5. On next login, user's preferred language loaded from database

---

## Core Features

### 1. Financial Dashboard
- **Summary Cards**: Total income, total expenses, net balance
- **Transaction List**: All transactions with filtering
- **Filters**: By type (INCOME/EXPENSE) and category
- **Calculations**: Real-time financial summaries

### 2. Transaction Management
- **Regular Transactions**: One-time income or expenses
- **Recurring Bills**: Monthly recurring transactions on specific day
- **Scheduled Transactions**: One-time transactions with due dates
- **CRUD Operations**: Create, read, update, delete all transaction types

### 3. Bill Management
- **Views**: List view and calendar view
- **Filters**: All, Upcoming (7/30 days), Overdue, Paid, Unpaid
- **Payment Tracking**: Mark bills as paid
- **Auto-calculation**: Next due date for recurring bills
- **Calendar Display**: Visual calendar showing bills by day of month

### 4. Date Calculation Logic
**For Recurring Bills:**
- Uses `dayOfMonth` field (1-31)
- Handles month-end edge cases (e.g., day 31 in February → day 28/29)
- Automatically calculates `nextDueDate`
- When marked paid, advances to next month

**Algorithm:**
```
1. Get current date
2. If dayOfMonth > days in current month:
   Set to last day of current month
3. Create date with year, month, and calculated day
4. If date is today or in past:
   Move to next month
   Recalculate day (handle short months)
```

### 5. User Management
- **Registration**: Username, email, password
- **Authentication**: Login with username and password
- **Uniqueness**: Username and email must be unique
- **Password Security**: BCrypt encryption
- **Language Preference**: Per-user language setting

### 6. Data Initialization
- **Sample Data**: Created on first run for testing
- **Test Users**:
  - john / password123
  - mary / password456
- **Sample Transactions**: Includes regular, recurring, and scheduled transactions

---

## Configuration Files

### application.properties (Development)
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:financedb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Logging
logging.level.org.springframework.security=DEBUG
```

### application-prod.properties (Production)
```properties
# PostgreSQL Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.driverClassName=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Logging
logging.level.org.springframework.web=INFO
```

### system.properties (Railway Deployment)
```properties
java.runtime.version=17
```

---

## Build and Deployment

### Maven Build
```bash
mvn clean package
```

### Local Development
```bash
mvn spring-boot:run
```

### Production Deployment (Railway)
1. Connect GitHub repository to Railway
2. Set environment variable: `DATABASE_URL`
3. Railway auto-detects Spring Boot and builds with Maven
4. Application deployed at assigned URL

---

## Future Enhancement Opportunities

1. **Budget Goals**: Set monthly spending limits by category
2. **Reporting**: Generate PDF/Excel financial reports
3. **Data Visualization**: Charts and graphs for spending patterns
4. **Mobile App**: React Native or Flutter mobile application
5. **Multi-Currency**: Support for different currencies
6. **Bank Integration**: Connect to bank accounts via APIs
7. **Shared Budgets**: Family/household budget sharing
8. **Notifications**: Email/SMS reminders for upcoming bills
9. **Export/Import**: CSV export and import functionality
10. **Two-Factor Authentication**: Enhanced security

---

**Document Version**: 1.0
**Last Updated**: 2025-01-28
**Project**: Finance Manager - IT Capstone Project
