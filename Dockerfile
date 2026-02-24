# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Only copy POM first to cache dependencies, saving time on future builds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user for security best practices
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy over the built artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Explicitly expose port 8080
EXPOSE 8080

# Spring profiles active flag is passed via environment variables in docker-compose
ENTRYPOINT ["java", "-jar", "app.jar"]
