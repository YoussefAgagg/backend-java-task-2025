package com.gitthub.youssefagagg.ecommerceorderprocessor.security.websocket;

import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * WebSocket security interceptor to enforce security rules for WebSocket connections. - Topics with
 * "admin" in the path require admin role - Topics with user IDs only accept connections from the
 * same user - Prevents subscription to unrelated topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

  // Valid topic patterns
  private static final Pattern USER_TOPIC_PATTERN = Pattern.compile(
      "/topic/(?:orders|notifications)/([a-zA-Z0-9_-]+)");
  private static final Pattern ADMIN_TOPIC_PATTERN = Pattern.compile("/topic/admin/.*");
  private static final Pattern INVENTORY_TOPIC_PATTERN = Pattern.compile("/topic/inventory");


  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                                                                     StompHeaderAccessor.class);

    if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      // Try to get the security context from the session attributes
      if (accessor.getSessionAttributes() != null &&
          accessor.getSessionAttributes().containsKey("SPRING_SECURITY_CONTEXT")) {
        // Restore the security context from the session attributes
        SecurityContextHolder.setContext(
            (org.springframework.security.core.context.SecurityContext)
                accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT"));
      }

      String destination = accessor.getDestination();
      String username = SecurityUtils.getCurrentUserUserName().orElse(null);

      if (destination == null) {
        log.warn("Blocked subscription due to missing destination");
        return null; // Block the message
      }

      // Check if it's a valid topic pattern
      if (!isValidTopicPattern(destination)) {
        log.warn("User {} attempted to subscribe to unrelated topic: {}", username, destination);
        return null; // Block the message
      }

      // Check if it's an admin topic
      if (ADMIN_TOPIC_PATTERN.matcher(destination).matches()) {
        if (!hasAdminRole()) {
          log.warn("User {} attempted to subscribe to admin topic {} without admin role",
                   username, destination);
          return null; // Block the message
        }
      }

      // Check if it's a user-specific topic
      Matcher userTopicMatcher = USER_TOPIC_PATTERN.matcher(destination);
      if (userTopicMatcher.matches()) {
        String topicUsername = userTopicMatcher.group(1);
        if (!isAuthorizedForUserTopic(topicUsername)) {
          log.warn("User {} attempted to subscribe to topic for user {}",
                   username, topicUsername);
          return null; // Block the message
        }
      }
    }

    // Don't clear the security context here as it might be needed for subsequent operations
    return message;
  }

  /**
   * Checks if the destination matches any valid topic pattern.
   */
  private boolean isValidTopicPattern(String destination) {
    return INVENTORY_TOPIC_PATTERN.matcher(destination).matches() ||
           USER_TOPIC_PATTERN.matcher(destination).matches() ||
           ADMIN_TOPIC_PATTERN.matcher(destination).matches();
  }


  private boolean hasAdminRole() {
    return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_ADMIN.getValue());
  }

  private boolean isAuthorizedForUserTopic(String topicUsername) {

    String username = SecurityUtils.getCurrentUserUserName().orElse(null);
    return Objects.equals(topicUsername, username);
  }
}
