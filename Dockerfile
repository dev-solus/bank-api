FROM maven:3.6.3 AS build-env
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:resolve --no-transfer-progress

COPY . .

# RUN ls -al
RUN mvn clean package -Dmaven.test.skip=true -Dspring.profiles.active=prod --file pom.xml


FROM openjdk:22-ea-jdk-slim
WORKDIR /app

COPY --from=build-env /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]