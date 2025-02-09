package main.java.ru.itis.snake.server;

import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Handler implements Runnable {
    private volatile boolean isConnected = true;
    private Socket socket;
    private GameSession gameSession;
    private BufferedReader reader;
    private BufferedWriter writer;

    private String identifier;
    public String username;

    private static final List<Handler> handlers = new ArrayList<>();

    public Handler(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.identifier = reader.readLine();
            this.username = reader.readLine();
            handlers.add(this);
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setGameSession(GameSession session) {
        this.gameSession = session;
    }

    private void handlePlayerInput(String message) {
        String dirStr = message.split(":")[1];
        KeyCode keyCode = KeyCode.valueOf(dirStr);

        if(gameSession != null) {
            gameSession.updateSnakeDirection(this.identifier, keyCode);
        }
    }

    public String getIdentifier() { return identifier; }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                String message = reader.readLine();
                if (message == null) break;

                if (message.startsWith("INPUT")) {
                    handlePlayerInput(message);
                }
            }
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    public void send(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    private void broadcast(String message) {
        for (Handler handler : handlers) {
            try {
                if (!handler.identifier.equals(identifier)) {
                    handler.writer.write(message);
                    handler.writer.newLine();
                    handler.writer.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }
        }
    }

    private void removeHanlder() {
        handlers.remove(this);
        broadcast("SERVER: " + username + "#" + identifier + " вышел");
    }

    public void sendScore(int score) {
        send("SCORE:" + score);
    }

    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
        isConnected = false;
        removeHanlder();
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
