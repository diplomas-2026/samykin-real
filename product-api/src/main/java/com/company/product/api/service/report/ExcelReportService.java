package com.company.product.api.service.report;

import com.company.product.api.entity.PayoutStatus;
import com.company.product.api.repository.PayoutRepository;
import com.company.product.api.repository.UserAccountRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final PayoutRepository payoutRepository;
    private final UserAccountRepository userAccountRepository;

    public byte[] buildPayoutsPeriodReport(LocalDate from, LocalDate to) {
        return withWorkbook("Выплаты за период", sheet -> {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Код");
            header.createCell(1).setCellValue("Сотрудник");
            header.createCell(2).setCellValue("Тип");
            header.createCell(3).setCellValue("Сумма");
            header.createCell(4).setCellValue("Дата");
            header.createCell(5).setCellValue("Статус");

            int rowIndex = 1;
            for (var payout : payoutRepository.findAllByPayoutDateBetweenOrderByPayoutDateAsc(from, to)) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(payout.getPayoutCode());
                row.createCell(1).setCellValue(payout.getEmployee().getFullName());
                row.createCell(2).setCellValue(payout.getPayoutType());
                row.createCell(3).setCellValue(payout.getAmount().doubleValue());
                row.createCell(4).setCellValue(payout.getPayoutDate().toString());
                row.createCell(5).setCellValue(payout.getStatus().name());
            }
        });
    }

    public byte[] buildStatusReport() {
        return withWorkbook("Выданные и невыданные", sheet -> {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Статус");
            header.createCell(1).setCellValue("Количество");
            header.createCell(2).setCellValue("Сумма");

            fillStatusRow(sheet.createRow(1), "Выдана", PayoutStatus.PAID);
            fillStatusRow(sheet.createRow(2), "Не выдана", null);
        });
    }

    public byte[] buildEmployeesReport() {
        return withWorkbook("Сотрудники", sheet -> {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ФИО");
            header.createCell(1).setCellValue("Email");
            header.createCell(2).setCellValue("Подразделение");
            header.createCell(3).setCellValue("Должность");
            header.createCell(4).setCellValue("Табельный номер");

            int rowIndex = 1;
            for (var user : userAccountRepository.findAllByRoleOrderByFullNameAsc(com.company.product.api.entity.UserRole.EMPLOYEE)) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(user.getFullName());
                row.createCell(1).setCellValue(user.getEmail());
                row.createCell(2).setCellValue(user.getDepartment());
                row.createCell(3).setCellValue(user.getPosition());
                row.createCell(4).setCellValue(user.getEmployeeCode());
            }
        });
    }

    public byte[] buildSummaryReport() {
        return withWorkbook("Сводка", sheet -> {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Статус");
            header.createCell(1).setCellValue("Количество");
            header.createCell(2).setCellValue("Сумма");
            int rowIndex = 1;
            for (PayoutStatus status : PayoutStatus.values()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(status.name());
                row.createCell(1).setCellValue(payoutRepository.countByStatus(status));
                double total = payoutRepository.findAllByStatusOrderByCreatedAtDesc(status).stream()
                    .mapToDouble(payout -> payout.getAmount().doubleValue())
                    .sum();
                row.createCell(2).setCellValue(total);
            }
        });
    }

    private void fillStatusRow(Row row, String label, PayoutStatus status) {
        row.createCell(0).setCellValue(label);
        var payouts = status == null
            ? payoutRepository.findAllDetailed().stream().filter(payout -> payout.getStatus() != PayoutStatus.PAID).toList()
            : payoutRepository.findAllByStatusOrderByCreatedAtDesc(status);
        row.createCell(1).setCellValue(payouts.size());
        row.createCell(2).setCellValue(payouts.stream().mapToDouble(payout -> payout.getAmount().doubleValue()).sum());
    }

    private byte[] withWorkbook(String sheetName, SheetWriter writer) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(sheetName);
            writer.accept(sheet);
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось сформировать Excel-отчет", exception);
        }
    }

    @FunctionalInterface
    private interface SheetWriter {
        void accept(org.apache.poi.xssf.usermodel.XSSFSheet sheet);
    }
}
