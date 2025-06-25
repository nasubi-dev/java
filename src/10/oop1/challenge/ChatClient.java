package oop1.challenge;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread readerThread;
    private volatile boolean running = true;

    public static void main(String[] args) {
        new ChatClient().start();
    }

    public void start() {
        try {
            // サーバーに接続
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("=== Chat Client ===");
            System.out.println("Connected to chat server at " + SERVER_HOST + ":" + SERVER_PORT);
            System.out.println("Type 'HELP' for available commands or 'QUIT' to exit.");
            System.out.println();
            
            // サーバーからのメッセージを受信するスレッド
            startReaderThread();
            
            // ユーザー入力を処理
            handleUserInput();
            
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.err.println("Make sure the ChatServer is running on " + SERVER_HOST + ":" + SERVER_PORT);
        } finally {
            cleanup();
        }
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                String serverMessage;
                while (running && (serverMessage = reader.readLine()) != null) {
                    // プロンプトでない場合は改行を入れて表示
                    if (!serverMessage.equals("> ")) {
                        // メッセージの種類に応じて色分け表示
                        if (serverMessage.startsWith("100 INFO BROADCAST_MESSAGE")) {
                            System.out.println("[BROADCAST] " + serverMessage.substring("100 INFO BROADCAST_MESSAGE ".length()));
                        } else if (serverMessage.startsWith("100 INFO GROUP_MESSAGE")) {
                            String content = serverMessage.substring("100 INFO GROUP_MESSAGE ".length());
                            System.out.println("[GROUP] " + content);
                        } else if (serverMessage.startsWith("200 OK")) {
                            System.out.println("✓ " + serverMessage.substring("200 OK ".length()));
                        } else if (serverMessage.contains("ERROR")) {
                            System.out.println("✗ " + serverMessage);
                        } else {
                            System.out.println(serverMessage);
                        }
                    } else {
                        System.out.print(serverMessage);
                        System.out.flush();
                    }
                }
            } catch (IOException e) {
                if (running && !socket.isClosed()) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        
        while (running) {
            try {
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                // QUITコマンドの場合は終了
                if (input.equalsIgnoreCase("QUIT")) {
                    running = false;
                    writer.println(input);
                    break;
                }
                
                // サーバーにコマンドを送信
                writer.println(input);
                
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error reading input: " + e.getMessage());
                }
                break;
            }
        }
        
        scanner.close();
    }

    private void cleanup() {
        running = false;
        try {
            if (readerThread != null) {
                readerThread.interrupt();
            }
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
