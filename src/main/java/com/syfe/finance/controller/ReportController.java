package com.syfe.finance.controller;

import com.syfe.finance.dto.MonthlyReportResponse;
import com.syfe.finance.dto.YearlyReportResponse;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for generating financial reports.
 * Provides monthly and yearly aggregated data.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    /**
     * Retrieves a monthly financial report.
     *
     * @param year  the year for the report
     * @param month the month for the report (1-12)
     * @return the monthly report data containing income, expenses, and savings
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        User currentUser = authService.getCurrentUser();
        MonthlyReportResponse response = reportService.getMonthlyReport(currentUser.getId(), year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a yearly financial report.
     *
     * @param year the year for the report
     * @return the yearly report data containing aggregated income, expenses, and
     *         savings
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        User currentUser = authService.getCurrentUser();
        YearlyReportResponse response = reportService.getYearlyReport(currentUser.getId(), year);
        return ResponseEntity.ok(response);
    }
}
