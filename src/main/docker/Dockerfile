####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the container image run:
#
# ./mvnw package
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.jvm -t quarkus/code-with-quarkus-jvm .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/code-with-quarkus-jvm
#
# If you want to include the debug port into your docker image
# you will have to expose the debug port (default 5005 being the default) like this :  EXPOSE 8080 5005.
# Additionally you will have to set -e JAVA_DEBUG=true and -e JAVA_DEBUG_PORT=*:5005
# when running the container
#
# Then run the container using :
#
# docker run -i --rm -p 8080:8080 quarkus/code-with-quarkus-jvm
###
# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file into the container
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set the default command to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]

