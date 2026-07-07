# --- Stage 1: Extract layers from the fat JAR ---
FROM eclipse-temurin:21-jdk AS extractor
WORKDIR /app
COPY target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# --- Stage 2: Create the highly cached runtime image ---
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 1. Copy heavy dependencies (Changes rarely - Highly Cached)
COPY --from=extractor /app/dependencies/ ./
# 2. Copy Spring Boot loader (Changes rarely - Highly Cached)
COPY --from=extractor /app/spring-boot-loader/ ./
# 3. Copy internal library snapshots (Changes sometimes)
COPY --from=extractor /app/snapshot-dependencies/ ./
# 4. Copy ONLY your application code (Changes frequently - Fast build!)
COPY --from=extractor /app/application/ ./

EXPOSE 8080

# Run using the extracted launcher instead of standard -jar
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
