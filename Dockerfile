FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /build
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S tecla && adduser -S -G tecla tecla
WORKDIR /app

COPY --from=build /build/target/the-legend-of-tecla.jar /app/game.jar
COPY --chown=tecla:tecla data /app/data

USER tecla
ENV LANG=C.UTF-8

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/app/game.jar"]
CMD ["--rapido"]
