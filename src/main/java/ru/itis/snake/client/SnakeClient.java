package main.java.ru.itis.snake.client;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import main.java.ru.itis.snake.server.GameState;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

import static com.sun.scenario.effect.impl.prism.PrEffectHelper.render;

public class SnakeClient extends Application {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1234;

    private String username;
    private final UUID uuid = UUID.randomUUID();
    private Color playerColor;

    private GameState gameState = new GameState();
    private Canvas canvas = new Canvas(640, 480);
    private GraphicsContext gc = canvas.getGraphicsContext2D();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        connectToServer(primaryStage);
    }

    private void connectToServer(Stage stage) {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите имя: ");
            username = scanner.nextLine();

            writer.write(uuid.toString());
            writer.newLine();
            writer.write(username);
            writer.newLine();
            writer.flush();

            new Thread(this::listenForMessages).start();

            setupUI(stage);
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    private void setupUI(Stage stage) {
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            try {
                if (code.isArrowKey() || code == KeyCode.W || code == KeyCode.A
                        || code == KeyCode.S || code == KeyCode.D) {
                    writer.write("INPUT:" + code);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }
        });
        stage.setScene(scene);
        stage.setTitle("Змейка - Ожидание игроков...");
        stage.show();

        new AnimationTimer() {
            public void handle(long now) {
                render();
            }
        }.start();
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split(":", 2);
        switch (parts[0]) {
            case "COLOR":
                playerColor = Color.valueOf(parts[1]);
                break;
            case "STATE":
                gameState.updateFromString(parts[1]);
                break;
            case "WINNER":
                Platform.runLater(() -> {
                    gc.setFill(Color.WHITE);
                    gc.fillText("Победитель: " + parts[1], 300, 240);
                });
                break;
        }
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 640, 480);

        gameState.getSnakes().forEach((id, snake) -> {
            gc.setFill(snake.color);
            snake.body.forEach(p ->
                    gc.fillRect(p.x, p.y, 19, 19));
        });

        gc.setFill(Color.RED);
        gc.fillOval(gameState.apple.x, gameState.apple.y, 20, 20);
    }

    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
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
