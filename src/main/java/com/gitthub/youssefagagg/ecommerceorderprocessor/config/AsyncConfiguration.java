package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for enabling and customizing asynchronous processing in the application.
 *
 * <p></p>
 * This class is marked as a Spring configuration class using the {@code @Configuration} annotation
 * and enables Spring's asynchronous method execution capability with the {@code @EnableAsync}
 * annotation. It provides a custom task executor and a task decorator to support asynchronous
 * operations with enhanced configurations.
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfiguration {

  @Value("${async.core-pool-size:5}")
  private int corePoolSize;

  @Value("${async.max-pool-size:10}")
  private int maxPoolSize;

  @Value("${async.queue-capacity:100}")
  private int queueCapacity;

  /**
   * Decorator to propagate the context to the async threads.
   *
   * @return the task decorator
   */
  @Bean
  public TaskDecorator decorator() {
    return new ContextPropagatingTaskDecorator();
  }

  /**
   * Defines a bean named "taskExecutor" to provide a custom thread pool task executor for
   * asynchronous operations in the application. The executor is configured with properties from
   * the application configuration.
   *
   * @return an {@link Executor} instance configured as a thread pool task executor.
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix("Async-");
    executor.initialize();
    return executor;
  }
}
