# ARCHITEKTURA: Wieloetapowy Dockerfile do budowania i uruchamiania mikroserwisów.
# Etap 'builder': Poprawnie kopiuje strukturę wielomodułową i kompiluje cały projekt.
# Etap finalny: Kopiuje tylko wynikowy .jar dla konkretnego serwisu.
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY danxils-commons/pom.xml ./danxils-commons/
COPY iam-service/pom.xml ./iam-service/
COPY tracking-service/pom.xml ./tracking-service/
COPY order-service/pom.xml ./order-service/
COPY planning-service/pom.xml ./planning-service/
COPY analytics-service/pom.xml ./analytics-service/
COPY dashboard-service/pom.xml ./dashboard-service/
COPY admin-panel-service/pom.xml ./admin-panel-service/
COPY driver-app-service/pom.xml ./driver-app-service/
COPY audit-service/pom.xml ./audit-service/
COPY address-verification-service/pom.xml ./address-verification-service/


RUN mvn -B dependency:go-offline

COPY . .

RUN mvn -B install -DskipTests

FROM eclipse-temurin:17-jre-jammy
ENV TZ=Europe/Warsaw
WORKDIR /app

ARG SERVICE_MODULE
COPY --from=builder /app/${SERVICE_MODULE}/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]