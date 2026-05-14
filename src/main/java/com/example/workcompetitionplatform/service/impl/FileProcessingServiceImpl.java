package com.example.workcompetitionplatform.service.impl;

import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.service.FileProcessingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件处理服务实现类
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.temp-dir:temp}")
    private String tempDir;

    @Override
    public File unzipSubmission(Long submissionId, String zipPath) {
        try {
            // 创建临时目录
            String tempPath = getTempDirPath(submissionId);
            Path tempDirectory = Paths.get(tempPath);
            Files.createDirectories(tempDirectory);

            // 解压ZIP文件
            File zipFile = new File(zipPath);
            if (!zipFile.exists()) {
                throw new RuntimeException("ZIP文件不存在: " + zipPath);
            }

            // 使用Java内置ZipInputStream解压（try-with-resources确保资源释放）
            try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry = zipIn.getNextEntry();

                while (entry != null) {
                    File outputFile = new File(tempDirectory.toFile(), entry.getName());

                    if (entry.isDirectory()) {
                        Files.createDirectories(outputFile.toPath());
                    } else {
                        // 确保父目录存在
                        Files.createDirectories(outputFile.getParentFile().toPath());

                        // 写入文件内容（try-with-resources确保输出流关闭）
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = zipIn.read(buffer)) != -1) {
                                bos.write(buffer, 0, read);
                            }
                        }
                    }

                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }

            return tempDirectory.toFile();
        } catch (IOException e) {
            throw new RuntimeException("解压ZIP文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateFileType(String filename, Work.WorkType workType) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // 获取文件扩展名
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return false;
        }

        String extension = filename.substring(lastDot + 1);
        return workType.isAllowedExtension(extension);
    }

    @Override
    public Map<String, List<File>> extractKeyFiles(File unzippedDir, Work.WorkType workType) {
        Map<String, List<File>> keyFiles = new HashMap<>();

        switch (workType) {
            case CODE:
                // 程序设计作品：提取源代码、README、文档
                keyFiles.put("code", findFilesByExtensions(unzippedDir, workType.getAllowedExtensions()));
                keyFiles.put("readme", findFilesByName(unzippedDir, "README", "readme"));
                keyFiles.put("docs", findFilesByExtensions(unzippedDir, Set.of("md", "txt", "doc", "docx")));
                break;

            case PPT:
                // 演示文稿作品：提取PPT/PDF文件
                keyFiles.put("ppt", findFilesByExtensions(unzippedDir, Set.of("pptx", "ppt", "pdf")));
                keyFiles.put("assets", findFilesByExtensions(unzippedDir, Set.of("jpg", "png", "gif", "mp3", "mp4")));
                break;

            case VIDEO:
                // 数媒视频作品：提取视频、剧本、说明
                keyFiles.put("video", findFilesByExtensions(unzippedDir, Set.of("mp4", "avi", "mov", "mkv")));
                keyFiles.put("script", findFilesByExtensions(unzippedDir, Set.of("txt", "doc", "docx")));
                keyFiles.put("assets", findFilesByExtensions(unzippedDir, Set.of("jpg", "png")));
                break;
        }

        return keyFiles;
    }

    @Override
    public void cleanupTempFiles(Long submissionId) {
        String tempPath = getTempDirPath(submissionId);
        Path tempDirectory = Paths.get(tempPath);

        if (Files.exists(tempDirectory)) {
            try {
                // 递归删除目录及其内容
                Files.walk(tempDirectory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // 忽略删除失败
                        }
                    });
            } catch (IOException e) {
                // 忽略清理失败
            }
        }
    }

    @Override
    public String getTempDirPath(Long submissionId) {
        return Paths.get(uploadDir, tempDir, "submission_" + submissionId).toString();
    }

    /**
     * 查找指定扩展名的文件
     */
    private List<File> findFilesByExtensions(File directory, Set<String> extensions) {
        List<File> files = new ArrayList<>();
        if (!directory.exists() || !directory.isDirectory()) {
            return files;
        }

        walkDirectory(directory, file -> {
            String name = file.getName();
            int lastDot = name.lastIndexOf('.');
            if (lastDot != -1 && lastDot != name.length() - 1) {
                String ext = name.substring(lastDot + 1).toLowerCase();
                if (extensions.contains(ext)) {
                    files.add(file);
                }
            }
        });

        return files;
    }

    /**
     * 查找指定名称的文件
     */
    private List<File> findFilesByName(File directory, String... names) {
        List<File> files = new ArrayList<>();
        if (!directory.exists() || !directory.isDirectory()) {
            return files;
        }

        walkDirectory(directory, file -> {
            String fileName = file.getName().toLowerCase();
            for (String targetName : names) {
                if (fileName.contains(targetName.toLowerCase())) {
                    files.add(file);
                    break;
                }
            }
        });

        return files;
    }

    /**
     * 递归遍历目录
     */
    private void walkDirectory(File directory, java.util.function.Consumer<File> consumer) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                walkDirectory(file, consumer);
            } else {
                consumer.accept(file);
            }
        }
    }
}