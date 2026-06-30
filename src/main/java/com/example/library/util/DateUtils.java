package com.example.library.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class providing static helper methods for date calculations
 * used throughout the library management system.
 */
public final class DateUtils {

    private static final DateTimeFormatter DUE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy");

    private DateUtils() {
        // Utility class — not instantiable
    }

    /**
     * Calculates the due date by adding loanPeriodDays to the checkoutDate.
     *
     * @param checkoutDate   the date the book was checked out
     * @param loanPeriodDays number of days for the loan period
     * @return the calculated due date
     */
    public static LocalDateTime calculateDueDate(LocalDateTime checkoutDate, int loanPeriodDays) {
        if (checkoutDate == null) {
            throw new IllegalArgumentException("Checkout date must not be null");
        }
        return checkoutDate.plusDays(loanPeriodDays);
    }

    /**
     * Checks whether a due date has passed.
     *
     * @param dueDate the due date to check
     * @return true if the current time is after the due date
     */
    public static boolean isOverdue(LocalDateTime dueDate) {
        if (dueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Returns the number of days the item is overdue.
     * Returns 0 if not yet overdue.
     *
     * @param dueDate the due date to check against
     * @return number of days overdue, or 0 if not overdue
     */
    public static long getDaysOverdue(LocalDateTime dueDate) {
        if (!isOverdue(dueDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
    }

    /**
     * Formats a due date into a human-readable string, e.g. "Jan 15, 2024".
     *
     * @param dueDate the date to format
     * @return formatted date string
     */
    public static String formatDueDate(LocalDateTime dueDate) {
        if (dueDate == null) {
            return "N/A";
        }
        return dueDate.format(DUE_DATE_FORMATTER);
    }

    /**
     * Returns the number of days remaining until the due date.
     * Returns a negative number if already overdue.
     *
     * @param dueDate the due date
     * @return days until due (negative if overdue)
     */
    public static long getDaysUntilDue(LocalDateTime dueDate) {
        if (dueDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }
}
