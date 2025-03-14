#!/bin/bash

# Run with ./setup.sh

set -e

echo "Stopping and removing all Docker containers..."
docker-compose down

echo "Starting Docker Compose..."
docker-compose up -d

echo "Building dapm-thesis..."
mvn clean install

echo "Building service-CY..."
mvn clean install -f service-CY/pom.xml

echo "Building data-stream-simulation..."
mvn clean install -f data-stream-simulation/pom.xml

echo "Command finished!"
