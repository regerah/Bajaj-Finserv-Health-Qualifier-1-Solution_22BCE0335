# Bajaj Finserv Spring Boot Project

A Spring Boot application that automatically generates a webhook, constructs an SQL query, and submits it to the webhook endpoint on startup.

<img width="1919" height="996" alt="image" src="https://github.com/user-attachments/assets/25ab7c3e-21c1-40fc-a983-c1397cd19adc" />

## Project Structure

```
bajaj-finserv-project/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── bajaj/
        │               ├── BajajApp.java
        │               └── service/
        │                   └── StartupService.java
        └── resources/
            └── application.properties
```

## Features

- **Automatic Startup Execution**: Uses `CommandLineRunner` to execute logic on application startup
- **Webhook Generation**: Sends POST request to generate webhook and access token
- **SQL Query Submission**: Constructs and submits the required SQL query to the webhook
- **RestTemplate Integration**: Uses Spring's RestTemplate for HTTP requests
- **Comprehensive Logging**: Detailed logging for monitoring execution flow

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for API calls)

## Build Instructions

### 1. Clone/Download the Project
```bash
# If using Git
git clone <repository-url>
cd bajaj-finserv-project

# Or extract the project files to a directory
```

### 2. Build the Project
```bash
# Clean and compile
mvn clean compile

# Create JAR file
mvn clean package

# Skip tests if needed
mvn clean package -DskipTests
```

The JAR file will be created at: `target/bajaj-finserv-project-1.0.0.jar`

## Run Instructions

### Method 1: Using Maven
```bash
mvn spring-boot:run
```

### Method 2: Using JAR file
```bash
# After building the project
java -jar target/bajaj-finserv-project-1.0.0.jar
```

### Method 3: Using Maven exec plugin
```bash
mvn exec:java -Dexec.mainClass="com.example.bajaj.BajajApp"
```

## Application Flow

1. **Startup**: Application starts and `StartupService` implements `CommandLineRunner`
2. **Webhook Generation**: 
   - POST request to `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
   - Request body: `{"name": "John Doe", "regNo": "REG12347", "email": "john@example.com"}`
   - Captures `webhook` and `accessToken` from response
3. **SQL Query Submission**:
   - Constructs the required SQL query
   - POST request to the webhook URL with Bearer token authentication
   - Request body: `{"finalQuery": "SQL_QUERY_STRING"}`
4. **Completion**: Application logs results and exits

## SQL Query

The application constructs and submits this SQL query:

```sql
SELECT p.AMOUNT AS SALARY,
       CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
       TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,
       d.DEPARTMENT_NAME
FROM PAYMENTS p
JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
WHERE DAY(p.PAYMENT_TIME) <> 1
ORDER BY p.AMOUNT DESC
LIMIT 1;
```

## Configuration

The application is configured via `src/main/resources/application.properties`:

- Logging levels for debugging
- Application runs as a non-web application
- Clean console output with disabled Spring Boot banner

## Troubleshooting

### Common Issues

1. **Java Version**: Ensure Java 17+ is installed
   ```bash
   java -version
   ```

2. **Maven Issues**: Verify Maven installation
   ```bash
   mvn -version
   ```

3. **Network Issues**: Check internet connectivity for API calls

4. **Build Failures**: Clean and rebuild
   ```bash
   mvn clean install -U
   ```

### Logs

The application provides detailed logging. Check console output for:
- Webhook generation status
- Access token receipt (partially masked)
- SQL query submission results
- Any error messages

## Dependencies

- Spring Boot Starter 3.1.5
- Spring Boot Starter Web (for RestTemplate)
- Jackson Databind (for JSON processing)
- SLF4J Logging

## Author

Created for Bajaj Finserv hiring challenge.

## License

This project is created for assessment purposes.
