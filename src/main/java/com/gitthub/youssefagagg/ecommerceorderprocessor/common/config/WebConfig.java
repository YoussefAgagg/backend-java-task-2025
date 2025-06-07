package com.gitthub.youssefagagg.ecommerceorderprocessor.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for web.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("*") // replace with your domain
            .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
            .allowedHeaders("*");
  }

  /**
   * Configure the ObjectMapper to handle Instant serialization globally. This ensures all Instant
   * fields are serialized in ISO format without requiring individual annotations.
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    // Register JavaTimeModule to handle Java 8 date/time types
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    objectMapper.registerModule(javaTimeModule);

    // Configure to write dates as ISO-8601 strings
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    return objectMapper;
  }

  /**
   * Configure message converters to use our custom ObjectMapper.
   */
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // Find and replace the default MappingJackson2HttpMessageConverter
    for (int i = 0; i < converters.size(); i++) {
      HttpMessageConverter<?> converter = converters.get(i);
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        converters.set(i, new MappingJackson2HttpMessageConverter(objectMapper()));
        break;
      }
    }
  }
}
