FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
RUN chmod +x ./gradlew
COPY src src

RUN ./gradlew build -x test
RUN mkdir -p build/dependency
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/dependency

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency

# Add metadata labels
LABEL org.opencontainers.image.source="https://github.com/youssefagagg/ecommerce-order-processor"
LABEL org.opencontainers.image.description="E-commerce Order Processor Application"
LABEL org.opencontainers.image.licenses="MIT"

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Configure health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/management/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java","-cp","app:app/lib/*","com.gitthub.youssefagagg.ecommerceorderprocessor.EcommerceOrderProcessorApplication"]
