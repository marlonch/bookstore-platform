FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY bookstore-api/pom.xml bookstore-api/pom.xml
COPY bookstore-order-service/pom.xml bookstore-order-service/pom.xml
RUN mvn dependency:go-offline -pl bookstore-api -am -q

COPY bookstore-api/src bookstore-api/src
RUN mvn clean package -pl bookstore-api -am -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/bookstore-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
