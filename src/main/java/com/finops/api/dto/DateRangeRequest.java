package com.finops.api.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record DateRangeRequest(
        @NotNull(message = "시작일은 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
    public DateRangeRequest {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다");
        }
    }

    public static DateRangeRequest lastNDays(int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        return new DateRangeRequest(start, end);
    }

    public static DateRangeRequest thisMonth() {
        LocalDate now = LocalDate.now();
        return new DateRangeRequest(now.withDayOfMonth(1), now);
    }
}
