package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt.TokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
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
  private final TokenProvider tokenProvider;

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
            .setAllowedOriginPatterns("*");
  }

  //
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info(message.getPayload().toString());
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                                                                         StompHeaderAccessor.class);

        log.info(accessor.toString());
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
          String authHeader = accessor.getFirstNativeHeader("Authorization");
          if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenProvider.validateToken(token)) {
              Authentication authentication = tokenProvider.getAuthentication(token);
              accessor.setUser(authentication);
            }
          }
        }

        return message;
      }
    });
  }
}
