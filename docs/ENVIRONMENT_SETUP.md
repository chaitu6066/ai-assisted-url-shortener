# Windows Development Environment Setup

## Project

AI-Assisted Production-Grade URL Shortener

## 1. Purpose

This document prepares a Windows 11 development machine to build, run, test, and submit the URL Shortener prototype.

The current machine already has:

- Windows 11;
- Spring Tool Suite / Eclipse;
- Apache Maven 3.9.12;
- Git 2.51.0;
- Oracle JDK 22.

The required setup work is therefore limited to:

1. install Java 21 LTS side by side;
2. configure Spring Tools and Maven to use Java 21;
3. install PostgreSQL;
4. create an application database and non-superuser account;
5. verify the complete toolchain.

---

# 2. Final Local Toolchain

| Tool | Target |
|---|---|
| Java | Eclipse Temurin JDK 21 LTS |
| Build | Apache Maven 3.9.12 |
| IDE | Existing Spring Tools / Eclipse |
| Database | PostgreSQL 18 |
| Database UI | pgAdmin, installed with PostgreSQL |
| Version control | Git 2.51.0 |
| API testing | Swagger UI; Postman optional |

Postman is not required for the assignment because Swagger and automated tests are sufficient.

---

# 3. Create the Local Workspace

Create:

```text
D:\Projects\url-shortener
```

Expected initial structure:

```text
url-shortener/
└── docs/
    ├── FUNCTIONAL_REQUIREMENTS.md
    ├── NON_FUNCTIONAL_REQUIREMENTS.md
    ├── ARCHITECTURE.md
    ├── API_DESIGN.md
    ├── DATABASE_DESIGN.md
    ├── PACKAGE_AND_CLASS_DESIGN.md
    ├── ENGINEERING_SCENARIOS.md
    └── INTERVIEW_QUESTIONS_AND_ANSWERS.md
```

Do not create `src` manually. Spring Initializr will create the application structure during project bootstrap.

---

# 4. Install Java 21 LTS

Open **PowerShell as Administrator** and run:

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Accept the package agreements when prompted.

Java 21 can coexist with the existing Oracle JDK 22.

## 4.1 Find the Java 21 installation

After installation, open a new PowerShell window:

```powershell
Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory
```

Expected directory pattern:

```text
C:\Program Files\Eclipse Adoptium\jdk-21...
```

## 4.2 Configure `JAVA_HOME`

Open:

```text
Windows Search
→ Edit the system environment variables
→ Environment Variables
```

Under **User variables**:

```text
JAVA_HOME
```

Set its value to the installed Java 21 directory, for example:

```text
C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot
```

Edit the user `Path` and ensure this entry is above older Java entries:

```text
%JAVA_HOME%\bin
```

Do not uninstall Java 22.

## 4.3 Verify Java and Maven

Close all old command prompts. Open a new Command Prompt:

```bat
java -version
```

Expected major version:

```text
21
```

Then run:

```bat
echo %JAVA_HOME%
mvn -version
```

`mvn -version` must report Java 21.

If Maven still reports Java 22, the old Java path appears before `%JAVA_HOME%\bin` in `Path`. Move `%JAVA_HOME%\bin` above the Oracle JDK path and open another terminal.

---

# 5. Configure Spring Tools / Eclipse

Open Spring Tools.

## 5.1 Add JDK 21

Navigate to:

```text
Window
→ Preferences
→ Java
→ Installed JREs
→ Add
→ Standard VM
```

For **JRE home**, select the Java 21 installation directory.

Example:

```text
C:\Program Files\Eclipse Adoptium\jdk-21...
```

Name it:

```text
Temurin JDK 21
```

Select its checkbox to make it the workspace default.

## 5.2 Configure compiler level

Navigate to:

```text
Window
→ Preferences
→ Java
→ Compiler
```

Set:

```text
Compiler compliance level = 21
```

The generated project will also declare Java 21 in `pom.xml`.

## 5.3 Configure the existing Maven installation

Navigate to:

```text
Window
→ Preferences
→ Maven
→ Installations
```

Add and select:

```text
D:\apache-maven-3.9.12-bin\apache-maven-3.9.12
```

Avoid using a different embedded Maven version when the command-line build uses Maven 3.9.12.

---

# 6. Install PostgreSQL 18

Use the official PostgreSQL Windows installer provided through EDB.

During installation select:

- PostgreSQL Server;
- pgAdmin;
- Command Line Tools.

StackBuilder is optional and may be skipped.

Recommended local values:

