FROM openjdk:21
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package
CMD ["java", "-jar", "target/*.jar"]