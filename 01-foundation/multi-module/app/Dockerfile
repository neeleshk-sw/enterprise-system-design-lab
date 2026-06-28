# syntax=docker/dockerfile:1

# --- Build stage: compile & package the reactor from source ---
# Build context is the multi-module root so every module is available.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY common-library common-library
COPY domain domain
COPY persistence persistence
COPY api api
COPY app app

RUN mvn -B -ntp -pl app -am -DskipTests clean package

# --- Runtime stage: slim JRE, non-root ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app

COPY --from=build /workspace/app/target/app-*.jar /app/app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS=""

HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
