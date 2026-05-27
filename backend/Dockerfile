FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/target/customer-service-backend-*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=15s --timeout=5s --retries=8 --start-period=90s \
  CMD curl -fsS http://localhost:8082/api/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
