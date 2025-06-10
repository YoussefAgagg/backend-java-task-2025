# E-commerce Order Processor

![Build and Push Docker Image](https://github.com/YoussefAgagg/backend-java-task-2025/actions/workflows/build-and-push.yml/badge.svg)

A Spring Boot application for processing e-commerce orders.

## Features

- Order processing and management
- User authentication and authorization
- RESTful API with rate limiting
- WebSocket support for real-time updates

## Technologies

- Java 21
- Spring Boot 3.5.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- Liquibase
- Bucket4j (for rate limiting)
- Caffeine (for caching)
- Docker

## Getting Started

### Prerequisites

- JDK 21
- Docker and Docker Compose

### Running Locally

```bash
./gradlew bootRun
```

### Running with Docker

```bash
cd docker
```

```bash
docker-compose up
```

## Rate Limiting

The API implements rate limiting to protect against abuse and ensure fair usage. The rate limiting is based on the
client's IP address and varies by endpoint:

- Default rate limit: 20 requests per minute
- Authentication endpoints:
    - `/api/v1/auth/login`: 5 requests per minute
    - `/api/v1/auth/register`: 3 requests per minute
- Admin endpoints: 10 requests per minute
- Public product endpoints: 30 requests per minute

When a rate limit is exceeded, the API returns a 429 Too Many Requests response with information about when to retry.

### Rate Limit Headers

The API includes the following headers in responses:

- `X-Rate-Limit-Remaining`: Number of requests remaining in the current time window
- `X-Rate-Limit-Retry-After-Seconds`: Seconds until the rate limit resets

### Configuring Rate Limits

Rate limits can be configured in the `application.yaml` file:

```yaml
rate-limit:
  capacity: 20              # Default maximum requests
  refill-tokens: 20         # Default tokens refilled per period
  refill-duration: 60       # Default period in seconds

  endpoints:
    "/api/v1/auth/login": # Endpoint-specific configuration
      capacity: 5
      refill-tokens: 5
      refill-duration: 60
```

## CI/CD

This project uses GitHub Actions for continuous integration and delivery:

- Automated builds on push to main branch and pull requests
- Unit tests and code coverage reports
- Docker image building and publishing to GitHub Container Registry
