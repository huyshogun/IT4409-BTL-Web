# --- GIAI ĐOẠN 1: BUILD (Dùng Maven) ---
FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Copy file cấu hình Maven trước (để tận dụng Docker Cache)
COPY pom.xml .

# 2. Tải các thư viện về (Bước này tốn thời gian nhất, nên làm riêng để cache)
RUN mvn dependency:go-offline

# 3. Copy toàn bộ code nguồn vào
COPY src ./src

# 4. Thực hiện lệnh Build (Tạo ra file .jar trong /app/target)
RUN mvn clean package -DskipTests


# --- GIAI ĐOẠN 2: RUN (Dùng JRE siêu nhẹ) ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy file .jar TỪ GIAI ĐOẠN 1 (builder) sang giai đoạn này
COPY --from=builder /app/target/*.jar account-service.jar

EXPOSE 10001

ENTRYPOINT ["java", "-jar", "account-service.jar"]