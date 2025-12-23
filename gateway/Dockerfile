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

# Cài curl để phục vụ Healthcheck của Docker Compose
RUN apk add --no-cache curl

# Copy file jar từ giai đoạn build
COPY --from=builder /app/target/*.jar gateway-service.jar

EXPOSE 10003

ENTRYPOINT ["java", "-jar", "gateway-service.jar"]