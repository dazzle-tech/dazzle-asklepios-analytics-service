FROM gradle:9-jdk21 AS build

WORKDIR /app

COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

RUN gradle build --no-daemon || true

COPY . .

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /application

RUN apt-get update && apt-get install -y \
    libnss3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libasound2t64 \
    libpango-1.0-0 \
    libcairo2 \
    libatspi2.0-0 \
    libxshmfence1 \
 && rm -rf /var/lib/apt/lists/*
RUN gradle playwrightInstall --no-daemon

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]