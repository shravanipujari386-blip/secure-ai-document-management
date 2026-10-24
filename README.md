# Secure AI-Powered Digital Document Management System for Law Enforcement

This is an **Eclipse-importable Spring Boot starter scaffold**, not the completed production system. It includes the Maven setup, Thymeleaf login/dashboard pages, a basic Spring Security form-login configuration, initial JPA entities/repositories, MySQL configuration, and styling.

## Technology stack
- Java 21
- Spring Boot 3.5.6
- Maven
- Spring MVC, Thymeleaf, Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Apache PDFBox dependency for future PDF text extraction
- Postman for API testing

## Requirements
- JDK 21
- Eclipse IDE with Maven support
- MySQL Server

## 1. Create the database
Run this in MySQL:

```sql
CREATE DATABASE secure_document_db;
```

## 2. Configure database credentials
Set these environment variables before starting Eclipse, or update `src/main/resources/application.properties` for local development:
- `DB_URL` (optional; defaults to `jdbc:mysql://localhost:3306/secure_document_db`)
- `DB_USERNAME` (optional; defaults to `root`)
- `DB_PASSWORD` (required for your actual local MySQL password)

Do not commit real passwords or API keys.

## 3. Import into Eclipse
1. Extract the ZIP.
2. Open Eclipse.
3. Select **File → Import → Maven → Existing Maven Projects**.
4. Select the extracted `SecureDocumentManagement` folder.
5. Finish and wait for Maven dependencies to download.
6. Ensure the project uses JDK 21.

## 4. Run
Run `SecureDocumentManagementApplication.java` as **Java Application** or **Spring Boot App** if Spring Tools is installed.

Open `http://localhost:8080/login`.

## Important authentication note
The current scaffold uses Spring Security form login, but it does **not** yet contain a user registration service, administrator approval workflow, or a configured database-backed `UserDetailsService`. Spring Security's generated development password may appear in the Eclipse console. This is a starter login configuration only; user provisioning and production-grade authentication still need to be implemented.

## Current scaffold contents
- Spring Boot application entry point
- Basic Spring Security login/logout configuration
- Thymeleaf login page
- Dashboard placeholder
- Initial `User`, `CaseRecord`, `DocumentRecord`, and `AuditLog` entities
- Initial JPA repositories
- MySQL configuration
- Basic responsive CSS
- Dependencies for future PDF text extraction and testing

## Not yet implemented
- Database-backed authentication and account approval
- Registration/email verification
- Admin, Officer, Investigator authorization rules
- Case-assignment enforcement
- Document upload/download service and secure file storage
- Audit service and audit event wiring
- REST API controllers
- JWT API authentication
- AI summarization, classification, and intelligent search
- A Postman collection
- Automated integration/security tests

Do not use this starter scaffold with real confidential law-enforcement records. Complete and test the missing security and document controls first.
