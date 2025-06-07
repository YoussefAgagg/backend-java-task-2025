package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.custom;

import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Represents a custom exception used in the application to handle domain-specific errors. This
 * exception class extends RuntimeException and provides additional context such as an
 * {@link ErrorCode} and an optional map of additional details.
 *
 * <p>The {@link ErrorCode} encapsulates the unique identifier and message
 * associated with errors, while the additional details map facilitates passing more error-related
 * metadata.</p>
 *
 * <p></p>
 * Constructors: - One constructor accepts the error code and a custom error message. - The second
 * constructor additionally takes a map of details to provide more context.
 *
 * <p></p>
 * Features: - Allows dynamic addition of individual details after the exception is created. -
 * Provides methods for retrieving specific details using a key.
 *
 * <p></p>
 * This class is particularly useful for handling and providing informative error details throughout
 * the application.
 */
@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode errorCode;
  private final Map<String, Object> additionalDetails;

  /**
   * Constructs a new {@code CustomException} with the specified error code and message.
   *
   * @param errorCode the {@link ErrorCode} representing the specific error type
   * @param message   the detailed message explaining the error
   */
  // Standard constructor
  public CustomException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.additionalDetails = new HashMap<>();
  }

  /**
   * Constructs a new {@code CustomException} with the specified error code, message, and additional
   * details.
   *
   * @param errorCode the {@link ErrorCode} representing the specific error type
   * @param message   the detailed message explaining the error
   * @param details   a map containing additional details related to the error; if null, an empty
   *                  map is used
   */
  // Constructor with additional details
  public CustomException(ErrorCode errorCode, String message, Map<String, Object> details) {
    super(message);
    this.errorCode = errorCode;
    this.additionalDetails = details != null ? details : new HashMap<>();
  }

  /**
   * Adds a key-value pair to the additional details map dynamically.
   *
   * @param key   the key for the detail to be added
   * @param value the value associated with the key
   */
  // Add individual details dynamically
  public void addDetail(String key, Object value) {
    this.additionalDetails.put(key, value);
  }

  /**
   * Retrieves the value associated with the specified key from the additional details map.
   *
   * @param key the key used to identify the specific detail to retrieve
   * @return the value associated with the specified key, or null if the key does not exist
   */
  // Retrieve a specific detail
  public Object getDetail(String key) {
    return this.additionalDetails.get(key);
  }
}