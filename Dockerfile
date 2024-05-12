FROM maven:3.9.6-eclipse-temurin-22-alpine AS build-env
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline dependency:resolve-plugins --quiet

# RUN mvn dependency:resolve --no-transfer-progress

COPY . .

# RUN ls -al

RUN mvn -v

RUN mvn clean package -Dmaven.test.skip=true -Dspring.profiles.active=prod --file pom.xml --quiet


FROM openjdk:22-ea-jdk-slim
WORKDIR /app

COPY --from=build-env /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]