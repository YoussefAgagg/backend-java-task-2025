package com.gitthub.youssefagagg.ecommerceorderprocessor.web.rest.v1;

import static com.gitthub.youssefagagg.ecommerceorderprocessor.util.Constants.OPEN_API_SECURITY_REQUIREMENT;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing
 * {@link Order}.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management")
public class OrderController {

  private final OrderService orderService;

  /**
   * {@code POST  /} : Create a new order.
   *
   * @param orderDTO the order to create. Clients can provide an idempotencyKey to prevent duplicate
   *                 order processing.
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new
   *     order
   */
  @PostMapping
  @Operation(
      summary = "Create a new order",
      description = "Create a new order with the provided details. " +
                    "Clients can provide an idempotencyKey to prevent duplicate order processing. "
                    +
                    "If not provided, a random idempotencyKey will be generated.",
      security = @SecurityRequirement(name = OPEN_API_SECURITY_REQUIREMENT)
  )
  public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
    log.debug("REST request to save Order : {}", orderDTO);
    OrderDTO result = orderService.createOrder(orderDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  /**
   * {@code GET  /} : Get all orders for the current user.
   *
   * @param pageable the pagination information
   * @param status   optional status filter
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of orders in body
   */
  @GetMapping
  @Operation(
      summary = "Get all orders for the current user with pagination",
      security = @SecurityRequirement(name = OPEN_API_SECURITY_REQUIREMENT)
  )
  public ResponseEntity<PaginationResponse<OrderDTO>> getAllOrders(
      @Parameter(
          description = "Pagination information",
          schema = @Schema(implementation = Pageable.class),
          example = "{\"page\": 0, \"size\": 10}"
      ) Pageable pageable,
      @RequestParam(required = false) OrderStatus status) {
    log.debug("REST request to get a page of Orders for current user");
    PaginationResponse<OrderDTO> page = status != null ?
                                        orderService.getCurrentUserOrdersByStatus(status, pageable)
                                                       :
                                        orderService.getCurrentUserOrders(pageable);
    return ResponseEntity.ok().body(page);
  }

  /**
   * {@code GET  /{id}} : Get the "id" order.
   *
   * @param id the id of the order to retrieve
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the order, or
   *     with status {@code 404 (Not Found)}
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get an order by ID",
      security = @SecurityRequirement(name = OPEN_API_SECURITY_REQUIREMENT)
  )
  public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
    log.debug("REST request to get Order : {}", id);
    OrderDTO orderDTO = orderService.getOrder(id);
    return ResponseEntity.ok().body(orderDTO);
  }

  /**
   * {@code PUT  /{id}/cancel} : Cancel an order.
   *
   * @param id the id of the order to cancel
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated order
   */
  @PutMapping("/{id}/cancel")
  @Operation(
      summary = "Cancel an order",
      security = @SecurityRequirement(name = OPEN_API_SECURITY_REQUIREMENT)
  )
  public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
    log.debug("REST request to cancel Order : {}", id);
    OrderDTO result = orderService.cancelOrder(id);
    return ResponseEntity.ok().body(result);
  }

  /**
   * {@code GET  /{id}/status} : Get the status of an order.
   *
   * @param id the id of the order
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the order status
   */
  @GetMapping("/{id}/status")
  @Operation(
      summary = "Get the status of an order",
      security = @SecurityRequirement(name = OPEN_API_SECURITY_REQUIREMENT)
  )
  public ResponseEntity<OrderStatus> getOrderStatus(@PathVariable Long id) {
    log.debug("REST request to get Order status : {}", id);
    OrderStatus status = orderService.getOrderStatus(id);
    return ResponseEntity.ok().body(status);
  }
}
