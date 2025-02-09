package main.java.ru.itis.snake.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Handler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private String identifier;
    private String username;

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

    @Override
    public void run() {
        try {
            String message;

            broadcast(username + "#" + identifier + " присоединился к игре");

            while (socket.isConnected()) {
                message = reader.readLine();
                if (message == null) {
                    closeEverything(socket, reader, writer);
                    return;
                }

                System.out.println("MESSAGE: " + message);

                // Логика сообщений пока стоит временная заглушка
                broadcast(message);
            }
        } catch (Exception e) {
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

    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
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
