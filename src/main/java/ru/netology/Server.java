package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/messages.txt", "/messages.html", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/messages");

    private final static ConcurrentHashMap<Map.Entry<String, String>, Handler> handlers = new ConcurrentHashMap<>();

    public void listen(int port) throws IOException {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                System.out.println("Новое соединение");
                threadPool.submit(() -> getConnection(socket));
            }
        }
    }

    private static void getConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            processRequest(in, out);
        } catch (IOException e) {
            System.err.println("Ошибка обработки подключения: " + e.getMessage());
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.put(Map.entry(method, path), handler);
    }

    private static void processRequest(BufferedReader in, BufferedOutputStream out) {
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

            final String method = parts[0];
            final String path = parts[1];
            System.out.println(path);
            if (!isValidPath(path)) {
                sendErrorResponse(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            if (!Files.exists(filePath)) {
                sendErrorResponse(out);
                return;
            }

            Request request = parseRequest(in, method);

            Handler handler = handlers.get(Map.entry(method, path));

            if (handler != null) {
                handler.handle(request, out);
            } else {
                sendErrorResponse(out);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isValidPath(String path) {
        return VALID_PATHS.contains(path);
    }


    private static void sendErrorResponse(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 401 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static Request parseRequest(BufferedReader in, String method) throws IOException {
        // получаем заголовки
        StringBuilder headersBuilder = new StringBuilder();
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            headersBuilder.append(headerLine).append("\r\n");
        }
        String headers = headersBuilder.toString();

        // Content-Length
        int contentLength = 0;
        String contentLengthHeader = headers.lines()
                .filter(h -> h.startsWith("Content-Length:"))
                .findFirst()
                .orElse(null);

        if (contentLengthHeader != null) {
            try {
                contentLength = Integer.parseInt(contentLengthHeader.substring("Content-Length:".length()).trim());
            } catch (NumberFormatException e) {
                System.err.println("Неверный Content-Length заголовок от " + ": " + contentLengthHeader);
                contentLength = 0; // Reset to 0 to prevent issues
            }
        }

        // Body
        String body = null;
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int bytesRead = in.read(buffer, 0, contentLength);
            if (bytesRead != contentLength) {
                System.err.println("Ошибка при чтении тела запроса от " + ": Ожидалось " + contentLength + " байт, прочитано " + bytesRead);
                // We don't send an error response here, instead we try to proceed with the partial body, or an empty body.
                body = (bytesRead > 0) ? new String(buffer, 0, bytesRead) : ""; // create a partial string if data was read.
            } else {
                body = new String(buffer);
            }
        }

        return new Request(method, headers, body);
    }
}