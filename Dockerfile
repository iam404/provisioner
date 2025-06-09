# Start from an OpenJDK image suitable for Spring Boot
FROM maven:3.9-eclipse-temurin-17 as build

# Set the working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY . .
#COPY src ./src

# Package the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Second stage: minimal runtime image
FROM amazoncorretto:17.0.10
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (adjust if your app uses a different port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
