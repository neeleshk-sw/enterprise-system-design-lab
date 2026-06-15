# syntax=docker/dockerfile:1

# --- Build stage: compile & package from source (multi-module reactor) ---
# Build context is the 00-template-service root so common-library is available.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY common-library common-library
COPY customer-service customer-service

RUN mvn -B -pl customer-service -am -DskipTests clean package

# --- Runtime stage: slim JRE, non-root ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as an unprivileged user
RUN addgroup -S app && adduser -S -G app app

COPY --from=build /workspace/customer-service/target/customer-service-*.jar /app/app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS=""

# Uses busybox wget (present in alpine); healthy only once the DB-backed app reports UP.
HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
