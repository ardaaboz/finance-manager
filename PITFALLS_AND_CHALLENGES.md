# Pitfalls and Challenges - Finance Manager
## IT Capstone Project

This document outlines potential challenges, common pitfalls, and gotchas that developers may encounter when working on or deploying this Finance Manager application. Learn from these issues to save time and avoid frustration.

---

## Table of Contents
1. [Configuration Issues](#configuration-issues)
2. [Database Challenges](#database-challenges)
3. [Date and Time Handling](#date-and-time-handling)
4. [Security Pitfalls](#security-pitfalls)
5. [Internationalization Gotchas](#internationalization-gotchas)
6. [Performance Considerations](#performance-considerations)
7. [Deployment Challenges](#deployment-challenges)
8. [Testing Pitfalls](#testing-pitfalls)
9. [Frontend Issues](#frontend-issues)
10. [Best Practices to Avoid Problems](#best-practices-to-avoid-problems)

---

## Configuration Issues

### 1. Java Version Mismatch

**Problem:**
`pom.xml` and `system.properties` had different Java versions configured, causing deployment failures on Railway.

**Symptoms:**
- Application builds locally but fails on Railway
- Error: "UnsupportedClassVersionError"

**Solution:**
Ensure both files specify the same Java version:
```xml
<!-- pom.xml -->
<java.version>17</java.version>
```
```properties
# system.properties
java.runtime.version=17
```

**Prevention:**
- Always check both files when changing Java version
- Use a single source of truth (e.g., Maven enforcer plugin)

---

### 2. Profile-Specific Configuration Not Loading

**Problem:**
Production configuration (`application-prod.properties`) not being used.

**Symptoms:**
- H2 database used instead of PostgreSQL in production
- Logs show "spring.profiles.active=default"

**Solution:**
Set active profile:
```bash
# Via environment variable
export SPRING_PROFILES_ACTIVE=prod

# Via command line
java -jar app.jar --spring.profiles.active=prod

# Via application.properties
spring.profiles.active=prod
```

**Prevention:**
- Check Railway environment variables include `SPRING_PROFILES_ACTIVE=prod`
- Verify in startup logs which profile is active

---

### 3. Missing Database URL in Production

**Problem:**
Application fails to start because `DATABASE_URL` environment variable not set.

**Symptoms:**
```
Failed to configure a DataSource: 'url' attribute is not specified
```

**Solution:**
- In Railway: Add PostgreSQL service (automatically creates `DATABASE_URL`)
- Locally: Set in `application-prod.properties` or environment variable

**Prevention:**
- Always use Railway-provided PostgreSQL add-on
- Never hardcode database credentials

---

## Database Challenges

### 4. H2 to PostgreSQL Migration Issues

**Problem:**
Code works with H2 but breaks with PostgreSQL due to SQL dialect differences.

**Common Differences:**
| Feature | H2 | PostgreSQL |
|---------|-----|------------|
| Auto-increment | `AUTO_INCREMENT` | `SERIAL` or `BIGSERIAL` |
| Boolean type | `BOOLEAN` | `BOOLEAN` (same) |
| String comparison | Case-insensitive | Case-sensitive |
| Date functions | Different syntax | Different syntax |

**Solution:**
- Use JPA annotations instead of native SQL
- Let Hibernate generate SQL: `spring.jpa.hibernate.ddl-auto=update`
- Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment

**Prevention:**
- Test with PostgreSQL locally before deploying
- Use Docker to run PostgreSQL locally:
```bash
docker run --name postgres-test -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres
```

---

### 5. N+1 Query Problem

**Problem:**
Loading User entity triggers separate queries for each Transaction, causing performance issues.

**Symptoms:**
- Slow dashboard loading
- Hundreds of SQL queries in logs when loading 10 transactions

**Example:**
```java
// This causes N+1 queries
List<Transaction> transactions = transactionRepository.findByUser(user);
// 1 query for transactions + N queries for each transaction's user
```

**Solution:**
Use JOIN FETCH or EntityGraph:
```java
@Query("SELECT t FROM Transaction t JOIN FETCH t.user WHERE t.user = :user")
List<Transaction> findByUserWithUser(@Param("user") User user);
```

**Prevention:**
- Enable SQL logging: `spring.jpa.show-sql=true`
- Count queries during development
- Use Hibernate statistics

---

### 6. Data Loss with ddl-auto=create-drop

**Problem:**
Using `spring.jpa.hibernate.ddl-auto=create-drop` in production causes data loss on every restart.

**Symptoms:**
- All user data disappears after server restart
- Fresh database on every deployment

**Solution:**
Use correct `ddl-auto` values:
- **Development**: `create-drop` or `create` (H2 only)
- **Production**: `update` or `validate`
- **Never use**: `create-drop` in production!

**Prevention:**
- Different `application-{profile}.properties` files
- Code review checklist includes database configuration

---

## Date and Time Handling

### 7. Month-End Recurring Bill Edge Cases

**Problem:**
Recurring bills with `dayOfMonth=31` fail in months with fewer days.

**Example Issue:**
- User sets bill due on day 31
- In February (28/29 days), what happens?

**Our Solution:**
```java
// Calculate actual day, handling short months
int lastDayOfMonth = today.lengthOfMonth();
int actualDay = Math.min(dayOfMonth, lastDayOfMonth);
```

**Pitfall:**
If you don't handle this, you'll get:
```
java.time.DateTimeException: Invalid date 'FEBRUARY 31'
```

**Test Cases to Consider:**
- Day 29, 30, 31 in February
- Day 31 in April, June, September, November
- Leap year February (day 29)

---

### 8. Time Zone Inconsistencies

**Problem:**
LocalDate.now() uses server time zone, which may differ from user's time zone.

**Symptoms:**
- Bills due "today" show as overdue or upcoming incorrectly
- Date stamps on transactions are wrong

**Current Limitation:**
Application uses server time zone (not user-specific).

**Future Enhancement:**
- Store user time zone preference
- Use ZonedDateTime instead of LocalDate
- Display dates in user's time zone

**Workaround:**
Deploy to server in your primary user's time zone.

---

## Security Pitfalls

### 9. Password Complexity Not Enforced

**Problem:**
Users can create weak passwords like "123456".

**Current State:**
Only minimum length validation (6 characters).

**Risk:**
- Brute force attacks easier
- Dictionary attacks possible

**Recommended Enhancement:**
```java
@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
         message = "Password must contain uppercase, lowercase, number, and special character")
```

**Best Practice:**
- Minimum 8 characters
- At least one uppercase, lowercase, number, special character
- Consider password strength meter on frontend

---

### 10. No Rate Limiting on Login

**Problem:**
Attackers can attempt unlimited login attempts.

**Risk:**
- Brute force password guessing
- Account enumeration (testing if usernames exist)

**Recommended Solution:**
Implement rate limiting with Spring Security:
```java
// Limit to 5 attempts per 15 minutes
// Lock account after 5 failed attempts
// Add CAPTCHA after 3 failed attempts
```

**Tools to Consider:**
- Bucket4j (rate limiting library)
- Spring Security login failure handlers
- Redis for distributed rate limiting

---

### 11. CSRF Token Issues with AJAX

**Problem:**
If you add AJAX requests without CSRF token, they fail with 403 Forbidden.

**Symptoms:**
```
Invalid CSRF Token 'null' was found on the request parameter '_csrf'
```

**Solution:**
Include CSRF token in AJAX requests:
```javascript
// In Thymeleaf template
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>

// In JavaScript
const token = document.querySelector('meta[name="_csrf"]').content;
const header = document.querySelector('meta[name="_csrf_header"]').content;

fetch('/api/endpoint', {
    method: 'POST',
    headers: {
        [header]: token
    }
});
```

**Prevention:**
- Always include CSRF tokens in AJAX requests
- Or use REST API with JWT (stateless, no CSRF needed)

---

## Internationalization Gotchas

### 12. Turkish Character Encoding Issues

**Problem:**
Turkish characters (ğ, ü, ş, ı, ö, ç, Ğ, Ü, Ş, İ, Ö, Ç) display as � or garbled text.

**Root Cause:**
- Properties files not saved as UTF-8
- MessageSource not configured for UTF-8
- HTML not declaring UTF-8 charset

**Solution:**
1. **Save files as UTF-8**:
   - In IntelliJ: Settings → Editor → File Encodings → UTF-8
   - In VS Code: Save with encoding → UTF-8

2. **Configure MessageSource**:
```java
messageSource.setDefaultEncoding("UTF-8");
```

3. **HTML charset**:
```html
<meta charset="UTF-8">
```

**Prevention:**
- Set IDE default encoding to UTF-8
- Add file encoding check to build process

---

### 13. Message Key Not Found

**Problem:**
Missing translation key causes Thymeleaf to display "??key_name??" instead of text.

**Symptoms:**
```html
<!-- Instead of "Dashboard" -->
??dashboard.title??
```

**Root Causes:**
- Typo in message key
- Key exists in `messages_en.properties` but missing in `messages_tr.properties`
- File not in classpath (wrong directory)

**Solution:**
1. Verify key exists in BOTH files
2. Check spelling exactly matches
3. Ensure files are in `src/main/resources/`

**Prevention:**
- Use constants for message keys
- Automated testing to check all keys exist in all locale files
- IDE plugin for i18n validation

---

### 14. Locale Not Persisting After Logout

**Problem:**
User sets language to Turkish, logs out, logs back in → language resets to English.

**Our Solution:**
Store `preferredLanguage` in User entity and load on login.

**Implementation Needed:**
Custom authentication success handler to set locale from user preference:
```java
@Component
public class CustomAuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private LocaleResolver localeResolver;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Locale locale = new Locale(user.getPreferredLanguage());
        localeResolver.setLocale(request, response, locale);
        // Redirect to dashboard
    }
}
```

---

## Performance Considerations

### 15. Loading All Transactions on Dashboard

**Problem:**
Dashboard loads ALL user transactions without pagination.

**Impact:**
- User with 1000 transactions → slow page load
- High memory usage
- Database query returns too much data

**Current Limitation:**
No pagination implemented.

**Recommended Enhancement:**
```java
// Use Spring Data Pagination
Page<Transaction> findByUser(User user, Pageable pageable);

// In controller
Pageable pageable = PageRequest.of(page, 20); // 20 per page
Page<Transaction> transactions = transactionService.getUserTransactions(username, pageable);
```

**Workaround:**
Filter transactions to show only recent ones:
```java
// Last 30 days only
LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
List<Transaction> recentTransactions = transactionRepository
    .findByUserAndCreatedDateAfter(user, thirtyDaysAgo);
```

---

### 16. No Database Indexing

**Problem:**
No indexes on frequently queried columns causes slow queries as data grows.

**Symptoms:**
- Dashboard gets slower over time
- Queries take seconds instead of milliseconds

**Recommended Indexes:**
```sql
CREATE INDEX idx_transaction_user_id ON transaction(user_id);
CREATE INDEX idx_transaction_type ON transaction(type);
CREATE INDEX idx_transaction_category ON transaction(category);
CREATE INDEX idx_transaction_next_due_date ON transaction(next_due_date);
CREATE INDEX idx_transaction_is_recurring ON transaction(is_recurring);
CREATE INDEX idx_transaction_created_date ON transaction(created_date);
```

**Adding Indexes with JPA:**
```java
@Table(name = "transaction", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "type")
})
public class Transaction { ... }
```

---

## Deployment Challenges

### 17. Railway Deployment Timeout

**Problem:**
Railway build times out during `mvn package`.

**Causes:**
- Slow dependency downloads
- Large number of dependencies
- Resource limits on Railway free tier

**Solution:**
1. **Use Railway build cache** (automatic)
2. **Optimize pom.xml**: Remove unused dependencies
3. **Upgrade Railway plan** if needed

**Prevention:**
- Keep dependencies minimal
- Use Maven wrapper to lock versions

---

### 18. Environment Variable Not Loaded

**Problem:**
`DATABASE_URL` set in Railway but application doesn't see it.

**Symptoms:**
```
Error: Could not find or load main class ${DATABASE_URL}
```

**Cause:**
Railway variables not injected correctly.

**Solution:**
- Restart deployment after adding variables
- Check variable name is exactly `DATABASE_URL`
- Use Railway CLI to verify: `railway variables`

---

### 19. H2 Console Exposed in Production

**Problem:**
H2 console left enabled in production = security risk!

**Risk:**
Anyone can access `/h2-console` and view/modify database.

**Solution:**
```properties
# application-prod.properties
spring.h2.console.enabled=false
```

**Prevention:**
- Use profiles correctly
- Security audit checklist

---

## Testing Pitfalls

### 20. Test Data Interferes with Production Data

**Problem:**
DataInitializer runs in production, creating test users john/mary.

**Risk:**
- Test accounts accessible in production
- Data pollution

**Our Current State:**
DataInitializer only runs if no users exist (prevents duplicates).

**Better Solution:**
```java
@Profile("!prod") // Don't run in production
@Component
public class DataInitializer { ... }
```

**Prevention:**
- Use `@Profile` annotations
- Separate test data from production config

---

### 21. Tests Not Isolated

**Problem:**
Tests depend on execution order or shared state.

**Example:**
```java
// Test 1 creates user "john"
// Test 2 assumes "john" exists
// Test 2 fails when run alone
```

**Solution:**
- Each test should be independent
- Use `@Transactional` on tests (auto-rollback)
- Or clean database before each test:
```java
@BeforeEach
public void setup() {
    transactionRepository.deleteAll();
    userRepository.deleteAll();
}
```

---

## Frontend Issues

### 22. Thymeleaf Template Not Found

**Problem:**
`TemplateInputException: Error resolving template "dasboard"` (typo!).

**Common Causes:**
1. **Typo in return value**:
```java
return "dasboard"; // Should be "dashboard"
```

2. **Wrong directory**:
```
src/main/resources/templates/views/dashboard.html // Wrong!
src/main/resources/templates/dashboard.html // Correct
```

3. **Wrong file extension**:
```
dashboard.tpl // Wrong!
dashboard.html // Correct
```

**Prevention:**
- Use constants for template names
- Enable IDE autocomplete for template names

---

### 23. Bootstrap JavaScript Not Working

**Problem:**
Dropdowns, modals, etc. don't work.

**Cause:**
Missing Bootstrap JavaScript or jQuery dependency.

**Solution:**
Include in `<head>`:
```html
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
```

**Note:**
Bootstrap 5 doesn't require jQuery!

---

### 24. Form Submission Returns 405 Method Not Allowed

**Problem:**
Form submission fails with 405 error.

**Common Causes:**
1. **Wrong method**:
```html
<form th:action="@{/register}" method="get"> <!-- Should be POST! -->
```

2. **Missing @PostMapping**:
```java
@GetMapping("/register") // Should have both GET and POST
public String register() { ... }
```

**Solution:**
- Forms that modify data → POST
- Forms that query data → GET
- Always have both mappings if showing form and processing it

---

## Best Practices to Avoid Problems

### Code Quality

✅ **DO:**
- Use custom exceptions instead of `RuntimeException`
- Add null checks for repository queries
- Use constructor injection instead of field injection
- Add comprehensive Javadoc
- Use SLF4J logger, not `System.out.println`
- Validate all user inputs
- Handle edge cases (month-end dates, empty lists, etc.)

❌ **DON'T:**
- Leave test credentials in production code
- Hardcode values (use enums or constants)
- Ignore compiler warnings
- Skip null checks
- Use `@Autowired` on fields (use constructor injection)

---

### Database

✅ **DO:**
- Use JPA/Hibernate for database access
- Add indexes on foreign keys and frequently queried columns
- Use transactions for multi-step operations
- Use proper `ddl-auto` values per environment
- Plan for data growth (pagination, archiving)

❌ **DON'T:**
- Use `create-drop` in production
- Write native SQL unless necessary
- Ignore N+1 query problems
- Load entire tables into memory

---

### Security

✅ **DO:**
- Encrypt passwords with BCrypt
- Validate all inputs (backend AND frontend)
- Use CSRF protection
- Implement rate limiting
- Keep dependencies updated (security patches)
- Use HTTPS in production
- Set secure session cookies

❌ **DON'T:**
- Store passwords in plain text
- Trust user input
- Expose stack traces to users
- Disable security features
- Hardcode secrets in code

---

### Deployment

✅ **DO:**
- Use environment variables for sensitive config
- Test on production-like environment first
- Use proper logging (not just `System.out`)
- Monitor application health (Actuator)
- Have database backups
- Document deployment process

❌ **DON'T:**
- Deploy directly to production without testing
- Commit sensitive data to Git
- Use development settings in production
- Ignore warnings/errors in logs

---

## Summary of Critical Issues

| Priority | Issue | Impact | Fix Difficulty |
|----------|-------|--------|----------------|
| 🔴 Critical | Java version mismatch | Deployment failure | Easy |
| 🔴 Critical | `ddl-auto=create-drop` in prod | Data loss | Easy |
| 🔴 Critical | H2 console in production | Security risk | Easy |
| 🟠 High | No password complexity | Security risk | Medium |
| 🟠 High | No rate limiting | Security risk | Medium |
| 🟠 High | N+1 queries | Performance | Medium |
| 🟠 High | No pagination | Performance | Medium |
| 🟡 Medium | Turkish encoding | UX issue | Easy |
| 🟡 Medium | No database indexes | Performance | Easy |
| 🟡 Medium | Month-end date edge cases | Logic bugs | Hard |

---

## Quick Checklist Before Deployment

- [ ] Java version matches in `pom.xml` and `system.properties`
- [ ] `spring.jpa.hibernate.ddl-auto=update` in production
- [ ] `spring.h2.console.enabled=false` in production
- [ ] All environment variables set in Railway
- [ ] Database backups configured
- [ ] HTTPS enabled
- [ ] Test users removed or disabled
- [ ] `spring.jpa.show-sql=false` in production
- [ ] Logging level set to WARN or ERROR
- [ ] i18n files saved as UTF-8
- [ ] All message keys exist in both locale files
- [ ] Tested in both languages
- [ ] Security audit completed

---

**Document Version**: 1.0
**Last Updated**: 2025-01-28
**Remember**: Most bugs are easier to prevent than to fix. Review this document before starting new features or deploying!
