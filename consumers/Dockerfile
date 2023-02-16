#docker build -t consumers:1.0 .
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN ./mvnw dependency:copy-dependencies -DoutputDirectory=libs

COPY src src

RUN --mount=type=cache,target=/root/.m2,rw ./mvnw compile -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/libs libs
COPY --from=build /app/target/classes classes

ENTRYPOINT ["java", "-cp", "/app/classes:/app/libs/*", "ch.frankel.blog.consumers.StreamConsumersKt"]
