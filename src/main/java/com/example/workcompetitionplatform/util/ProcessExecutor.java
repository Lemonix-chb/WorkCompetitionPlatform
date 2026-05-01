package com.example.workcompetitionplatform.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 进程执行工具类（带超时保护）
 *
 * @author 陈海波
 * @since 2026-04-13
 */
public class ProcessExecutor {

    public static ProcessResult execute(List<String> command, int timeoutSeconds) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            throw new IOException("Process timed out after " + timeoutSeconds + " seconds");
        }

        return new ProcessResult(process.exitValue(), output.toString());
    }

    public static class ProcessResult {
        private final int exitCode;
        private final String output;

        public ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() { return exitCode; }
        public String getOutput() { return output; }
        public boolean isSuccess() { return exitCode == 0; }
    }
}