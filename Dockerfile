# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x ./gradlew

# Download dependencies first (will be cached if no changes)
RUN ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add metadata labels
LABEL org.opencontainers.image.source="https://github.com/youssefagagg/ecommerce-order-processor"
LABEL org.opencontainers.image.description="E-commerce Order Processor Application"
LABEL org.opencontainers.image.licenses="MIT"

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar file
COPY --from=build /app/build/libs/*.jar app.jar

# Set permissions for the non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Configure health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/management/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
