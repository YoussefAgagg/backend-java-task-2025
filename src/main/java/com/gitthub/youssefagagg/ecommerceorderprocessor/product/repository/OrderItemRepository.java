package com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderItem;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link OrderItem} entity.
 */
@Repository
public interface OrderItemRepository
    extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {

  /**
   * Find order items by order.
   *
   * @param order the order
   * @return the list of order items
   */
  List<OrderItem> findByOrder(Order order);

  /**
   * Find order items by product.
   *
   * @param product the product
   * @return the list of order items
   */
  List<OrderItem> findByProduct(Product product);

  /**
   * Delete order items by order.
   *
   * @param order the order
   */
  void deleteByOrder(Order order);
}