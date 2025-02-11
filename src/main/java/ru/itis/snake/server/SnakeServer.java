package main.java.ru.itis.snake.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SnakeServer {
    private static final int PORT = 1234;
    private static final List<Handler> waitingPlayers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            Handler handler = new Handler(socket);
            new Thread(handler).start();

            synchronized (SnakeServer.class) {
                if (waitingPlayers.size() == 3) {
                    waitingPlayers.add(handler);

                    final List<Handler> sessionPlayers = new ArrayList<>(waitingPlayers);

                    waitingPlayers.clear();

                    new Thread(() -> {
                        try {
                            for (int countdown = 10; countdown > 0; countdown--) {
                                final int timeLeft = countdown;
                                sessionPlayers.forEach(h ->
                                        h.send("STATUS: Игра начнется через " + timeLeft + " секунд"));
                                Thread.sleep(1000);
                            }
                            sessionPlayers.forEach(h
                                    -> h.send("STATUS: Игра началась"));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        GameSession gameSession = new GameSession(sessionPlayers);
                        sessionPlayers.forEach(h -> h.setGameSession(gameSession));
                        new Thread(gameSession).start();
                    }).start();
                } else {
                    waitingPlayers.add(handler);
                    waitingPlayers.forEach(h ->
                            h.send("STATUS: Ожидание " + (4 - waitingPlayers.size()) + " игроков")
                    );
                }
            }
        }
    }
}
