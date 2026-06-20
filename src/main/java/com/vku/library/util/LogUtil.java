package com.vku.library.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LogUtil {
    // Chế độ 'true' trong FileWriter giúp ghi nối tiếp (append) vào cuối file
    public static void writeLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("library_action.log", true))) {
            writer.write("[" + LocalDateTime.now() + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Lỗi ghi log: " + e.getMessage());
        }
    }
}