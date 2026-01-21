FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copiar código fuente
COPY src src

# Construir la aplicación
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR construido
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer puerto
EXPOSE 8081

# Variables de entorno por defecto
ENV DB_HOST=postgres-inventory
ENV DB_PORT=5432
ENV DB_NAME=inventory_db
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV RABBITMQ_HOST=rabbitmq
ENV RABBITMQ_PORT=5672
ENV RABBITMQ_USER=guest
ENV RABBITMQ_PASSWORD=guest

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
