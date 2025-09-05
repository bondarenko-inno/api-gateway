FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

RUN ./gradlew build --dry-run

COPY src ./src

RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

USER spring:spring

WORKDIR /app

COPY --from=builder /app/build/libs/api-gateway-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java", "-jar", "api-gateway-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080
