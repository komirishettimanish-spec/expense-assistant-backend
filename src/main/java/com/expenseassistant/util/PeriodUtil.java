package com.expenseassistant.util;

import com.expenseassistant.exception.BadRequestException;

import java.time.YearMonth;

public final class PeriodUtil {

    private PeriodUtil() {
    }

    /** Uses the current month/year for any value that is null. */
    public static YearMonth resolve(Integer month, Integer year) {
        YearMonth now = YearMonth.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        if (m < 1 || m > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (y < 2000 || y > 2100) {
            throw new BadRequestException("Year must be between 2000 and 2100");
        }
        return YearMonth.of(y, m);
    }
}
