package com.retail.controller;

import com.retail.interceptor.RequireRole;
import com.retail.service.ExportService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * 导出统计报表 Excel
     * type: traffic / conversion / duration
     */
    @GetMapping("/report")
    @RequireRole({"admin", "manager", "analyst"})
    public void exportReport(
            @RequestParam String type,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String granularity,
            HttpServletResponse response) throws Exception {

        Workbook workbook;
        String fileName;

        switch (type) {
            case "conversion":
                workbook = exportService.exportConversionExcel(storeId, startDate, endDate);
                fileName = "购买转化分析报表.xlsx";
                break;
            case "duration":
                workbook = exportService.exportDurationExcel(storeId, startDate, endDate);
                fileName = "停留时长分析报表.xlsx";
                break;
            case "traffic":
            default:
                workbook = exportService.exportTrafficExcel(storeId, startDate, endDate, granularity);
                fileName = "客流统计报表.xlsx";
                break;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 导出仪表盘 PDF
     */
    @GetMapping("/dashboard")
    @RequireRole({"admin", "manager", "analyst"})
    public void exportDashboardPdf(
            @RequestParam(required = false) Long storeId,
            HttpServletResponse response) throws Exception {

        byte[] pdfBytes = exportService.exportDashboardPdf(storeId, null);

        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("仪表盘报表.pdf", "UTF-8"));
        response.setContentLength(pdfBytes.length);

        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}
