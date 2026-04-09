package com.company.product.api.controller;

import com.company.product.api.service.report.ExcelReportService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ExcelReportService excelReportService;

    @GetMapping("/payouts-period")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<byte[]> payoutsPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return excel("viplaty-za-period.xlsx", excelReportService.buildPayoutsPeriodReport(from, to));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<byte[]> status() {
        return excel("vidannye-i-nevidannye.xlsx", excelReportService.buildStatusReport());
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<byte[]> employees() {
        return excel("otchet-po-sotrudnikam.xlsx", excelReportService.buildEmployeesReport());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<byte[]> summary() {
        return excel("svodnyy-otchet.xlsx", excelReportService.buildSummaryReport());
    }

    private ResponseEntity<byte[]> excel(String fileName, byte[] body) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .body(body);
    }
}
