# ---- Build stage ----
# Compile and package the application into a runnable jar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Resolve dependencies before copying sources so the layer stays cached
# across rebuilds when only source files change.
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Runtime stage ----
# A slim JRE image keeps the deployed size small. Explicit heap flags keep
# the JVM comfortably inside Render's 512 MB free-tier memory limit.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/scheduler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-jar", "app.jar"]
