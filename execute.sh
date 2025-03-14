#!/bin/bash

# Run with ./execute.sh
# Currently processes are not killed after closing command window
# netstat -ano | findstr :8080
# taskkill /PID {insert PID} /F

echo "Running data-stream-simulation..."
mvn exec:java -f data-stream-simulation/pom.xml -Dexec.mainClass="client.StreamClientApplication" &

echo "Running service-CY..."
mvn exec:java -f service-CY/pom.xml -Dexec.mainClass="start.Main" &

wait

echo "Programs closed!"