| Setting | Value |
|---|---|
| Version | PostgreSQL 18 stable |
| Port | `5432` |
| Superuser | `postgres` |
| Locale | Default |
| Data directory | Installer default |
| Password | A strong local password that is not committed |

Do not install PostgreSQL 19 beta for this assignment.

## 6.1 Add PostgreSQL tools to `Path`

If `psql` is not recognized, add:

```text
C:\Program Files\PostgreSQL\18\bin
```

to the user `Path`.

Open a new Command Prompt and verify:

```bat
psql --version
pg_isready -h localhost -p 5432
```

Expected readiness output includes:

```text
accepting connections
```

---

# 7. Create the Application Database

Open **SQL Shell (psql)** from the Start menu.

Connect using:

```text
Server: localhost
Database: postgres
Port: 5432
Username: postgres
Password: <the password selected during installation>
```

Run:

```sql
CREATE ROLE url_shortener_app
    WITH LOGIN
    PASSWORD 'replace-with-a-local-password';

CREATE DATABASE url_shortener
    OWNER url_shortener_app;
```

Do not use the `postgres` superuser from the Spring Boot application.

## 7.1 Verify the application account

Exit and reconnect, or run from Command Prompt:

```bat
psql -h localhost -p 5432 -U url_shortener_app -d url_shortener
```

Then execute:

```sql
SELECT current_database(), current_user, CURRENT_TIMESTAMP;
```

Expected:

```text
current_database = url_shortener
current_user     = url_shortener_app
```

Exit using:

```text
\q
```

---

# 8. Local Application Configuration

The application will read configuration from environment variables.

Planned variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/url_shortener
DB_USERNAME=url_shortener_app
DB_PASSWORD=<local password>
PUBLIC_BASE_URL=http://localhost:8080
```

Do not put the real database password in:

- Git;
- `application.yml`;
- Markdown documentation;
- screenshots;
- AI prompts.

## 8.1 Recommended Spring Tools configuration

After the project is created:

```text
Run
→ Run Configurations
→ Spring Boot App
→ Environment
```

Add:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
PUBLIC_BASE_URL
```

This keeps local credentials outside source control.

The repository may later include an `.env.example` or configuration example containing placeholders only.

---

# 9. Verify Git Identity

Run:

```bat
git config --global user.name
git config --global user.email
```

If blank, configure them:

```bat
git config --global user.name "Your Name"
git config --global user.email "your-public-github-email@example.com"
```

Use an email address appropriate for a public repository. A GitHub-provided no-reply email is acceptable.

Do not initialize Git until the Spring Boot project is generated and the initial repository structure is ready.

---

# 10. Final Verification Checklist

Run these commands from a new Command Prompt:

```bat
java -version
mvn -version
git --version
psql --version
pg_isready -h localhost -p 5432
```

Expected state:

| Check | Expected |
|---|---|
| `java -version` | Java 21 |
| `mvn -version` | Maven 3.9.12 using Java 21 |
| `git --version` | Git 2.51.0 or later installed version |
| `psql --version` | PostgreSQL 18 client |
| `pg_isready` | Accepting connections |
| STS Installed JRE | Temurin JDK 21 selected |
| PostgreSQL database | `url_shortener` |
| PostgreSQL app user | `url_shortener_app` |

---

# 11. Troubleshooting

## Maven still uses Java 22

Cause:

- Oracle JDK 22 appears before Java 21 in `Path`;
- old terminal inherited old environment values.

Fix:

1. move `%JAVA_HOME%\bin` above the Oracle JDK path;
2. close and reopen Command Prompt;
3. rerun `mvn -version`.

## Spring Tools still compiles with another JDK

Check:

```text
Window → Preferences → Java → Installed JREs
```

Later, also verify:

```text
Project → Properties → Java Build Path
Project → Properties → Java Compiler
```

## `psql` is not recognized

Add:

```text
C:\Program Files\PostgreSQL\18\bin
```

to `Path`, then open a new terminal.

## PostgreSQL connection refused

Check:

```bat
pg_isready -h localhost -p 5432
```

Then open:

```text
services.msc
```

Verify that the PostgreSQL service is running.

## Authentication failed

Confirm:

- correct username;
- correct password;
- correct port;
- database name is `url_shortener`;
- role was created with `LOGIN`.

---

# 12. Environment Definition of Done

Environment setup is complete only when:

- command-line Java is version 21;
- Maven reports Java 21;
- Spring Tools uses Java 21;
- PostgreSQL accepts connections;
- the application user can connect to `url_shortener`;
- local secrets are not stored in project files;
- Git identity is configured;
- all verification commands pass.
