package main.java.ru.itis.snake.client;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import main.java.ru.itis.snake.server.GameState;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class SnakeClient extends Application {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1234;
    private boolean gameOver = false;

    private String username;
    private final UUID uuid = UUID.randomUUID();
    private Color playerColor;

    private GameState gameState = new GameState();
    private Canvas canvas = new Canvas(640, 480);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private Stage stage;

    private String statusMessage = "";
    private boolean gameStarted = false;
    private long gameStartTimestamp = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Имя игрока");
        dialog.setHeaderText("Введите имя игрока:");
        dialog.setContentText("Имя:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            username = result.get().trim();
        } else {
            Platform.exit();
            return;
        }

        connectToServer(primaryStage);
    }

    private void connectToServer(Stage stage) {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
        this.stage = stage;
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
        stage.setTitle("Змейка");
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
                Platform.runLater(() -> {
                    gameState.updateFromString(parts[1]);
                    if (!gameStarted) {
                        gameStarted = true;
                        gameStartTimestamp = System.currentTimeMillis();
                    }
                });
                break;
            case "STATUS":
                Platform.runLater(() -> {
                    statusMessage = parts[1].trim();
                    if ("Игра началась".equals(statusMessage)) {
                        javafx.animation.PauseTransition pause =
                                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                        pause.setOnFinished(e -> statusMessage = "");
                        pause.play();
                    }
                });
                break;
            case "SCORE":
                Platform.runLater(() ->
                        stage.setTitle("Змейка - Счет: " + parts[1]));
                break;
            case "WINNER":
                gameOver = true;
                Platform.runLater(() -> {
                    gc.setFill(Color.WHITE);
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.setFont(new Font(24));
                    gc.fillText("Победитель: " + parts[1], canvas.getWidth() / 2, canvas.getHeight() / 2);
                });
                break;
        }
    }

    private void render() {
        if (gameOver) return;

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (gameState != null) {
            gameState.getSnakes().forEach((id, snake) -> {
                gc.setFill(snake.color);
                snake.body.forEach(p ->
                        gc.fillRect(p.x, p.y, 19, 19));
            });

            gc.setFill(Color.RED);
            gc.fillOval(gameState.apple.x, gameState.apple.y, 20, 20);

            gc.setFill(Color.ORANGE);
            gc.fillRect(gameState.badFood.x, gameState.badFood.y, 20, 20);

            gc.setFill(Color.GRAY);
            gc.fillRoundRect(gameState.obstacle.x, gameState.obstacle.y, 20, 20, 10, 10);
        }

        if (!statusMessage.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(36));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(statusMessage, canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
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
