package com.retail.service.impl;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.retail.mapper.CustomerBehaviorMapper;
import com.retail.service.AnalyticsService;
import com.retail.service.ExportService;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private CustomerBehaviorMapper behaviorMapper;

    @Autowired
    private AnalyticsService analyticsService;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    // ==================== Excel 导出 ====================

    @Override
    public Workbook exportTrafficExcel(Long storeId, String startDate, String endDate, String granularity) {
        Map<String, Object> data = analyticsService.traffic(storeId, startDate, endDate, granularity);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("客流统计");

        // 标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("客流统计报表");
        titleCell.setCellStyle(createTitleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 筛选条件
        Row filterRow = sheet.createRow(1);
        filterRow.createCell(0).setCellValue("时间范围: " + (startDate != null ? startDate : "全部") + " ~ " + (endDate != null ? endDate : "全部"));
        filterRow.createCell(1).setCellValue("统计粒度: " + granularity);

        // 汇总
        Row summaryRow = sheet.createRow(2);
        summaryRow.createCell(0).setCellValue("总进店人数: " + data.get("totalVisitors"));
        summaryRow.createCell(1).setCellValue("总购买人数: " + data.get("totalPurchases"));

        // 表头
        Row headerRow = sheet.createRow(4);
        String[] headers = {"序号", "时段", "进店人数", "购买人数", "转化率(%)"};
        CellStyle headerStyle = createHeaderStyle(wb);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("data");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Row row = sheet.createRow(i + 5);
                Map<String, Object> item = list.get(i);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(String.valueOf(item.getOrDefault("period", "")));
                row.createCell(2).setCellValue(toLong(item.get("total_count")));
                row.createCell(3).setCellValue(toLong(item.get("purchase_count")));
                double rate = 0;
                long total = toLong(item.get("total_count"));
                long purchase = toLong(item.get("purchase_count"));
                if (total > 0) {
                    rate = Math.round(purchase * 10000.0 / total) / 100.0;
                }
                row.createCell(4).setCellValue(rate);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 18 * 256);
        }

        return wb;
    }

    @Override
    public Workbook exportConversionExcel(Long storeId, String startDate, String endDate) {
        List<Map<String, Object>> dailyData = behaviorMapper.trafficByDay(storeId, startDate, endDate);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("购买转化分析");

        // 标题
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("购买转化分析报表");
        titleCell.setCellStyle(createTitleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 筛选条件
        Row filterRow = sheet.createRow(1);
        filterRow.createCell(0).setCellValue("时间范围: " + (startDate != null ? startDate : "全部") + " ~ " + (endDate != null ? endDate : "全部"));

        // 表头
        Row headerRow = sheet.createRow(3);
        String[] headers = {"序号", "日期", "进店人数", "购买人数", "转化率(%)"};
        CellStyle headerStyle = createHeaderStyle(wb);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据
        if (dailyData != null) {
            for (int i = 0; i < dailyData.size(); i++) {
                Row row = sheet.createRow(i + 4);
                Map<String, Object> item = dailyData.get(i);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(String.valueOf(item.getOrDefault("period", "")));
                long total = toLong(item.get("total_count"));
                long purchase = toLong(item.get("purchase_count"));
                row.createCell(2).setCellValue(total);
                row.createCell(3).setCellValue(purchase);
                double rate = total > 0 ? Math.round(purchase * 10000.0 / total) / 100.0 : 0;
                row.createCell(4).setCellValue(rate);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 18 * 256);
        }

        return wb;
    }

    @Override
    public Workbook exportDurationExcel(Long storeId, String startDate, String endDate) {
        Map<String, Object> data = analyticsService.duration(storeId, startDate, endDate);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("停留时长分析");

        // 标题
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("停留时长分析报表");
        titleCell.setCellStyle(createTitleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 筛选条件
        Row filterRow = sheet.createRow(1);
        filterRow.createCell(0).setCellValue("时间范围: " + (startDate != null ? startDate : "全部") + " ~ " + (endDate != null ? endDate : "全部"));

        // 汇总
        Row summaryRow = sheet.createRow(2);
        summaryRow.createCell(0).setCellValue("整体平均停留时长: " + data.get("overallAvgDuration") + " 分钟");

        // 表头
        Row headerRow = sheet.createRow(4);
        String[] headers = {"序号", "日期", "进店人数", "平均停留时长(分钟)"};
        CellStyle headerStyle = createHeaderStyle(wb);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 每日数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyData = (List<Map<String, Object>>) data.get("dailyData");
        if (dailyData != null) {
            for (int i = 0; i < dailyData.size(); i++) {
                Row row = sheet.createRow(i + 5);
                Map<String, Object> item = dailyData.get(i);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(String.valueOf(item.getOrDefault("period", "")));
                row.createCell(2).setCellValue(toLong(item.get("total_count")));
                double avg = item.get("avg_duration_min") != null
                        ? ((Number) item.get("avg_duration_min")).doubleValue() : 0;
                row.createCell(3).setCellValue(Math.round(avg * 10.0) / 10.0);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 22 * 256);
        }

        return wb;
    }

    // ==================== PDF 导出 ====================

    @Override
    public byte[] exportDashboardPdf(Long storeId, Map<String, Object> params) {
        try {
            Map<String, Object> dashboard = analyticsService.dashboard(storeId);
            Map<String, Object> summary = (Map<String, Object>) dashboard.get("summary");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);

            // 中文字体 — 优先使用系统绝对路径，确保跨环境可用
            BaseFont bfChinese = null;
            String lastError = "";
            // 按优先级排序：Windows 绝对路径 → Java 字体名
            // 每个候选格式: "路径|编码"，用 | 分隔路径和编码
            String[] fontCandidates = {
                // Windows 绝对路径（最可靠）— 使用 Identity-H 编码（对 ttf/ttc 更兼容）
                "c:/windows/fonts/simsun.ttc,0|Identity-H",
                "c:/windows/fonts/simhei.ttf|Identity-H",
                "c:/windows/fonts/msyh.ttc,0|Identity-H",
                "c:\\windows\\fonts\\simsun.ttc,0|Identity-H",
                "c:\\windows\\fonts\\simhei.ttf|Identity-H",
                "c:\\windows\\fonts\\msyh.ttc,0|Identity-H",
                // Linux 常见路径
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc|Identity-H",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc|Identity-H",
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf|Identity-H",
                // Java 系统字体名（作为最后兜底）— 必须使用 UniGB-UCS2-H
                "SimSun|UniGB-UCS2-H", "SimHei|UniGB-UCS2-H",
                "STSong-Light|UniGB-UCS2-H"
            };
            for (String candidate : fontCandidates) {
                String[] parts = candidate.split("\\|");
                String fontName = parts[0];
                String encoding = parts.length > 1 ? parts[1] : "Identity-H";
                try {
                    bfChinese = BaseFont.createFont(fontName, encoding, BaseFont.EMBEDDED);
                    System.out.println("[PDF] 字体加载成功: " + fontName + " (encoding=" + encoding + ")");
                    break;
                } catch (Exception e) {
                    lastError = fontName + ": " + e.getMessage();
                }
            }
            if (bfChinese == null) {
                System.err.println("[PDF] 所有中文字体加载失败，最后错误: " + lastError);
                // 最终兜底：尝试内嵌 itext-asian 的自带字体
                try {
                    bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                    System.out.println("[PDF] 使用 STSong-Light 兜底成功");
                } catch (Exception e2) {
                    System.err.println("[PDF] 兜底字体也失败: " + e2.getMessage());
                    bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                }
            }
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font headingFont = new Font(bfChinese, 14, Font.BOLD);
            Font normalFont = new Font(bfChinese, 11, Font.NORMAL);
            Font smallFont = new Font(bfChinese, 9, Font.NORMAL);

            document.open();

            // 标题
            Paragraph title = new Paragraph("零售用户行为分析 - 仪表盘报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // 日期
            Paragraph dateLine = new Paragraph("生成时间: " + DATE_FMT.format(new Date()), smallFont);
            dateLine.setAlignment(Element.ALIGN_RIGHT);
            dateLine.setSpacingAfter(20);
            document.add(dateLine);

            // 关键指标表格
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingAfter(20);

            // 表头
            PdfPCell h1 = new PdfPCell(new Paragraph("指标", headingFont));
            PdfPCell h2 = new PdfPCell(new Paragraph("数值", headingFont));
            h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            h1.setPadding(8);
            h2.setPadding(8);
            table.addCell(h1);
            table.addCell(h2);

            // 数据行
            addTableRow(table, "今日进店人数", String.valueOf(summary.getOrDefault("todayVisitors", 0)), normalFont);
            addTableRow(table, "今日购买人数", String.valueOf(summary.getOrDefault("todayPurchases", 0)), normalFont);
            addTableRow(table, "累计进店人数", String.valueOf(summary.getOrDefault("totalVisitors", 0)), normalFont);
            addTableRow(table, "整体转化率", summary.getOrDefault("totalConversionRate", "0") + "%", normalFont);
            addTableRow(table, "平均停留时长(分钟)", String.valueOf(summary.getOrDefault("avgStayDuration", 0)), normalFont);

            document.add(table);

            // 最近7天趋势
            Paragraph trendTitle = new Paragraph("最近7天客流趋势", headingFont);
            trendTitle.setSpacingAfter(10);
            document.add(trendTitle);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recentTrend = (List<Map<String, Object>>) dashboard.get("recentTrend");

            if (recentTrend != null && !recentTrend.isEmpty()) {
                PdfPTable trendTable = new PdfPTable(4);
                trendTable.setWidthPercentage(100);

                String[] trendHeaders = {"日期", "进店人数", "购买人数", "平均停留(分钟)"};
                for (String h : trendHeaders) {
                    PdfPCell cell = new PdfPCell(new Paragraph(h, headingFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(6);
                    trendTable.addCell(cell);
                }

                for (Map<String, Object> item : recentTrend) {
                    trendTable.addCell(new PdfPCell(new Paragraph(
                            String.valueOf(item.getOrDefault("date_key", "")), normalFont)));
                    trendTable.addCell(new PdfPCell(new Paragraph(
                            String.valueOf(item.getOrDefault("total_count", 0)), normalFont)));
                    trendTable.addCell(new PdfPCell(new Paragraph(
                            String.valueOf(item.getOrDefault("purchase_count", 0)), normalFont)));
                    double avg = item.get("avg_duration_min") != null
                            ? ((Number) item.get("avg_duration_min")).doubleValue() : 0;
                    trendTable.addCell(new PdfPCell(new Paragraph(
                            String.valueOf(Math.round(avg * 10.0) / 10.0), normalFont)));
                }

                document.add(trendTable);
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF 生成失败: " + e.getMessage(), e);
        }
    }

    // ==================== 辅助方法 ====================

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private long toLong(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0; }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Paragraph(label, font));
        PdfPCell c2 = new PdfPCell(new Paragraph(value, font));
        c1.setPadding(6);
        c2.setPadding(6);
        table.addCell(c1);
        table.addCell(c2);
    }
}
