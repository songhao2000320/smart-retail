package com.retail.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Excel 导入工具类
 * 解析行为数据 Excel 文件（支持 xlsx 格式）
 *
 * Excel 模板格式：
 * | 门店ID | 进店时间              | 离开时间              | 是否购买 | 区域ID | 区域进入时间          | 区域离开时间          |
 * | 1      | 2024-01-15 09:30:00  | 2024-01-15 10:45:00  | 1       | 1      | 2024-01-15 09:30:00  | 2024-01-15 09:50:00  |
 * |        |                       |                       |         | 2      | 2024-01-15 09:50:00  | 2024-01-15 10:30:00  |
 */
public class ExcelUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析行为数据 Excel
     * @param inputStream Excel 文件输入流
     * @return 解析后的行为记录列表，每条记录包含基本信息 + zoneStays
     */
    public static List<Map<String, Object>> parseBehaviorExcel(InputStream inputStream) throws Exception {
        List<Map<String, Object>> behaviorList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("Excel 文件中没有找到工作表");
            }

            int rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount < 2) {
                throw new RuntimeException("Excel 文件没有数据行（至少需要标题行+1行数据）");
            }

            // 读取标题行
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colMap = buildColumnMap(headerRow);

            // 逐行读取数据，按 storeId+entryTime+leaveTime 分组
            Map<String, Map<String, Object>> groupMap = new LinkedHashMap<>();

            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                String storeId = getCellString(row, colMap.get("storeId"));
                String entryTime = getCellString(row, colMap.get("entryTime"));
                String leaveTime = getCellString(row, colMap.get("leaveTime"));
                String isPurchased = getCellString(row, colMap.get("isPurchased"));
                String zoneId = getCellString(row, colMap.get("zoneId"));
                String zoneEntry = getCellString(row, colMap.get("zoneEntryTime"));
                String zoneLeave = getCellString(row, colMap.get("zoneLeaveTime"));

                // 校验必填字段
                if (isEmpty(storeId) || isEmpty(entryTime) || isEmpty(leaveTime)) {
                    throw new RuntimeException("第 " + (i + 1) + " 行：门店ID、进店时间、离开时间为必填项");
                }

                // 校验时间格式
                try { FMT.parse(entryTime); } catch (Exception e) { throw new RuntimeException("第 " + (i + 1) + " 行进店时间格式错误，应为 yyyy-MM-dd HH:mm:ss"); }
                try { FMT.parse(leaveTime); } catch (Exception e) { throw new RuntimeException("第 " + (i + 1) + " 行离开时间格式错误，应为 yyyy-MM-dd HH:mm:ss"); }

                String groupKey = storeId + "|" + entryTime + "|" + leaveTime;

                Map<String, Object> behavior = groupMap.get(groupKey);
                if (behavior == null) {
                    behavior = new LinkedHashMap<>();
                    behavior.put("storeId", Long.parseLong(storeId));
                    behavior.put("entryTime", entryTime);
                    behavior.put("leaveTime", leaveTime);
                    behavior.put("isPurchased", isEmpty(isPurchased) ? 0 : Integer.parseInt(isPurchased));
                    behavior.put("zoneStays", new ArrayList<Map<String, Object>>());
                    groupMap.put(groupKey, behavior);
                }

                // 添加区域停留（如果有）
                if (!isEmpty(zoneId)) {
                    Map<String, Object> zoneStay = new LinkedHashMap<>();
                    zoneStay.put("zoneId", Long.parseLong(zoneId));
                    zoneStay.put("entryTime", zoneEntry);
                    zoneStay.put("leaveTime", zoneLeave);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> stays = (List<Map<String, Object>>) behavior.get("zoneStays");
                    stays.add(zoneStay);
                }
            }

            behaviorList.addAll(groupMap.values());
        }

        if (behaviorList.isEmpty()) {
            throw new RuntimeException("Excel 文件中没有有效数据");
        }

        return behaviorList;
    }

    /**
     * 构建列名→列索引的映射（兼容中英文标题）
     */
    private static Map<String, Integer> buildColumnMap(Row headerRow) {
        Map<String, Integer> map = new LinkedHashMap<>();
        Map<String, String> aliasMap = new LinkedHashMap<>();
        aliasMap.put("storeid", "storeId");
        aliasMap.put("门店id", "storeId");
        aliasMap.put("门店", "storeId");
        aliasMap.put("entrytime", "entryTime");
        aliasMap.put("进店时间", "entryTime");
        aliasMap.put("leavetime", "leaveTime");
        aliasMap.put("离开时间", "leaveTime");
        aliasMap.put("ispurchased", "isPurchased");
        aliasMap.put("是否购买", "isPurchased");
        aliasMap.put("zoneid", "zoneId");
        aliasMap.put("区域id", "zoneId");
        aliasMap.put("区域", "zoneId");
        aliasMap.put("zoneentrytime", "zoneEntryTime");
        aliasMap.put("区域进入时间", "zoneEntryTime");
        aliasMap.put("区域进店时间", "zoneEntryTime");
        aliasMap.put("zoneleavetime", "zoneLeaveTime");
        aliasMap.put("区域离开时间", "zoneLeaveTime");

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String header = cell.getStringCellValue().trim().toLowerCase().replaceAll("\\s+", "");
                String mapped = aliasMap.get(header);
                if (mapped != null) {
                    map.putIfAbsent(mapped, i);
                }
            }
        }

        // 如果中文标题未匹配，尝试按英文列名查找
        if (!map.containsKey("storeId") && headerRow.getCell(0) != null) {
            // 尝试按位置回退：标准模板列顺序
            map.put("storeId", 0);
            map.put("entryTime", 1);
            map.put("leaveTime", 2);
            map.put("isPurchased", 3);
            map.put("zoneId", 4);
            map.put("zoneEntryTime", 5);
            map.put("zoneLeaveTime", 6);
        }

        return map;
    }

    private static String getCellString(Row row, Integer colIndex) {
        if (colIndex == null || colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(FMT);
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "1" : "0";
            case FORMULA:
                try { return cell.getStringCellValue().trim(); } catch (Exception e) { return null; }
            default:
                return null;
        }
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * 生成 Excel 导入模板（xlsx 格式）
     */
    public static Workbook createTemplate() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("行为数据导入模板");

        // 标题样式
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // 说明行样式
        CellStyle noteStyle = wb.createCellStyle();
        Font noteFont = wb.createFont();
        noteFont.setColor(IndexedColors.BLUE.getIndex());
        noteFont.setItalic(true);
        noteStyle.setFont(noteFont);

        // 第 1 行：说明
        Row noteRow = sheet.createRow(0);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：同一顾客的一次进店行为可以有多行区域停留记录，门店ID+进店时间+离开时间相同的行会自动合并为一条记录");
        noteCell.setCellStyle(noteStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        // 第 2 行：列标题
        String[] headers = {"门店ID", "进店时间", "离开时间", "是否购买(1是0否)", "区域ID(可选)", "区域进入时间(可选)", "区域离开时间(可选)"};
        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 示例数据
        String[][] sampleData = {
            {"1", "2024-01-15 09:30:00", "2024-01-15 10:45:00", "1", "1", "2024-01-15 09:30:00", "2024-01-15 09:50:00"},
            {"", "", "", "", "2", "2024-01-15 09:50:00", "2024-01-15 10:30:00"},
            {"1", "2024-01-15 14:00:00", "2024-01-15 14:30:00", "0", "", "", ""},
        };

        for (int i = 0; i < sampleData.length; i++) {
            Row dataRow = sheet.createRow(i + 2);
            for (int j = 0; j < sampleData[i].length; j++) {
                dataRow.createCell(j).setCellValue(sampleData[i][j]);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 22 * 256);
        }

        return wb;
    }
}
