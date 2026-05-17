FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --create-home --shell /usr/sbin/nologin appuser

COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser

EXPOSE 19200

ENTRYPOINT ["java", "-jar", "/app/app.jar"]