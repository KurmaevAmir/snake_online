package main.java.ru.itis.snake.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SnakeServer {
    private static final int PORT = 1234;
    private static List<Handler> waitingPlayers = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            Handler handler = new Handler(socket);
            new Thread(handler).start();

            if (waitingPlayers.size() >= 3) {
                List<Handler> sessionPlayers = new ArrayList<>(waitingPlayers);
                sessionPlayers.add(handler);
                GameSession gameSession = new GameSession(sessionPlayers);
                sessionPlayers.forEach(h -> h.setGameSession(gameSession));
                new Thread(gameSession).start();
                waitingPlayers.clear();
            } else {
                waitingPlayers.add(handler);
            }
        }
    }
}
