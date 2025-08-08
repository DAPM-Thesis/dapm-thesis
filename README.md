# security-service 
The security service ensures trusted communication, authentication, and role-based access control across decentralized organizational environments.
 
## 🛠️ Tech Stack
- Backend: SpringBoot
- Deployment: Docker containers
- Database: Postgress

## Prerequisetes
- Java21
- Postgres
- Docker compose and docker
- git

## 📦 Installation
To run the project locally:
- git clone --branch dapm-security-service https://github.com/DAPM-Thesis/dapm-thesis.git 
- cd security-service
- cd security-service
- mvn clean install
- mvn clean package
- docker compose down --volumes --remove-orphans
- docker compose build --no-cache  
- docker compose up

## Testing Apis
To access endpoints:
- OrgA: http://localhost:8081/swagger-ui/index.html#
- OrgB: http://localhost:8082/swagger-ui/index.html#
