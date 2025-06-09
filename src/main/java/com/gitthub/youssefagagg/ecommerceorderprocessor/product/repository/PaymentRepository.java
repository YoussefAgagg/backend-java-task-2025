package com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Payment;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Payment} entity.
 */
@Repository
public interface PaymentRepository
    extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

  /**
   * Find payments by order.
   *
   * @param order the order
   * @return the list of payments
   */
  List<Payment> findByOrder(Order order);

  /**
   * Find payments by order and status.
   *
   * @param order  the order
   * @param status the status
   * @return the list of payments
   */
  List<Payment> findByOrderAndStatus(Order order, PaymentStatus status);

  /**
   * Find payment by transaction ID.
   *
   * @param transactionId the transaction ID
   * @return the payment if found, empty otherwise
   */
  Optional<Payment> findByTransactionId(String transactionId);

  /**
   * Find payment by idempotency key.
   *
   * @param idempotencyKey the idempotency key
   * @return the payment if found, empty otherwise
   */
  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  /**
   * Find the latest payment for an order.
   *
   * @param order the order
   * @return the payment if found, empty otherwise
   */
  Optional<Payment> findTopByOrderOrderByCreatedDateDesc(Order order);
}
