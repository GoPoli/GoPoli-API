FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd --system gopoli && useradd --system --gid gopoli --no-create-home gopoli

COPY --from=build /workspace/target/*.jar /app/gopoli-api.jar

USER gopoli

ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/gopoli-api.jar"]
