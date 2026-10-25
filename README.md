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

## Implemented Features

### 1. User Authentication

* User registration and login.
* Authentication to restrict access to the application.

### 2. Email Verification

* Email verification during account registration.
* Verification flow to help validate user email addresses.

### 3. Document Management

* Document upload functionality.
* Integration with the document processing workflow.

### 4. AI-Powered Document Analysis

* AI-based analysis of uploaded documents.
* Processing of document content to generate useful information and insights.

### 5. Dashboard

* Dashboard interface for accessing application functionality.
* Centralized access to document management and analysis features.

### 6. Database Integration

* Database support for application data.
* Spring Data JPA and Hibernate integration.

### 7. Security

* Spring Security integration.
* Authentication and email verification workflows.

## Future Enhancements

The following features can be considered for future development, depending on the current implementation:

* Role-based access control for administrators, officers, and investigators.
* Case assignment and case-level access restrictions.
* Comprehensive audit logging for security-sensitive operations.
* Additional REST API endpoints and a Postman collection.
* Automated integration and security testing.
* Advanced AI-powered document classification and intelligent search.

## Security Notice

Although the application implements authentication, email verification, document uploads, and AI-powered document analysis, further security testing is necessary before using it with confidential law-enforcement records. Verify access permissions, document storage, data protection, and AI-processing safeguards before production deployment.

