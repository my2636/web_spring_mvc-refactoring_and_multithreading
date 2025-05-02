package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Необходимо:
Выделить класс Server с методами для:

- запуска;
- обработки конкретного подключения.
Реализовать обработку подключений с помощью ThreadPool — выделите фиксированный на 64 потока,
 и каждое подключение обрабатывайте в потоке из пула.
* */

public class Server {
    private final static int PORT = 9999;
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private final static List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    static void start() throws IOException {
        try (final var serverSocket = new ServerSocket(PORT)) {
            while (true) {
                final var socket = serverSocket.accept();
                System.out.println("Новое соединение");
                threadPool.submit(() -> {
                    getConnection(socket);
                });
            }
        }
    }

    private static void getConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            processConnection(in, out);
        } catch (IOException e) {
            System.err.println("Ошибка обработки подключения: " + e.getMessage());
        }
    }

    private static void processConnection(BufferedReader in, BufferedOutputStream out) {
        try {
            final String requestLine = in.readLine();

            if (requestLine == null) {
                sendErrorResponse(out);
                return;
            }
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                sendErrorResponse(out);
                return;
            }

            final var path = parts[1];
            if (!isValidPath(path)) {
                sendErrorResponse(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            if (!Files.exists(filePath)) {
                sendErrorResponse(out);
                return;
            }
            if (path.equals("/classic.html")) {
                sendClassicResponse(filePath, out);
            } else {
                sendFileResponse(filePath, out);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isValidPath(String path) {
        return VALID_PATHS.contains(path);
    }

    private static void sendClassicResponse(Path filePath, BufferedOutputStream out) throws IOException {
        final String template = Files.readString(filePath);
        final var mimeType = Files.probeContentType(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private static void sendFileResponse(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    private static void sendErrorResponse(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}