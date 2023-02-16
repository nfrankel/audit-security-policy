#docker build -t secure-boot:1.2 .
FROM eclipse-temurin:21-jdk AS build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2,rw ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre

COPY --from=build target/secure-boot-1.2.jar secure-boot.jar

ENTRYPOINT ["java", "-jar", "secure-boot.jar"]
