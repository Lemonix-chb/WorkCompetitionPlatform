package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 系统控制器
 * 提供系统级别的接口
 *
 * @author 陈海波
 * @since 2026-05-01
 */
@Tag(name = "系统管理")
@RestController
@RequestMapping("/api/system")
public class SystemController {

    /**
     * 获取服务器当前时间
     * 用于前端同步时间倒计时
     *
     * @return API响应（包含服务器时间）
     */
    @Operation(summary = "获取服务器时间")
    @GetMapping("/time")
    public ApiResponse<LocalDateTime> getServerTime() {
        return ApiResponse.success(LocalDateTime.now());
    }
}