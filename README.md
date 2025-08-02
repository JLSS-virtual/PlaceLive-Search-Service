# PlaceLive-Api-Gateway

🚪 **Unified Entry Point & Intelligent Request Router**

## Overview

**PlaceLive-Api-Gateway** serves as the single, intelligent entry point for all PlaceLive client applications. This microservice handles unified routing, authentication, rate limiting, and load balancing while abstracting the complexity of the underlying microservices architecture. It ensures secure, scalable, and efficient communication between mobile apps, web clients, and the PlaceLive backend ecosystem.

## 🚀 Key Features

### Core Gateway Capabilities
- **Unified Entry Point**: Single endpoint for all client-to-backend communication
- **Intelligent Routing**: Dynamic request routing based on paths, headers, and conditions
- **Load Balancing**: Distribute traffic across multiple service instances
- **Service Discovery**: Automatic detection and routing to healthy service instances
- **Circuit Breaker**: Fault tolerance and resilience against service failures

### Security & Authentication
- **JWT Token Validation**: Centralized authentication and authorization
- **Rate Limiting**: Protect backend services from abuse and DDoS attacks
- **API Key Management**: Support for API keys and client authentication
- **CORS Handling**: Cross-origin request management for web clients
- **Request Sanitization**: Input validation and security filtering

### Performance & Monitoring
- **Request/Response Transformation**: Modify requests and responses as needed
- **Caching**: Edge caching for frequently requested data
- **Compression**: Automatic response compression to reduce bandwidth
- **Request Logging**: Comprehensive logging for monitoring and debugging
- **Metrics Collection**: Performance metrics and analytics

### Developer Experience
- **API Documentation**: Centralized Swagger/OpenAPI documentation
- **Request Tracing**: Distributed tracing across microservices
- **Error Handling**: Standardized error responses and error codes
- **Versioning Support**: API version management and backward compatibility

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Mobile Apps   │────│                  │────│  User Service   │
│   (Android/iOS) │    │                  │    │  (Port 8081)    │
└─────────────────┘    │                  │    └─────────────────┘
                       │                  │
┌─────────────────┐    │   API Gateway    │    ┌─────────────────┐
│   Web Client    │────│   (Port 8080)    │────│ Geofencing Svc  │
│   (React/Vue)   │    │                  │    │  (Port 8082)    │
└─────────────────┘    │                  │    └─────────────────┘
                       │                  │
┌─────────────────┐    │                  │    ┌─────────────────┐
│  Admin Panel    │────│                  │────│  Tracker Svc    │
│    (Web)        │    │                  │    │  (Port 8083)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Technology Stack
- **Framework**: Spring Cloud Gateway (WebFlux-based)
- **Service Discovery**: Netflix Eureka (optional)
- **Configuration**: Spring Cloud Config
- **Security**: Spring Security with JWT
- **Monitoring**: Spring Boot Actuator + Micrometer
- **Caching**: Redis for session and response caching
- **Load Balancing**: Spring Cloud LoadBalancer

