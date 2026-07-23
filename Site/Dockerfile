# Multi-stage build for the Spring Boot backend (Site/).
#
# Build:   docker build -t tpf-server -f Site/Dockerfile Site
# Run:     docker run -p 8080:8080 --env-file .env tpf-server
#
# All secrets (DB credentials, encryption key, API keys, SendGrid key, ...)
# must be supplied at runtime via environment variables / --env-file, never
# baked into the image. See application.properties.example for the full list
# of keys and Spring Boot's standard ENV_VAR override convention
# (e.g. SPRING_DATASOURCE_PASSWORD, ENCRYPTION_SECRET_KEY, APP_API_KEY, ...).

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependencies separately from source for faster rebuilds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Run as a non-root user.
RUN useradd --system --create-home --shell /usr/sbin/nologin tpf
USER tpf

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

# Spring Boot honours SPRING_PROFILES_ACTIVE / any property as an env var
# override (relaxed binding), so application.properties.example's keys can
# all be set here without a custom entrypoint script.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
