# 1) Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Gradle 캐시 최적화 (gradle wrapper 포함)
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 소스 코드 복사
COPY . .

# Spring Boot JAR 빌드
RUN ./gradlew clean bootJar --no-daemon

# 2) Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]