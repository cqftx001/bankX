package com.bankx.demo.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Generic paginated response wrapper.
 *
 * Wraps Spring Data's {@code Page<T>} into a stable, frontend-friendly
 * structure. Uses 1-based page numbers (Spring uses 0-based internally).
 *
 * Why a custom wrapper instead of returning Spring's Page directly?
 *  - Decouples the API contract from Spring's internal data structures
 *  - Allows the page number convention (1-based) to match user expectations
 *  - Future-proofs the API for a switch to cursor-based pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic paginated response")
public class PageResult<T> {

    @Schema(description = "Items in the current page", example = "[]")
    private List<T> items;

    @Schema(description = "Current page number, 1-based", example = "1")
    private int pageNumber;

    @Schema(description = "Number of items per page", example = "20")
    private int pageSize;

    @Schema(description = "Total number of items across all pages", example = "128")
    private long total;

    @Schema(description = "Total number of pages", example = "7")
    private int totalPages;

    @Schema(description = "Whether more pages exist after the current one")
    private boolean hasNext;

    @Schema(description = "Whether this is the first page")
    private boolean firstPage;

    @Schema(description = "Whether this is the last page")
    private boolean lastPage;

    /**
     * Build a PageResult from Spring Data's Page.
     * Converts 0-based page number to 1-based for the API contract.
     */
    public static <T> PageResult<T> from(Page<T> page) {
        if (page == null) {
            return empty();
        }
        return PageResult.<T>builder()
                .items(page.getContent())
                .pageNumber(page.getNumber() + 1)   // 0-based → 1-based
                .pageSize(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .firstPage(page.isFirst())
                .lastPage(page.isLast())
                .build();
    }

    /**
     * Build an empty PageResult — useful for guard cases.
     */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .items(List.of())
                .pageNumber(1)
                .pageSize(0)
                .total(0L)
                .totalPages(0)
                .hasNext(false)
                .firstPage(true)
                .lastPage(true)
                .build();
    }

    /**
     * Transform items from type T to type R while preserving page metadata.
     * Used to convert Page&lt;Entity&gt; to PageResult&lt;Vo&gt;.
     */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        List<R> mapped = items == null
                ? List.of()
                : items.stream().map(mapper).toList();
        return PageResult.<R>builder()
                .items(mapped)
                .pageNumber(this.pageNumber)
                .pageSize(this.pageSize)
                .total(this.total)
                .totalPages(this.totalPages)
                .hasNext(this.hasNext)
                .firstPage(this.firstPage)
                .lastPage(this.lastPage)
                .build();
    }
}