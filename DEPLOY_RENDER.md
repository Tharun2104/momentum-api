# Deploy Momentum API to Render

This backend is a Spring Boot API deployed to Render with PostgreSQL hosted on Supabase. The backend folder is `momentum-api`.

## 1. Create the PostgreSQL database

1. In Supabase, create or open your project.
2. Copy the database host, database name, username, and password.
3. Build the JDBC URL in this format:

```text
jdbc:postgresql://HOST:5432/DB_NAME
```

For your current Supabase project:

```text
jdbc:postgresql://db.dfpnhgogvweboiuradlu.supabase.co:5432/postgres
```

Spring Boot needs the JDBC form: `jdbc:postgresql://`, not `postgresql://`. If Supabase requires SSL for your connection mode, append `?sslmode=require` to the URL.

## 2. Create the Web Service

1. In Render, create a new Web Service.
2. Connect the GitHub repository that contains `momentum-api`.
3. Use these settings:

```text
Runtime: Java
Root Directory: leave blank if this repo is momentum-api; use momentum-api if deploying from the parent full-stack repo
Build Command: ./mvnw clean package -DskipTests
Start Command: java -jar target/*.jar
```

The Maven wrapper exists at `momentum-api/mvnw` and should remain executable.

## 3. Add environment variables

Add these environment variables to the Render Web Service:

```text
SPRING_PROFILES_ACTIVE=deployment
SPRING_DATASOURCE_URL=jdbc:postgresql://db.dfpnhgogvweboiuradlu.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD_HERE
```

Render sets `PORT` automatically. The app reads it with `server.port=${PORT:8080}`.

Local development uses the `local` profile by default and reads `application-local.properties`.

## 4. Verify deployment

After Render finishes deploying, open:

```text
https://YOUR_RENDER_SERVICE.onrender.com/
https://YOUR_RENDER_SERVICE.onrender.com/health
```

Expected responses:

```text
Momentum API is running
OK
```
