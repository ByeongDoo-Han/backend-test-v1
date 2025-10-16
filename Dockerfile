FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY modules/bootstrap/api-payment-gateway/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]