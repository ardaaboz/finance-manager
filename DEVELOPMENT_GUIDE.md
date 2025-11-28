# Development Guide - Finance Manager
## IT Capstone Project

This guide will help developers set up, run, and contribute to the Finance Manager project.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Running the Application](#running-the-application)
4. [Development Workflow](#development-workflow)
5. [Project Structure](#project-structure)
6. [Adding New Features](#adding-new-features)
7. [Testing](#testing)
8. [Deployment](#deployment)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
| Software | Minimum Version | Purpose | Download Link |
|----------|----------------|---------|---------------|
| **JDK** | 17 or higher | Java runtime | https://adoptium.net/ |
| **Maven** | 3.6+ | Build tool | https://maven.apache.org/ |
| **Git** | 2.x+ | Version control | https://git-scm.com/ |
| **IDE** | Any | Development | IntelliJ IDEA / Eclipse / VS Code |
| **PostgreSQL** | 12+ (for production) | Database | https://www.postgresql.org/ |

### Recommended IDE Setup
**IntelliJ IDEA** (Recommended):
- Install Spring Boot plugin
- Enable Lombok annotation processing
- Configure Maven auto-import

**VS Code**:
- Install "Extension Pack for Java"
- Install "Spring Boot Extension Pack"

---

## Initial Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd finance-manager
```

### 2. Verify Java Installation
```bash
java -version
# Should show Java 17 or higher

mvn -version
# Should show Maven 3.6 or higher
```

### 3. Install Dependencies
```bash
mvn clean install
```

This will download all required dependencies from Maven Central.

### 4. Configure Environment

#### Development Mode (H2 Database)
No additional configuration needed! The application uses H2 in-memory database by default.

#### Production Mode (PostgreSQL)
Create `application-prod.properties` in `src/main/resources/`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financedb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

---

## Running the Application

### Method 1: Using Maven
```bash
mvn spring-boot:run
```

### Method 2: Using IDE
1. Open project in your IDE
2. Find `FinanceManagerApplication.java`
3. Right-click → Run

### Method 3: Using JAR
```bash
mvn clean package
java -jar target/finance-manager-0.0.1-SNAPSHOT.jar
```

### Accessing the Application
Once running, open your browser:
- **URL**: http://localhost:8080
- **H2 Console** (dev only): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:financedb`
  - Username: `sa`
  - Password: (leave empty)

### Test User Accounts
Sample data is automatically created on first run:
- **User 1**: john / password123
- **User 2**: mary / password456

---

## Development Workflow

### Branch Strategy
```
main (stable, production-ready code)
└── feature/feature-name (new features)
└── bugfix/issue-description (bug fixes)
└── hotfix/critical-issue (urgent fixes)
```

### Making Changes

1. **Create a new branch**
```bash
git checkout -b feature/your-feature-name
```

2. **Make your changes**
- Follow existing code style
- Add proper Javadoc comments
- Test your changes locally

3. **Commit your changes**
```bash
git add .
git commit -m "Add feature: description of changes"
```

4. **Push to remote**
```bash
git push origin feature/your-feature-name
```

5. **Create Pull Request**
- Go to GitHub repository
- Create PR from your feature branch to main
- Add description of changes

### Code Style Guidelines
- **Naming Conventions**:
  - Classes: PascalCase (`UserService`)
  - Methods: camelCase (`createUser`)
  - Constants: UPPER_SNAKE_CASE (`MAX_LOGIN_ATTEMPTS`)
  - Packages: lowercase (`com.example.financemanager`)

- **Javadoc**: All public classes and methods must have Javadoc
- **Logging**: Use SLF4J logger, not `System.out.println`
- **Exception Handling**: Use custom exceptions, not generic `RuntimeException`

---

## Project Structure

```
finance-manager/
├── src/
│   ├── main/
│   │   ├── java/com/example/financemanager/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controllers/         # MVC controllers
│   │   │   ├── entities/            # JPA entities
│   │   │   ├── exceptions/          # Custom exceptions
│   │   │   ├── repositories/        # Data access layer
│   │   │   ├── services/            # Business logic
│   │   │   └── FinanceManagerApplication.java
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf HTML templates
│   │       ├── static/              # CSS, JS, images (if any)
│   │       ├── messages_en.properties  # English i18n messages
│   │       ├── messages_tr.properties  # Turkish i18n messages
│   │       ├── application.properties  # Dev config
│   │       └── application-prod.properties  # Prod config
│   └── test/                        # Unit and integration tests
├── pom.xml                          # Maven configuration
├── system.properties                # Railway deployment config
├── README.md
├── TECHNICAL_SPECIFICATION.md
├── DEVELOPMENT_GUIDE.md
└── PITFALLS_AND_CHALLENGES.md
```

---

## Adding New Features

### Example: Adding a New Transaction Category

#### 1. Update Message Files
Add to `messages_en.properties`:
```properties
category.entertainment=Entertainment
```

Add to `messages_tr.properties`:
```properties
category.entertainment=Eğlence
```

#### 2. Update HTML Templates
In `dashboard.html`, add to category dropdown:
```html
<option value="Entertainment" th:text="#{category.entertainment}">Entertainment</option>
```

#### 3. Test
- Create a transaction with the new category
- Verify it displays correctly in English and Turkish

### Example: Adding a New Controller Endpoint

#### 1. Create Controller Method
```java
@GetMapping("/my-new-endpoint")
public String myNewEndpoint(Model model, Principal principal) {
    String username = principal.getName();
    // Add logic here
    model.addAttribute("data", someData);
    return "my-template";
}
```

#### 2. Create Thymeleaf Template
Create `src/main/resources/templates/my-template.html`

#### 3. Add i18n Messages
Update both `messages_en.properties` and `messages_tr.properties`

#### 4. Add Navigation Link (if needed)
Update navigation in relevant templates

---

## Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Writing Tests

#### Unit Test Example (Service Layer)
```java
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testCreateUser() {
        userService.createUser("testuser", "test@email.com", "password");
        // Add assertions
    }
}
```

#### Integration Test Example (Controller)
```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk())
               .andExpect(view().name("login"));
    }
}
```

### Manual Testing Checklist
- [ ] User registration works
- [ ] User login works
- [ ] Can create all transaction types (regular, recurring, with due date)
- [ ] Can edit transactions
- [ ] Can delete transactions
- [ ] Dashboard shows correct financial summary
- [ ] Bills page displays correctly (both views)
- [ ] Can mark bills as paid
- [ ] Language switching works
- [ ] Language preference persists after logout/login
- [ ] Both English and Turkish display correctly

---

## Deployment

### Local Deployment
Already covered in "Running the Application" section.

### Railway Deployment

#### Prerequisites
- Railway account (https://railway.app/)
- GitHub repository

#### Steps
1. **Connect Railway to GitHub**
   - Login to Railway
   - Create new project
   - Select "Deploy from GitHub repo"
   - Authorize Railway to access your repository

2. **Configure Environment Variables**
   - In Railway dashboard, go to Variables tab
   - Add:
     - `DATABASE_URL`: Automatically provided by Railway PostgreSQL

3. **Add PostgreSQL Database**
   - In Railway dashboard, click "New"
   - Select "Database" → "PostgreSQL"
   - Railway will automatically create and link the database

4. **Deploy**
   - Railway automatically detects Spring Boot project
   - Builds using Maven
   - Deploys application
   - Provides public URL

5. **Verify Deployment**
   - Visit the provided Railway URL
   - Test login and registration
   - Check database persistence

#### Monitoring
- Railway provides logs in the Deployments tab
- Use Spring Boot Actuator endpoints (if exposed)

### Environment-Specific Configuration

#### Development
```properties
# application.properties
spring.profiles.active=default
spring.jpa.show-sql=true
logging.level.root=INFO
```

#### Production
```properties
# application-prod.properties
spring.jpa.show-sql=false
logging.level.root=WARN
spring.jpa.hibernate.ddl-auto=update
```

---

## Troubleshooting

### Common Issues

#### Issue: "Port 8080 already in use"
**Solution:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

#### Issue: "Failed to configure a DataSource"
**Cause**: Database configuration missing

**Solution**:
- Check `application.properties` has correct datasource URL
- For H2, ensure H2 dependency is in pom.xml
- For PostgreSQL, ensure PostgreSQL is running

#### Issue: "java.lang.ClassNotFoundException: jakarta.servlet.Servlet"
**Cause**: Incorrect Java version

**Solution**:
```bash
# Ensure Java 17 is being used
java -version
# If not, set JAVA_HOME to Java 17 installation
```

#### Issue: Thymeleaf templates not loading
**Cause**: Templates not in correct location

**Solution**:
- Ensure templates are in `src/main/resources/templates/`
- File names must end with `.html`
- Controller return values must match template names (without .html extension)

#### Issue: i18n messages not displaying
**Cause**: Message files not found or incorrect locale

**Solution**:
- Verify `messages_en.properties` and `messages_tr.properties` exist in `src/main/resources/`
- Check encoding is UTF-8
- Verify message keys match in templates: `#{key.name}`

#### Issue: "User already exists" when running app
**Cause**: Database persisted from previous run

**Solution**:
- For H2: Restart application (H2 is in-memory, data clears)
- For PostgreSQL: Clear database or drop/recreate tables

#### Issue: Turkish characters (ğ, ü, ş, ı, ö, ç) not displaying correctly
**Cause**: Encoding issue

**Solution**:
- Ensure `messages_tr.properties` saved with UTF-8 encoding
- Verify `MessageSource` bean has `setDefaultEncoding("UTF-8")`
- In IDE, set file encoding to UTF-8

### Getting Help
- Check existing GitHub Issues
- Review TECHNICAL_SPECIFICATION.md
- Review PITFALLS_AND_CHALLENGES.md
- Check Spring Boot documentation: https://docs.spring.io/spring-boot/

### Debugging Tips
- Enable SQL logging: `spring.jpa.show-sql=true`
- Enable Spring Security debug: `logging.level.org.springframework.security=DEBUG`
- Use H2 console to inspect database: http://localhost:8080/h2-console
- Add breakpoints in IDE and run in debug mode
- Check application logs in console output

---

## Useful Commands

### Maven Commands
```bash
# Clean build artifacts
mvn clean

# Compile code
mvn compile

# Run tests
mvn test

# Package as JAR
mvn package

# Skip tests during package
mvn package -DskipTests

# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Git Commands
```bash
# Check status
git status

# Create new branch
git checkout -b feature/new-feature

# Switch branches
git checkout main

# Pull latest changes
git pull origin main

# View commit history
git log --oneline

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard local changes
git checkout -- <file>
```

### Database Commands (PostgreSQL)
```bash
# Connect to PostgreSQL
psql -U username -d financedb

# List tables
\dt

# Describe table structure
\d users
\d transaction

# Run query
SELECT * FROM users;

# Exit
\q
```

---

## Code Quality Tools (Optional)

### SonarLint (IDE Plugin)
- Real-time code quality feedback
- Available for IntelliJ IDEA, Eclipse, VS Code

### Checkstyle
Add to `pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.0</version>
</plugin>
```

Run: `mvn checkstyle:check`

### SpotBugs
Add to `pom.xml`:
```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.6</version>
</plugin>
```

Run: `mvn spotbugs:check`

---

## Contributing Guidelines

### Before Submitting Code
- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] New features have tests
- [ ] Code follows project style guidelines
- [ ] Javadoc added for public methods
- [ ] No `System.out.println` statements
- [ ] i18n messages added for both English and Turkish
- [ ] Tested in both languages

### Pull Request Template
```
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe how you tested your changes

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed code
- [ ] Commented complex code
- [ ] Updated documentation
- [ ] Added tests
- [ ] All tests pass
```

---

## Additional Resources

### Spring Boot Documentation
- Official Docs: https://docs.spring.io/spring-boot/
- Spring Security: https://docs.spring.io/spring-security/
- Spring Data JPA: https://docs.spring.io/spring-data/jpa/

### Thymeleaf
- Documentation: https://www.thymeleaf.org/documentation.html
- Tutorial: https://www.baeldung.com/thymeleaf-in-spring-mvc

### Bootstrap
- Documentation: https://getbootstrap.com/docs/5.3/

### Tools
- Railway Docs: https://docs.railway.app/
- Maven Guide: https://maven.apache.org/guides/
- Git Tutorial: https://www.atlassian.com/git/tutorials

---

**Document Version**: 1.0
**Last Updated**: 2025-01-28
**Maintained by**: Development Team
