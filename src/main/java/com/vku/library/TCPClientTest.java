package com.vku.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPClientTest {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Đã kết nối tới Server Thư Viện!");
            System.out.println("Các lệnh hỗ trợ: PING, GET_ALL_BOOKS, EXIT");

            while (true) {
                System.out.print("Nhập lệnh gửi Server: ");
                String command = scanner.nextLine();

                // Gửi qua mạng
                out.println(command);

                // Đọc phản hồi
                String response = in.readLine();
                System.out.println("Server trả lời: " + response);

                if (command.equals("EXIT")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể kết nối. Đảm bảo AppLauncher của thư viện đang chạy. Lỗi: " + e.getMessage());
        }
    }
}