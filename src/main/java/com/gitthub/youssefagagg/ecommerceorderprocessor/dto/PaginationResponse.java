package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * PaginationResponse is a generic immutable record class used to encapsulate paginated data. It
 * provides information about the current page, total count of items, total pages, and the number of
 * rows per page.
 *
 * @param <T>         The type of objects contained in the paginated data list.
 * @param data        The list of objects representing the current page's data.
 * @param totalCount  The total number of items available across all pages.
 * @param noOfPages   The total number of pages, calculated based on the total count and rows per
 *                    page.
 * @param pageNo      The current page number in the pagination sequence.
 * @param rowsPerPage The number of rows per page, determining the volume of data on each page.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PaginationResponse<T>(
    @NotNull
    List<T> data,
    @NotNull
    Long totalCount,
    @NotNull
    Integer noOfPages,
    @NotNull
    Integer pageNo,
    @NotNull
    Integer rowsPerPage
) {
  /**
   * Constructor for the PaginationResponse record. Initializes the data list to an empty ArrayList
   * if it is null.
   *
   * @param data        The list of objects representing the current page's data.
   * @param totalCount  The total number of items available across all pages.
   * @param noOfPages   The total number of pages, calculated based on the total count and rows per
   *                    page.
   * @param pageNo      The current page number in the pagination sequence.
   * @param rowsPerPage The number of rows per page, determining the volume of data on each page.
   */
  public PaginationResponse {
    if (data == null) {
      data = new ArrayList<>();
    }
  }
}