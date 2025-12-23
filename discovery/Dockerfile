# --- GIAI ĐOẠN 1: BUILD ---
FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- GIAI ĐOẠN 2: RUN ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
# Cài thêm curl để làm Healthcheck (Docker Compose sẽ dùng cái này)
RUN apk add --no-cache curl

COPY --from=builder /app/target/*.jar discovery-service.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "discovery-service.jar"]