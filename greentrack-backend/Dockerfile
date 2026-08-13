FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/greentrack-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=55.0 -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=128m -Dserver.port=${PORT:-8080} -jar app.jar"]
