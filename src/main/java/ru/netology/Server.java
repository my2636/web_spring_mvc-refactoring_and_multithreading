package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
Необходимо:
Выделить класс Server с методами для:

- запуска;
- обработки конкретного подключения.
Реализовать обработку подключений с помощью ThreadPool — выделите фиксированный на 64 потока, и каждое подключение обрабатывайте в потоке из пула.
* */

public class Server {
    private final static int PORT = 9999;
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public static void main(String[] args) {
        start();
    }

    static void start() {
        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка запуска: " + e.getMessage());
        }
    }



    static String processConnection(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void sendResponse() {

    }

    static void off() {

    }
}
