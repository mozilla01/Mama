# Use Ubuntu as base image
FROM ubuntu:latest

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive

# Update packages and install necessary tools
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the Maven project into the container
COPY . .

# Build the Maven project
RUN mvn compile && mvn exec:java -Dexec.mainClass=org.mamallc.Main