# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper to the container
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Ensure mvnw has executable permission
RUN chmod +x mvnw
COPY src ./src
# Copy the local dependency JAR to the correct location in the Docker image
COPY src/main/resources/jcl-core-2.9.jar /app/src/main/resources/jcl-core-2.9.jar
# Run the Maven build command to create the JAR file
RUN ./mvnw clean package -DskipTests

# Copy the JAR file to the container
COPY target/back-end-0.0.1-SNAPSHOT.jar app.jar
# Run the JAR file
# Expose port 8080
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
