package org.example.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {
    private final String logFileName;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoggerService(String logFileName) {
        this.logFileName = logFileName;
    }

    public synchronized void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logLine = String.format("[%s] [%s] %s", timestamp, level, message);

        // הדפסה ל-Console
        System.out.println(logLine);

        // כתיבה לקובץ הלוג
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            writer.println(logLine);
        } catch (IOException e) {
            System.err.println("Failed to write log to file: " + e.getMessage());
        }
    }
}