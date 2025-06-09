package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.PaymentDTO;

/**
 * Service Interface for managing
 * {@link com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Payment}.
 */
public interface PaymentService {

  /**
   * Process a payment for an order.
   *
   * @param paymentDTO the payment to process
   * @return the processed payment
   */
  PaymentDTO processPayment(PaymentDTO paymentDTO);

}