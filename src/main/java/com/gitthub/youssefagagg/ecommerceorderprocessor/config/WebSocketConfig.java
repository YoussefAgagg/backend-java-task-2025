package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import com.gitthub.youssefagagg.ecommerceorderprocessor.security.websocket.WebSocketAuthenticationInterceptor;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.websocket.WebSocketSecurityInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration for WebSocket support.
 */
@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketSecurityInterceptor webSocketSecurityInterceptor;
  private final WebSocketAuthenticationInterceptor webSocketAuthenticationInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable a simple memory-based message broker to send messages to clients
    // on destinations prefixed with /topic
    config.enableSimpleBroker("/topic");

    // Set prefix for messages from clients to application
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Register the "/ws" endpoint, enabling SockJS fallback options
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .addInterceptors(webSocketAuthenticationInterceptor);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // Add security interceptor to the client inbound channel
    registration.interceptors(webSocketSecurityInterceptor);
  }

}
