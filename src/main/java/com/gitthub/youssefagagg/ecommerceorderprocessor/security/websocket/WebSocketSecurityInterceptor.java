package com.gitthub.youssefagagg.ecommerceorderprocessor.security.websocket;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import java.util.Optional;
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
import org.springframework.stereotype.Component;

/**
 * WebSocket security interceptor to enforce security rules for WebSocket connections. - Topics with
 * "admin" in the path require admin role - Topics with user IDs only accept connections from the
 * same user
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

  private static final Pattern USER_TOPIC_PATTERN = Pattern.compile(
      "/topic/(?:orders|notifications)/([a-zA-Z0-9_-]+)");
  private static final Pattern ADMIN_TOPIC_PATTERN = Pattern.compile("/topic/admin/.*");
  private final UserRepository userRepository;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                                                                     StompHeaderAccessor.class);

    if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      String destination = accessor.getDestination();

      if (destination == null) {
        log.warn("Blocked subscription to {} due to missing destination or authentication",
                 destination);
        return null; // Block the message
      }

      // Check if it's an admin topic
      if (ADMIN_TOPIC_PATTERN.matcher(destination).matches()) {
        if (!hasAdminRole()) {
          log.warn("User attempted to subscribe to admin topic {} without admin role", destination);
          return null; // Block the message
        }
      }

      // Check if it's a user-specific topic
      Matcher userTopicMatcher = USER_TOPIC_PATTERN.matcher(destination);
      if (userTopicMatcher.matches()) {
        String topicUserId = userTopicMatcher.group(1);
        if (!isAuthorizedForUserTopic(topicUserId)) {
          log.warn("User attempted to subscribe to topic for user {}", topicUserId);
          return null; // Block the message
        }
      }
    }

    return message;
  }

  private boolean hasAdminRole() {
    return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_USER.getValue());
  }

  private boolean isAuthorizedForUserTopic(String topicUserId) {
    if (hasAdminRole()) {
      return true; // Admins can access all user topics
    }

    // For regular users, check if the topic user ID matches their own user ID
    String username = SecurityUtils.getCurrentUserUserName().orElseThrow();
    Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(username);

    if (userOptional.isPresent()) {
      Long userId = userOptional.get().getId();
      return userId.toString().equals(topicUserId);
    }


    return false;
  }
}