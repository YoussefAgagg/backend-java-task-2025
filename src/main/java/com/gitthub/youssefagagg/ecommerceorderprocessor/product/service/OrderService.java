package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.DailySalesReportDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Interface for managing
 * {@link com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order}.
 */
public interface OrderService {

  /**
   * Create a new order.
   *
   * @param orderDTO the order to create
   * @return the created order
   */
  OrderDTO createOrder(OrderDTO orderDTO);

  /**
   * Get all orders for the current user with pagination.
   *
   * @param pageable the pagination information
   * @return the list of orders
   */
  PaginationResponse<OrderDTO> getCurrentUserOrders(Pageable pageable);

  /**
   * Get all orders for the current user with the given status and pagination.
   *
   * @param status   the status
   * @param pageable the pagination information
   * @return the list of orders
   */
  PaginationResponse<OrderDTO> getCurrentUserOrdersByStatus(OrderStatus status, Pageable pageable);

  /**
   * Get all orders with pagination (admin only).
   *
   * @param pageable the pagination information
   * @return the list of orders
   */
  PaginationResponse<OrderDTO> getAllOrders(Pageable pageable);

  /**
   * Get all orders with the given status and pagination (admin only).
   *
   * @param status   the status
   * @param pageable the pagination information
   * @return the list of orders
   */
  PaginationResponse<OrderDTO> getAllOrdersByStatus(OrderStatus status, Pageable pageable);

  /**
   * Get an order by ID.
   *
   * @param id the order ID
   * @return the order
   */
  OrderDTO getOrder(Long id);

  /**
   * Cancel an order.
   *
   * @param id the order ID
   * @return the updated order
   */
  OrderDTO cancelOrder(Long id);

  /**
   * Update order status (admin only).
   *
   * @param id     the order ID
   * @param status the new status
   * @return the updated order
   */
  OrderDTO updateOrderStatus(Long id, OrderStatus status);

  /**
   * Get order status.
   *
   * @param id the order ID
   * @return the order status
   */
  OrderStatus getOrderStatus(Long id);

  @Transactional(readOnly = true)
  List<DailySalesReportDTO> getDailySalesReport(LocalDate startDate, LocalDate endDate);
}