## 🔧 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Redis 6.x (for caching and rate limiting)
- Service Registry (Eureka, Consul, or static configuration)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/JLSS-virtual/PlaceLive-Api-Gateway.git
   cd PlaceLive-Api-Gateway
   ```

2. **Configure the gateway**
   ```yaml
   # application.yml
   server:
     port: 8080
   
   spring:
     application:
       name: placelive-api-gateway
     cloud:
       gateway:
         routes:
           - id: user-service-route
             uri: http://localhost:8081
             predicates:
               - Path=/api/v1/users/**, /api/v1/auth/**
             filters:
               - name: CircuitBreaker
                 args:
                   name: user-service-cb
                   fallbackUri: forward:/fallback/user-service
   
   jwt:
     secret: your-jwt-secret
     expiration: 86400000
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

### Docker Deployment
```bash
docker build -t placelive-api-gateway .
docker run -p 8080:8080 placelive-api-gateway
```

## 🌐 Routing Configuration

### Route Definitions
```yaml
spring:
  cloud:
    gateway:
      routes:
        # User Service Routes
        - id: user-authentication
          uri: http://user-service:8081
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - name: RateLimiter
              args:
                rate-limiter: "#{@redisRateLimiter}"
                key-resolver: "#{@userKeyResolver}"
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20

        - id: user-management
          uri: http://user-service:8081
          predicates:
            - Path=/api/v1/users/**
          filters:
            - JwtAuthentication
            - name: Retry
              args:
                retries: 3
                methods: GET,POST,PUT
                exceptions: java.io.IOException

        # Geofencing Service Routes
        - id: geofencing-service
          uri: http://geofencing-service:8082
          predicates:
            - Path=/api/v1/geofences/**, /api/v1/places/**
          filters:
            - JwtAuthentication
            - name: CircuitBreaker
              args:
                name: geofencing-cb
                fallbackUri: forward:/fallback/geofencing

        # Tracker Service Routes
        - id: tracker-service
          uri: http://tracker-service:8083
          predicates:
            - Path=/api/v1/tracker/**
          filters:
            - JwtAuthentication
            - name: RequestRateLimiter
              args:
                rate-limiter: "#{@redisRateLimiter}"
                key-resolver: "#{@userKeyResolver}"
```

### Dynamic Routing with Service Discovery
```java
@Configuration
public class GatewayConfiguration {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/v1/users/**", "/api/v1/auth/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter)
                    .circuitBreaker(c -> c
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setMethods(HttpMethod.GET, HttpMethod.POST)))
                .uri("lb://user-service"))
            
            .route("geofencing-service", r -> r
                .path("/api/v1/geofences/**", "/api/v1/places/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter)
                    .requestRateLimiter(rl -> rl
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://geofencing-service"))
            
            .build();
    }
}
```

## 🔐 Security Implementation

### JWT Authentication Filter
```java
@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        if (isPublicPath(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        
        String token = extractToken(request);
        if (token == null || !jwtTokenUtil.validateToken(token)) {
            return handleUnauthorized(exchange);
        }
        
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", userId)
            .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }
}
```

### Rate Limiting Configuration
```java
@Configuration
public class RateLimitConfiguration {
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }
    
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            return Mono.just(clientIp);
        };
    }
}
```

## 🚀 Advanced Features

### Global Filters
```java
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put("requestId", requestId);
        
        logger.info("Request [{}] {} {} from {}",
            requestId,
            request.getMethod(),
            request.getPath(),
            request.getRemoteAddress());
        
        return chain.filter(exchange)
            .doFinally(signalType -> {
                ServerHttpResponse response = exchange.getResponse();
                logger.info("Response [{}] {} in {}ms",
                    requestId,
                    response.getStatusCode(),
                    calculateResponseTime(exchange));
            });
    }
    
    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
```

### Circuit Breaker Implementation
```java
@Configuration
public class CircuitBreakerConfiguration {
    
    @Bean
    public ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
        
        ReactiveResilience4JCircuitBreakerFactory factory = 
            new ReactiveResilience4JCircuitBreakerFactory();
        factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(circuitBreakerConfig)
            .build());
        
        return factory;
    }
}
```

### Fallback Handlers
```java
@RestController
public class FallbackController {
    
    @RequestMapping("/fallback/user-service")
    public Mono<ResponseEntity<Object>> userServiceFallback(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "User service is temporarily unavailable",
                "message", "Please try again later",
                "timestamp", Instant.now()
            )));
    }
    
    @RequestMapping("/fallback/geofencing")
    public Mono<ResponseEntity<Object>> geofencingServiceFallback(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Geofencing service is temporarily unavailable",
                "message", "Location features may be limited",
                "timestamp", Instant.now()
            )));
    }
    
    @RequestMapping("/fallback/tracker")
    public Mono<ResponseEntity<Object>> trackerServiceFallback(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Tracker service is temporarily unavailable",
                "message", "Friend tracking may not be available",
                "timestamp", Instant.now()
            )));
    }
}
```

## 📊 Monitoring & Observability

### Metrics Configuration
```java
@Configuration
public class MetricsConfiguration {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("service", "api-gateway");
    }
}
```

### Health Checks
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    circuitbreakers:
      enabled: true
    ratelimiters:
      enabled: true
```

### Custom Health Indicators
```java
@Component
public class DownstreamServicesHealthIndicator implements HealthIndicator {
    
    @Autowired
    private WebClient webClient;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            checkServiceHealth("user-service", "http://user-service:8081/actuator/health", builder);
            checkServiceHealth("geofencing-service", "http://geofencing-service:8082/actuator/health", builder);
            checkServiceHealth("tracker-service", "http://tracker-service:8083/actuator/health", builder);
            
            return builder.up().build();
        } catch (Exception e) {
            return builder.down(e).build();
        }
    }
    
    private void checkServiceHealth(String serviceName, String healthUrl, Health.Builder builder) {
        try {
            String response = webClient.get()
                .uri(healthUrl)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            builder.withDetail(serviceName, "UP");
        } catch (Exception e) {
            builder.withDetail(serviceName, "DOWN - " + e.getMessage());
        }
    }
}
```

## 🛡️ Security Best Practices

### CORS Configuration
```java
@Configuration
public class CorsConfiguration {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
```

### Request Size Limits
```yaml
spring:
  webflux:
    multipart:
      max-in-memory-size: 10MB
  cloud:
    gateway:
      httpclient:
        max-header-size: 16KB
        max-initial-line-length: 4KB
```

### API Versioning Support
```java
@Component
public class ApiVersioningFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String version = extractVersionFromRequest(request);
        
        if (version != null) {
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-API-Version", version)
                .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }
        
        return chain.filter(exchange);
    }
    
    private String extractVersionFromRequest(ServerHttpRequest request) {
        // Extract from header: X-API-Version
        String headerVersion = request.getHeaders().getFirst("X-API-Version");
        if (headerVersion != null) return headerVersion;
        
        // Extract from path: /api/v1/users
        String path = request.getPath().toString();
        Pattern pattern = Pattern.compile("/api/v(\\d+)/");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "1"; // Default version
    }
}
```

## 📈 Performance Optimization

### Response Caching
```java
@Configuration
public class CachingConfiguration {
    
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        Jackson2JsonRedisSerializer<Object> serializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        
        RedisSerializationContext<String, Object> context = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(RedisSerializer.string())
                .value(serializer)
                .build();
        
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
```

### Request Compression
```yaml
server:
  compression:
    enabled: true
    mime-types: 
      - application/json
      - application/xml
      - text/html
      - text/xml
      - text/plain
    min-response-size: 1024
```

## 🔄 Load Balancing Strategies

### Custom Load Balancer
```java
@Configuration
public class LoadBalancerConfiguration {
    
    @Bean
    @LoadBalancerClient(name = "user-service", configuration = UserServiceLoadBalancerConfiguration.class)
    public ServiceInstanceListSupplier userServiceInstanceSupplier() {
        return ServiceInstanceListSupplier.builder()
            .withDiscoveryClient()
            .withHealthChecks()
            .build();
    }
}

@Configuration
class UserServiceLoadBalancerConfiguration {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RoundRobinLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name);
    }
}
```

## 🛠️ Development & Testing

### Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=test-route",
    "spring.cloud.gateway.routes[0].uri=http://httpbin.org",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**"
})
class ApiGatewayIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldRouteToDownstreamService() {
        webTestClient.get()
            .uri("/test/get")
            .exchange()
            .expectStatus().isOk();
    }
    
    @Test
    void shouldApplyRateLimiting() {
        // Test rate limiting functionality
        for (int i = 0; i < 25; i++) {
            WebTestClient.ResponseSpec response = webTestClient.get()
                .uri("/api/v1/users/profile")
                .header("Authorization", "Bearer valid-token")
                .exchange();
            
            if (i < 20) {
                response.expectStatus().isOk();
            } else {
                response.expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            }
        }
    }
}
```

### Configuration Management
```yaml
# application-dev.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-dev
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/users/**
          filters:
            - name: RequestRateLimiter
              args:
                rate-limiter: "#{@redisRateLimiter}"
                key-resolver: "#{@userKeyResolver}"
                redis-rate-limiter.replenishRate: 100  # Higher limits for dev
                redis-rate-limiter.burstCapacity: 200

# application-prod.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-prod
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - name: RequestRateLimiter
              args:
                rate-limiter: "#{@redisRateLimiter}"
                key-resolver: "#{@userKeyResolver}"
                redis-rate-limiter.replenishRate: 10   # Production limits
                redis-rate-limiter.burstCapacity: 20
```

## 🤝 Contributing

We welcome contributions to PlaceLive-Api-Gateway! Here's how to contribute:

### Development Guidelines
1. **Performance**: All changes should maintain or improve response times
2. **Security**: Security-related changes require thorough review
3. **Documentation**: Update configuration documentation for any new features
4. **Testing**: Include both unit and integration tests
5. **Backward Compatibility**: Ensure existing clients continue to work

### Pull Request Process
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/enhanced-rate-limiting`
3. Implement changes with comprehensive tests
4. Update documentation and configuration examples
5. Submit pull request with performance benchmarks

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Wiki](https://github.com/JLSS-virtual/PlaceLive-Api-Gateway/wiki)
- **Issues**: [GitHub Issues](https://github.com/JLSS-virtual/PlaceLive-Api-Gateway/issues)
- **Performance Issues**: performance@placelive.com
- **General Support**: jlss.virtual.0808@gmail.com

---

**PlaceLive-Api-Gateway**: The intelligent, secure, and scalable entry point powering PlaceLive's microservices ecosystem. 🚪⚡
