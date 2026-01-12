FROM eclipse-temurin:17-jre-focal
ENV TZ=Europe/Warsaw
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
