package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

/**
 * Represents an API error response that is used to encapsulate details about errors that occur
 * during the processing of a request. This class is typically serialized to JSON and returned to
 * API clients when an exception or error occurs. The response contains details such as: - HTTP
 * status of the error. - A unique error code. - A descriptive error key and message. - Error
 * details, if available. - Collections of field-specific, global, and parameter errors for
 * validation or other issues. - Additional error properties that can be dynamically added. -
 * Optional trace information for debugging purposes. This class also provides methods to add field
 * errors, global errors, parameter errors, and arbitrary properties to the error response. The
 * class leverages Jackson annotations for JSON serialization: - Non-empty fields are included in
 * the JSON response. - The `httpStatus` field is ignored during serialization. - Any additional
 * properties are serialized as part of the JSON response.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiErrorResponse {
  @JsonIgnore
  private final HttpStatusCode httpStatus;
  private final String code;
  private final String key;
  private final String message;
  private final String errorDetails;
  @JsonAnyGetter
  private final Map<String, Object> properties;
  private final List<ApiFieldError> fieldErrors;
  private final List<ApiGlobalError> globalErrors;
  private final List<ApiParameterError> parameterErrors;
  private String traceParent;

  /**
   * Constructs an ApiErrorResponse instance with the specified HTTP status, error code, error key,
   * and error message.
   *
   * @param httpStatus the HTTP status code representing the type of error (e.g., 400, 404, 500).
   * @param code       a string representing the error code, which uniquely identifies the error
   *                   type or category.
   * @param key        a descriptive key that provides additional context or categorization for the
   *                   error.
   * @param message    a human-readable message describing the error, suitable for displaying to the
   *                   user.
   */
  public ApiErrorResponse(HttpStatusCode httpStatus,
                          String code,
                          String key,
                          String message) {
    this(httpStatus, code, key, message, null);
  }

  /**
   * Constructs an ApiErrorResponse instance with the specified HTTP status, error code, error key,
   * error message, and additional error details.
   *
   * @param httpStatus   the HTTP status code representing the type of error (e.g., 400, 404, 500)
   * @param code         a string representing the error code, which uniquely identifies the error
   *                     type or category
   * @param key          a descriptive key that provides additional context or categorization for
   *                     the error
   * @param message      a human-readable message describing the error, suitable for displaying to
   *                     the user
   * @param errorDetails a detailed message or additional information about the error
   */
  public ApiErrorResponse(HttpStatusCode httpStatus,
                          String code,
                          String key,
                          String message,
                          String errorDetails) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.key = key;
    this.message = message;
    this.errorDetails = errorDetails;
    this.properties = new HashMap<>();
    this.fieldErrors = new ArrayList<>();
    this.globalErrors = new ArrayList<>();
    this.parameterErrors = new ArrayList<>();
  }

  /**
   * Adds an error property to the existing collection of properties. This can be used to provide
   * supplementary data related to the error, such as method names, parameter details, or custom
   * attributes.
   *
   * @param propertyName  the name of the property to be added. This serves as the key for
   *                      identifying the property.
   * @param propertyValue the value associated with the property. This can be any object that
   *                      represents the property value.
   */
  public void addErrorProperty(String propertyName, Object propertyValue) {
    properties.put(propertyName, propertyValue);
  }

  /**
   * Adds a field-specific error to the collection of field errors. This method is used to capture
   * detailed validation issues related to individual fields or properties in an API request.
   *
   * @param fieldError an instance of {@link ApiFieldError} representing the details of the field
   *                   error, including the field name, error message, the rejected value, and the
   *                   property path.
   */
  public void addFieldError(ApiFieldError fieldError) {
    fieldErrors.add(fieldError);
  }

  /**
   * Adds a global error to the collection of global errors associated with the API response.
   *
   * @param globalError an instance of {@link ApiGlobalError} representing a global error that is
   *                    not specific to a particular field or parameter in the API request.
   */
  public void addGlobalError(ApiGlobalError globalError) {
    globalErrors.add(globalError);
  }

  /**
   * Adds a parameter-specific error to the collection of parameter errors. This method allows
   * capturing detailed issues related to method parameters in an API request.
   *
   * @param parameterError an instance of {@link ApiParameterError} representing the details of the
   *                       parameter error, including the parameter name, error message, and the
   *                       rejected value.
   */
  public void addParameterError(ApiParameterError parameterError) {
    parameterErrors.add(parameterError);
  }
}
