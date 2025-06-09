package com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Order} entity.
 */
@Repository
public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

  /**
   * Find orders by user.
   *
   * @param user     the user
   * @param pageable the pagination information
   * @return the list of orders
   */
  Page<Order> findByUser(User user, Pageable pageable);

  /**
   * Find orders by status.
   *
   * @param status   the status
   * @param pageable the pagination information
   * @return the list of orders
   */
  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  /**
   * Find orders by user and status.
   *
   * @param user     the user
   * @param status   the status
   * @param pageable the pagination information
   * @return the list of orders
   */
  Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

  /**
   * Get daily sales report.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return the list of orders with total amount
   */
  @Query(value = "SELECT CAST(created_date AS DATE) as order_date, SUM(total_amount) as total " +
                 "FROM orders " +
                 "WHERE status = 'PAID' AND created_date BETWEEN ?1 AND ?2 " +
                 "GROUP BY CAST(created_date AS DATE) " +
                 "ORDER BY order_date",
         nativeQuery = true)
  List<Object[]> getDailySalesReport(Instant startDate, Instant endDate);
}
