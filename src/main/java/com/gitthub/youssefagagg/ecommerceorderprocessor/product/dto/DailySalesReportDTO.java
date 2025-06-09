package com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for daily sales report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesReportDTO {
  private LocalDate date;
  private BigDecimal totalSales;
}