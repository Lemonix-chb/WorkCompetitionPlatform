package com.example.workcompetitionplatform.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 文件操作工具类
 *
 * @author 陈海波
 * @since 2026-04-13
 */
public class FileUtils {

    public static List<String> readFileLines(File file, int maxLines) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && (maxLines == -1 || count < maxLines)) {
                lines.add(line);
                count++;
            }
        }
        return lines;
    }

    public static String readFileContent(File file, int maxLines) throws IOException {
        StringBuilder content = new StringBuilder();
        List<String> lines = readFileLines(file, maxLines);
        for (String line : lines) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    public static File ensureDirectoryExists(String path) throws IOException {
        Path directory = Path.of(path);
        Files.createDirectories(directory);
        return directory.toFile();
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";
        return filename.substring(lastDot + 1);
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (directory == null || !directory.exists()) return;
        try (var walk = Files.walk(directory.toPath())) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.delete(path); } catch (IOException e) { }
            });
        }
    }
}