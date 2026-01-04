# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Gradle wrapper와 의존성 파일만 먼저 복사 (캐싱 최적화)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 의존성 다운로드 (이 레이어는 의존성이 변경될 때만 재빌드)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle build -x test --no-daemon

# Production stage - Eclipse Temurin JRE 사용
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안을 위한 비root 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]