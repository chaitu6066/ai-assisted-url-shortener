# syntax=docker/dockerfile:1

FROM maven:3.9.16-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S app \
    && adduser -S app -G app

COPY --from=build --chown=app:app \
    /workspace/target/url-shortener-*.jar \
    /app/app.jar

USER app

EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
