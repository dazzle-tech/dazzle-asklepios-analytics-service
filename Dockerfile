FROM gradle:9-jdk21 AS build

WORKDIR /app

COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

RUN gradle build --no-daemon || true

COPY . .

RUN gradle clean bootJar --no-daemon


FROM mcr.microsoft.com/playwright/java:v1.45.0-jammy
WORKDIR /application

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]