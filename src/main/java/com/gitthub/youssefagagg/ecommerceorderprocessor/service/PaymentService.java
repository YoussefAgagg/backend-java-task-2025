package com.gitthub.youssefagagg.ecommerceorderprocessor.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaymentDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Payment;

/**
 * Service Interface for managing
 * {@link Payment}.
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