package com.retail.service;

import org.apache.poi.ss.usermodel.Workbook;
import java.util.Map;

public interface ExportService {

    /**
     * 导出客流统计 Excel
     */
    Workbook exportTrafficExcel(Long storeId, String startDate, String endDate, String granularity);

    /**
     * 导出购买转化 Excel
     */
    Workbook exportConversionExcel(Long storeId, String startDate, String endDate);

    /**
     * 导出停留时长 Excel
     */
    Workbook exportDurationExcel(Long storeId, String startDate, String endDate);

    /**
     * 导出仪表盘 PDF（返回 PDF 字节数组）
     */
    byte[] exportDashboardPdf(Long storeId, Map<String, Object> params);
}
