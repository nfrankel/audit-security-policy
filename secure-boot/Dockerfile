#docker build -t secure-boot:1.0 .
FROM eclipse-temurin:21-jdk AS build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2,rw ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre

COPY --from=build target/secure-boot-1.0.jar secure-boot.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "secure-boot.jar"]