package com.example.bijlibachat.service;

import com.example.bijlibachat.dao.MeterReadingDAO;
import com.example.bijlibachat.dao.BudgetDAO;
import com.example.bijlibachat.model.ConsumptionReport;
import com.example.bijlibachat.model.MeterReading;
import com.example.bijlibachat.model.MonthlyBudget;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportService {
    private MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private BudgetDAO budgetDAO = new BudgetDAO();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    public ConsumptionReport generateReport(int userID, Date startDate, Date endDate, String accountType) {
        List<MeterReading> readings = meterReadingDAO.getReadingsByPeriod(userID, startDate, endDate);

        float totalUnits = 0;
        for (MeterReading r : readings) {
            totalUnits += r.getUnitsConsumed();
        }

        ConsumptionReport report = new ConsumptionReport();
        report.setUserID(userID);
        report.setGeneratedDate(new Date());
        report.setPeriod(startDate.toString() + " to " + endDate.toString());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalUnits(totalUnits);
        report.setReadings(readings);
        report.setEstimatedCost(estimateCost(totalUnits, accountType));

        return report;
    }

    public ConsumptionReport exportReportFiles(ConsumptionReport report, String accountType) {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null.");
        }

        report.setEstimatedCost(estimateCost(report.getTotalUnits(), accountType));

        try {
            Path reportsDir = Paths.get("reports").toAbsolutePath().normalize();
            Files.createDirectories(reportsDir);

            String baseName = buildBaseName(report);
            Path csvPath = reportsDir.resolve(baseName + ".csv");
            Path pdfPath = reportsDir.resolve(baseName + ".pdf");

            writeCsv(report, csvPath);
            writePdf(report, pdfPath);

            report.setCsvPath(csvPath.toString());
            report.setPdfPath(pdfPath.toString());
            return report;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not export report files: " + ex.getMessage(), ex);
        }
    }

    public float getCurrentCycleUnits(int userID) {
        return meterReadingDAO.getTotalUnitsThisCycle(userID);
    }

    public float estimateCost(float units, String accountType) {
        TariffEngine engine = new TariffEngine(accountType);
        return engine.applyTariffStructure(units);
    }

    public float getCeilingProgress(int userID, String month) {
        MonthlyBudget budget = budgetDAO.getBudgetByUserAndMonth(userID, month);
        if (budget == null) return 0;
        float consumed = meterReadingDAO.getTotalUnitsThisCycle(userID);
        return (consumed / budget.getCeilingAmount()) * 100;
    }

    public void saveCeiling(int userID, float units, String month) {
        MonthlyBudget existing = budgetDAO.getBudgetByUserAndMonth(userID, month);
        if (existing != null) {
            existing.setCeilingAmount(units);
            budgetDAO.updateBudget(existing);
        } else {
            MonthlyBudget b = new MonthlyBudget();
            b.setUserID(userID);
            b.setCeilingAmount(units);
            b.setMonth(month);
            budgetDAO.saveBudget(b);
        }
    }
    public MonthlyBudget getCurrentBudget(int userID, String month) {
        return budgetDAO.getBudgetByUserAndMonth(userID, month);
    }

    private String buildBaseName(ConsumptionReport report) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(report.getGeneratedDate());
        return "report_user_" + report.getUserID() + "_" + timestamp;
    }

    private void writeCsv(ConsumptionReport report, Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("User ID,").append(report.getUserID()).append('\n');
        builder.append("Period,").append(csv(report.getPeriod())).append('\n');
        builder.append("Generated On,").append(csv(dateFormat.format(report.getGeneratedDate()))).append('\n');
        builder.append("Total Units,").append(numberFormat.format(report.getTotalUnits())).append('\n');
        builder.append("Estimated Cost,").append(currencyFormat.format(report.getEstimatedCost())).append('\n');
        builder.append('\n');
        builder.append("Reading Date,Meter Value,Units Consumed\n");

        if (report.getReadings() != null) {
            for (MeterReading reading : report.getReadings()) {
                builder.append(csv(dateFormat.format(reading.getDate()))).append(',')
                        .append(numberFormat.format(reading.getValue())).append(',')
                        .append(numberFormat.format(reading.getUnitsConsumed())).append('\n');
            }
        }

        Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
    }

    private void writePdf(ConsumptionReport report, Path path) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font headingFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50;
                float y = 780;

                y = writeLine(content, headingFont, 20, margin, y, "Bijli Bachat Report");
                y = writeLine(content, bodyFont, 11, margin, y - 8, "User ID: " + report.getUserID());
                y = writeLine(content, bodyFont, 11, margin, y, "Period: " + report.getPeriod());
                y = writeLine(content, bodyFont, 11, margin, y, "Generated On: " + dateFormat.format(report.getGeneratedDate()));
                y = writeLine(content, bodyFont, 11, margin, y, "Total Units: " + numberFormat.format(report.getTotalUnits()) + " kWh");
                y = writeLine(content, bodyFont, 11, margin, y, "Estimated Cost: Rs. " + currencyFormat.format(report.getEstimatedCost()));

                y -= 10;
                y = writeLine(content, headingFont, 14, margin, y, "Meter Reading Details");

                if (report.getReadings() == null || report.getReadings().isEmpty()) {
                    writeLine(content, bodyFont, 11, margin, y, "No readings were found for the selected period.");
                } else {
                    for (MeterReading reading : report.getReadings()) {
                        if (y < 70) {
                            break;
                        }
                        String line = dateFormat.format(reading.getDate())
                                + " | Value: " + numberFormat.format(reading.getValue())
                                + " | Units: " + numberFormat.format(reading.getUnitsConsumed());
                        y = writeLine(content, bodyFont, 10, margin, y, line);
                    }
                }
            }

            document.save(path.toFile());
        }
    }

    private float writeLine(PDPageContentStream content, PDType1Font font, int size, float x, float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - (size + 8);
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
