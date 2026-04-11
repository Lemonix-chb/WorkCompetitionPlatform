package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.annotation.RateLimit;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.*;
import com.example.workcompetitionplatform.mapper.*;
import com.example.workcompetitionplatform.service.ITeamService;
import com.example.workcompetitionplatform.util.FileValidator;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 文件上传控制器
 * 提供作品附件上传和管理的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    private WorkAttachmentMapper workAttachmentMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private ITeamService teamService;

    /**
     * 上传作品附件
     * 包含完整的文件验证和团队成员权限检查
     * 速率限制：每个用户每分钟最多10次上传请求
     *
     * @param workId 作品ID
     * @param file 上传的文件
     * @param attachmentType 附件类型
     * @return API响应（包含附件信息）
     */
    @Operation(summary = "上传作品附件")
    @PostMapping(value = "/work/{workId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @RateLimit(value = 10, timeout = 60, message = "文件上传请求过于频繁，请等待1分钟后再试")
    public ApiResponse<WorkAttachment> uploadWorkFile(
            @Parameter(description = "作品ID") @PathVariable Long workId,
            @Parameter(description = "上传文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "附件类型") @RequestParam("attachmentType") String attachmentType) {

        try {
            // 验证作品是否存在
            Work work = workMapper.selectById(workId);
            if (work == null) {
                return ApiResponse.notFound("作品不存在");
            }

            // 验证用户权限（必须是团队成员）
            Long userId = UserContext.getCurrentUserId();
            if (!teamService.isUserInTeam(work.getTeamId(), userId)) {
                log.warn("用户 {} 尝试上传文件到作品 {} 但不是团队成员", userId, workId);
                return ApiResponse.forbidden("只有团队成员可以上传文件");
            }

            // 验证文件不为空
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }

            // 获取文件信息
            String originalFileName = file.getOriginalFilename();
            long fileSize = file.getSize();
            String mimeType = file.getContentType();

            // 验证文件大小
            if (!FileValidator.isValidFileSize(fileSize)) {
                return ApiResponse.error("文件大小超出限制（最大300MB）");
            }

            // 验证文件类型
            if (!FileValidator.isValidFileType(originalFileName, attachmentType)) {
                String allowed = FileValidator.getAllowedExtensions(attachmentType);
                return ApiResponse.error(
                        String.format("文件类型不允许。%s 允许的格式：%s",
                                FileValidator.getFileTypeDescription(attachmentType), allowed));
            }

            // 清理原始文件名（防止路径注入）
            String sanitizedFileName = FileValidator.sanitizeFileName(originalFileName);

            // 生成规范的存储文件名
            String fileExtension = "." + FileValidator.getFileExtension(sanitizedFileName);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String teamCode = getTeamCode(work.getTeamId());

            String standardizedFileName = String.format("%s_%s_v%d_%s_%s%s",
                    work.getWorkCode(),
                    teamCode,
                    work.getCurrentVersion(),
                    attachmentType,
                    timestamp,
                    fileExtension);

            // 创建规范的上传目录
            String year = String.valueOf(LocalDateTime.now().getYear());
            String competitionName = sanitizeDirectoryName(getCompetitionName(work.getCompetitionId()));
            String trackType = work.getWorkType().name();

            String workUploadDir = String.format("%s/works/%s/%s/%s/%s",
                    uploadDir, year, competitionName, trackType, work.getWorkCode());

            Path uploadPath = Paths.get(workUploadDir);
            Files.createDirectories(uploadPath);

            // 保存文件
            String filePath = workUploadDir + "/" + standardizedFileName;
            file.transferTo(new File(filePath));

            // 创建附件记录
            WorkAttachment attachment = new WorkAttachment();
            attachment.setWorkId(workId);
            attachment.setFileName(originalFileName);
            attachment.setFilePath(filePath);
            attachment.setFileSize(fileSize);
            attachment.setMimeType(mimeType);
            attachment.setAttachmentType(WorkAttachment.AttachmentType.valueOf(attachmentType));
            attachment.setUploaderId(userId);
            attachment.setUploadTime(LocalDateTime.now());
            attachment.setVersion(work.getCurrentVersion());

            workAttachmentMapper.insert(attachment);

            log.info("文件上传成功：作品 {}, 用户 {}, 文件 {} -> {}",
                    work.getWorkCode(), userId, originalFileName, standardizedFileName);

            return ApiResponse.success("文件上传成功", attachment);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("无效的附件类型", e);
            return ApiResponse.error("无效的附件类型");
        } catch (Exception e) {
            log.error("文件上传发生未知错误", e);
            return ApiResponse.error("上传失败，请稍后重试");
        }
    }

    /**
     * 查询作品附件列表
     *
     * @param workId 作品ID
     * @return API响应（包含附件列表）
     */
    @Operation(summary = "查询作品附件")
    @GetMapping("/work/{workId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkAttachment>> listWorkAttachments(@Parameter(description = "作品ID") @PathVariable Long workId) {
        List<WorkAttachment> attachments = workAttachmentMapper.selectByWorkId(workId);
        return ApiResponse.success(attachments);
    }

    /**
     * 删除作品附件
     *
     * @param attachmentId 附件ID
     * @return API响应
     */
    @Operation(summary = "删除作品附件")
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteAttachment(@Parameter(description = "附件ID") @PathVariable Long attachmentId) {
        try {
            WorkAttachment attachment = workAttachmentMapper.selectById(attachmentId);
            if (attachment == null) {
                return ApiResponse.notFound("附件不存在");
            }

            // 删除文件
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                file.delete();
            }

            // 删除数据库记录
            workAttachmentMapper.deleteById(attachmentId);

            log.info("附件删除成功：附件ID {}", attachmentId);

            return ApiResponse.success("附件已删除");
        } catch (Exception e) {
            log.error("删除附件失败", e);
            return ApiResponse.error("删除附件失败");
        }
    }

    /**
     * 获取团队编号
     */
    private String getTeamCode(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        return team != null ? team.getTeamCode() : "UNKNOWN";
    }

    /**
     * 获取赛事名称（简化版，去除特殊字符）
     */
    private String getCompetitionName(Long competitionId) {
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            return "UNKNOWN";
        }
        return competition.getCompetitionName();
    }

    /**
     * 清理目录名称中的危险字符
     */
    private String sanitizeDirectoryName(String name) {
        if (name == null || name.isEmpty()) {
            return "UNKNOWN";
        }
        return name.replaceAll("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    /**
     * 下载作品附件
     * 评委、管理员和团队成员可以下载作品附件
     *
     * @param attachmentId 附件ID
     * @return 文件资源
     */
    @Operation(summary = "下载作品附件")
    @GetMapping("/download/{attachmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadAttachment(@Parameter(description = "附件ID") @PathVariable Long attachmentId) {
        try {
            // 查询附件信息
            WorkAttachment attachment = workAttachmentMapper.selectById(attachmentId);
            if (attachment == null) {
                log.warn("附件不存在：{}", attachmentId);
                return ResponseEntity.notFound().build();
            }

            // 查询作品信息
            Work work = workMapper.selectById(attachment.getWorkId());
            if (work == null) {
                log.warn("作品不存在：{}", attachment.getWorkId());
                return ResponseEntity.notFound().build();
            }

            // 验证下载权限（评委、管理员或团队成员）
            Long userId = UserContext.getCurrentUserId();
            User.UserRole userRole = UserContext.getCurrentUserRole();

            boolean canDownload = false;

            // 管理员可以下载所有作品
            if (userRole == User.UserRole.ADMIN) {
                canDownload = true;
            }
            // 评委可以下载已提交的作品
            else if (userRole == User.UserRole.JUDGE && work.getDevelopmentStatus() == Work.DevelopmentStatus.SUBMITTED) {
                canDownload = true;
            }
            // 团队成员可以下载自己团队的作品
            else if (teamService.isUserInTeam(work.getTeamId(), userId)) {
                canDownload = true;
            }

            if (!canDownload) {
                log.warn("用户 {} 无权限下载附件 {}", userId, attachmentId);
                return ResponseEntity.status(403).build();
            }

            // 加载文件资源
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("文件不存在或无法读取：{}", attachment.getFilePath());
                return ResponseEntity.notFound().build();
            }

            // 设置响应头
            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + attachment.getFileName() + "\"";

            log.info("文件下载成功：用户 {} -> 附件 {}", userId, attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);
        } catch (IOException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("文件下载发生未知错误", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}