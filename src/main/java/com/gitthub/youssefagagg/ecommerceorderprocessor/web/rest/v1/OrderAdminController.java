package com.gitthub.youssefagagg.ecommerceorderprocessor.web.rest.v1;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.DailySalesReportDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin operations.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations")
public class OrderAdminController {

  private final OrderService orderService;

  /**
   * {@code GET  /orders} : Get all orders.
   *
   * @param pageable the pagination information
   * @param status   optional status filter
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of orders in body
   */
  @GetMapping("/orders")
  @Operation(summary = "Get all orders with pagination (admin only)")
  public ResponseEntity<PaginationResponse<OrderDTO>> getAllOrders(
      Pageable pageable,
      @RequestParam(required = false) OrderStatus status) {
    log.debug("REST request to get a page of all Orders");
    PaginationResponse<OrderDTO> page = status != null ?
                                        orderService.getAllOrdersByStatus(status, pageable) :
                                        orderService.getAllOrders(pageable);
    return ResponseEntity.ok().body(page);
  }

  /**
   * {@code PUT  /orders/{id}/status} : Update the status of an order.
   *
   * @param id     the id of the order to update
   * @param status the new status
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated order
   */
  @PutMapping("/orders/{id}/status")
  @Operation(summary = "Update the status of an order (admin only)")
  public ResponseEntity<OrderDTO> updateOrderStatus(
      @PathVariable Long id,
      @RequestParam OrderStatus status) {
    log.debug("REST request to update Order status : {}, {}", id, status);
    OrderDTO result = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok().body(result);
  }

  /**
   * {@code GET  /reports/daily} : Get daily sales report.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the daily sales
   *     report
   */
  @GetMapping("/reports/daily")
  @Operation(summary = "Get daily sales report (admin only)")
  public ResponseEntity<List<DailySalesReportDTO>> getDailySalesReport(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    log.debug("REST request to get daily sales report from {} to {}", startDate, endDate);
    List<DailySalesReportDTO> report = orderService.getDailySalesReport(startDate, endDate);
    return ResponseEntity.ok().body(report);
  }


}
