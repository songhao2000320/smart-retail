package com.retail.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.retail.dto.ApiResponse;
import com.retail.entity.CustomerBehavior;
import com.retail.entity.ZoneStay;
import com.retail.interceptor.RequireRole;
import com.retail.service.BehaviorService;
import com.retail.util.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/behaviors")
public class BehaviorController {

    @Autowired
    private BehaviorService behaviorService;

    /** 分页查询 */
    @GetMapping
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer isPurchased,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {

        Map<String, Object> params = new java.util.HashMap<>();
        if (storeId != null) params.put("storeId", storeId);
        if (isPurchased != null) params.put("isPurchased", isPurchased);
        if (startDate != null && !startDate.isEmpty()) params.put("startDate", startDate);
        if (endDate != null && !endDate.isEmpty()) params.put("endDate", endDate);
        if (keyword != null && !keyword.isEmpty()) params.put("keyword", keyword);

        IPage<CustomerBehavior> result = behaviorService.page(page, size, params);
        return ApiResponse.page(result.getRecords(), result.getTotal(), page, size);
    }

    /** 获取详情 */
    @GetMapping("/{id}")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse detail(@PathVariable Long id) {
        CustomerBehavior behavior = behaviorService.getById(id);
        if (behavior == null) {
            return ApiResponse.error("记录不存在");
        }
        List<ZoneStay> zoneStays = behaviorService.getZoneStays(id);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("behavior", behavior);
        result.put("zoneStays", zoneStays);
        return ApiResponse.success(result);
    }

    /** 创建 */
    @PostMapping
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse create(@RequestBody Map<String, Object> body) {
        CustomerBehavior behavior = new CustomerBehavior();

        if (body.get("storeId") != null) {
            behavior.setStoreId(Long.valueOf(body.get("storeId").toString()));
        }
        if (body.get("isPurchased") != null) {
            behavior.setIsPurchased(Integer.valueOf(body.get("isPurchased").toString()));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            if (body.get("entryTime") != null) {
                behavior.setEntryTime(LocalDateTime.parse(body.get("entryTime").toString(), fmt));
            }
            if (body.get("leaveTime") != null) {
                behavior.setLeaveTime(LocalDateTime.parse(body.get("leaveTime").toString(), fmt));
            }
        } catch (Exception e) {
            return ApiResponse.error("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> zoneStays = (List<Map<String, Object>>) body.get("zoneStays");
        return behaviorService.create(behavior, zoneStays);
    }

    /** 更新 */
    @PutMapping("/{id}")
    @RequireRole({"admin", "manager"})
    public ApiResponse update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        CustomerBehavior behavior = new CustomerBehavior();
        behavior.setId(id);

        if (body.get("storeId") != null) {
            behavior.setStoreId(Long.valueOf(body.get("storeId").toString()));
        }
        if (body.get("isPurchased") != null) {
            behavior.setIsPurchased(Integer.valueOf(body.get("isPurchased").toString()));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            if (body.get("entryTime") != null) {
                behavior.setEntryTime(LocalDateTime.parse(body.get("entryTime").toString(), fmt));
            }
            if (body.get("leaveTime") != null) {
                behavior.setLeaveTime(LocalDateTime.parse(body.get("leaveTime").toString(), fmt));
            }
        } catch (Exception e) {
            return ApiResponse.error("时间格式错误");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> zoneStays = (List<Map<String, Object>>) body.get("zoneStays");
        return behaviorService.update(behavior, zoneStays);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    @RequireRole({"admin", "manager"})
    public ApiResponse delete(@PathVariable Long id) {
        return behaviorService.delete(id);
    }

    /** Excel 批量导入 */
    @PostMapping("/import")
    @RequireRole({"admin", "manager"})
    public ApiResponse importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ApiResponse.error("仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }
        try {
            List<Map<String, Object>> records = ExcelUtil.parseBehaviorExcel(file.getInputStream());
            return behaviorService.importExcel(records);
        } catch (Exception e) {
            return ApiResponse.error("导入失败: " + e.getMessage());
        }
    }

    /** 下载导入模板 */
    @GetMapping("/template")
    @RequireRole({"admin", "manager"})
    public void downloadTemplate(HttpServletResponse response) {
        try {
            Workbook workbook = ExcelUtil.createTemplate();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("行为数据导入模板.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("生成模板失败", e);
        }
    }
}
