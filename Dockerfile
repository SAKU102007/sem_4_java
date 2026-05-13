FROM node:20-alpine AS client-build

WORKDIR /workspace/client

COPY client/package*.json ./
RUN npm ci

COPY client/ ./
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

FROM eclipse-temurin:17-jdk-jammy AS app-build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY config config
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
COPY --from=client-build /workspace/client/dist src/main/resources/static
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app

COPY --chown=spring:spring --from=app-build /workspace/target/*.jar app.jar

ENV PORT=10000
ENV LOG_DIR=/app/logs

EXPOSE 10000

USER spring

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fsS "http://localhost:${PORT}/actuator/health" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
