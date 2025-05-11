package ru.netology;

import java.io.BufferedOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;


public class Main {

    public static void main(String[] args) throws IOException {
        final var server = new Server();


        server.addHandler("POST", "/messages.html", (request, responseStream) -> {
            String requestBody = request.getRequestBody();
            final var fileToWritePath = "./public/messages.txt";
            final var filePath = Path.of(".", "public", "/messages.html");
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            FileWriter writer = new FileWriter(fileToWritePath, true);
            writer.write(URLDecoder.decode(requestBody, StandardCharsets.UTF_8) + "\n");
            writer.close();
            String responseContent = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            responseStream.write(responseContent.getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();

        });

        server.addHandler("GET", "/messages.txt", (Request request, BufferedOutputStream responseStream) -> {
            try {
                final var filePath = Path.of(".", "public", "messages.txt");
                final var mimeType = "text/plain";
                final var length = Files.size(filePath);

                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());

                Files.copy(filePath, responseStream);
                responseStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("GET", "/classic.html", (Request request, BufferedOutputStream responseStream) -> {

            try {
                final var filePath = Path.of(".", "public", "/classic.html");
                final String template = Files.readString(filePath);
                final var mimeType = Files.probeContentType(filePath);// Retrieve the request body
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                );
                String responseContent = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";

                responseStream.write(responseContent.getBytes());
                responseStream.write(content.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("GET", "/messages.html", (Request request, BufferedOutputStream responseStream) -> {
            try {
                final var filePath = Path.of(".", "public", "/messages.html");
                final var mimeType = Files.probeContentType(filePath);
                final var length = Files.size(filePath);
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, responseStream);
                responseStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.listen(9999);
    }
}