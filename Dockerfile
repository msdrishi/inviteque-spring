# ==========================================
# Stage 1: Build the Maven application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy the pom.xml first to download and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the JAR package
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Create the lightweight runtime image
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/invitique-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
