package be.hoffmann.backtaxes.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for pagination with security limits.
 *
 * Prevents memory exhaustion attacks by limiting maximum page size.
 */
public final class PaginationUtils {

    /**
     * Maximum allowed page size to prevent memory exhaustion.
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Default page size when not specified.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtils() {
        // Utility class, no instantiation
    }

    /**
     * Creates a safe Pageable with enforced size limits.
     *
     * @param page Page number (0-indexed), defaults to 0 if null
     * @param size Page size, defaults to DEFAULT_PAGE_SIZE if null, capped at MAX_PAGE_SIZE
     * @param sort Sort configuration
     * @return A safe Pageable instance
     */
    public static Pageable createPageable(Integer page, Integer size, Sort sort) {
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        return PageRequest.of(pageNum, pageSize, sort);
    }

    /**
     * Creates a safe Pageable with enforced size limits and default sorting.
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return A safe Pageable instance with no sorting
     */
    public static Pageable createPageable(Integer page, Integer size) {
        return createPageable(page, size, Sort.unsorted());
    }

    /**
     * Validates and caps a page size to the maximum allowed.
     *
     * @param requestedSize The requested page size
     * @return The capped page size (between 1 and MAX_PAGE_SIZE)
     */
    public static int capPageSize(int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(requestedSize, MAX_PAGE_SIZE);
    }
}
