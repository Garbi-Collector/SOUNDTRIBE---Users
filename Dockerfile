# Etapa 1: Construcción
FROM eclipse-temurin:17-jdk-alpine AS builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml y las dependencias para aprovechar el caché de Docker
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Asume que ya estás en el paso 4 (WORKDIR configurado y archivos copiados)
RUN chmod +x mvnw

# Descarga las dependencias sin construir la aplicación
RUN ./mvnw dependency:go-offline -B

# Copia el código fuente y construye la aplicación
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Ejecución (Distroless)
FROM gcr.io/distroless/java17:nonroot

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto de tu aplicación
EXPOSE 8080

# Ejecuta la aplicación
CMD ["app.jar"]
