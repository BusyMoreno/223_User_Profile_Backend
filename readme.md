# README #

## Starter Project Spring Boot

## Prerequisites

Before you begin, ensure you have the following installed:
- **Java 17 or higher** - [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop/)
- **IDE** (IntelliJ IDEA recommended) - [Download here](https://www.jetbrains.com/idea/download/)
- **Git** - [Download here](https://git-scm.com/downloads)

## Setup Instructions (Step-by-Step)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd spring_backend
```

### 2. Start the PostgreSQL Database with Docker

First, make sure Docker Desktop is running, then execute:

```bash
docker run --name postgres_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
```

**What this command does:**
- `--name postgres_db`: Names the container "postgres_db"
- `-e POSTGRES_USER=postgres`: Sets username to "postgres"
- `-e POSTGRES_PASSWORD=postgres`: Sets password to "postgres"
- `-p 5432:5432`: Maps port 5432 (database port)
- `-d postgres`: Runs PostgreSQL in the background

**Verify the database is running:**
```bash
docker ps
```
You should see `postgres_db` in the list of running containers.

### 3. Configure the Application

Check that your `application.properties` or `application.yml` file has the correct database connection settings:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 4. Run the Application

**Option A: Using IntelliJ IDEA**
1. Open the project in IntelliJ IDEA
2. Wait for dependencies to download
3. Find the main application class (usually `*Application.java`)
4. Right-click and select "Run"

**Option B: Using Command Line**
```bash
./gradlew bootRun
```
or
```bash
./mvnw spring-boot:run
```

The application should start on `http://localhost:8080`

### 5. Access Swagger UI

Once the application is running, open your browser and navigate to:
```
http://localhost:8080/swagger-ui/index.html
```

Swagger UI provides an interactive interface to test all API endpoints.

## Adding Testing Data with data.sql

### Creating a data.sql Script

You can automatically populate your database with test data when the application starts by creating a `data.sql` file.

**Step 1: Create the file**

Create a file named `data.sql` in the `src/main/resources` directory:
```
src/main/resources/data.sql
```

**Step 2: Add test data**

Example `data.sql` content:
```sql
-- Insert test users
INSERT INTO users (id, username, email, password) VALUES
(1, 'testuser1', 'test1@example.com', '$2a$10$encoded_password_here'),
(2, 'testuser2', 'test2@example.com', '$2a$10$encoded_password_here');

-- Insert test roles
INSERT INTO roles (id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

-- Insert test data for your entities
-- Add more INSERT statements as needed for your project
```

**Step 3: Configure Spring to use data.sql**

Add these properties to your `application.properties`:
```properties
# Enable data.sql execution
spring.sql.init.mode=always
# Set when to run data.sql (after or before Hibernate)
spring.jpa.defer-datasource-initialization=true
```

**Important Notes:**
- `spring.sql.init.mode=always`: Runs data.sql every time the app starts
- `spring.jpa.defer-datasource-initialization=true`: Ensures tables are created before inserting data
- For password hashing, use BCrypt encoded passwords
- Be careful with ID values if using auto-increment
- Consider using `ON CONFLICT DO NOTHING` for PostgreSQL to avoid duplicate key errors:
  ```sql
  INSERT INTO users (id, username, email) VALUES (1, 'test', 'test@example.com')
  ON CONFLICT (id) DO NOTHING;
  ```

### Alternative: Using import.sql

If you're using Hibernate, you can also create an `import.sql` file in `src/main/resources/`. This file is automatically executed by Hibernate without additional configuration.

## Useful Docker Commands

**Stop the database:**
```bash
docker stop postgres_db
```

**Start the database (after stopping):**
```bash
docker start postgres_db
```

**Remove the database container:**
```bash
docker rm -f postgres_db
```

**View database logs:**
```bash
docker logs postgres_db
```

**Access PostgreSQL CLI:**
```bash
docker exec -it postgres_db psql -U postgres
```

## Troubleshooting

### Issue: "relation does not exist" error
```
org.postgresql.util.PSQLException: ERROR: relation "role_authority" does not exist
```
**Solution:** Simply restart the application. Hibernate sometimes does not initialize the tables fast enough and causes this error.

### Issue: Port 5432 already in use
**Solution:** Another PostgreSQL instance is running. Either stop it or use a different port:
```bash
docker run --name postgres_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5433:5432 -d postgres
```
Then update your `application.properties` to use port 5433.

### Issue: Docker command not found
**Solution:** Make sure Docker Desktop is installed and running.

### Issue: Application won't start
**Checklist:**
1. Is Docker running? Check with `docker ps`
2. Is the database container running?
3. Are the database credentials correct in `application.properties`?
4. Check application logs for specific errors

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)

## Getting Help

If you encounter issues:
1. Check the Troubleshooting section above
2. Review application logs in the console
3. Check Docker logs: `docker logs postgres_db`
4. Ask your instructor or teaching assistant