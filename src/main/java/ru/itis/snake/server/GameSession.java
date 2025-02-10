package main.java.ru.itis.snake.server;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import main.java.ru.itis.snake.game.Game;
import main.java.ru.itis.snake.game.Point;
import main.java.ru.itis.snake.game.Snake;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

import static com.sun.javafx.scene.traversal.Direction.UP;
import static javafx.scene.input.KeyCode.W;

public class GameSession implements Runnable {
    private static final int GRID_SIZE = 20;
    private List<Handler> players;
    private Map<String, Snake> snakes = new ConcurrentHashMap<>();
    private Point apple = new Point(0, 0);
    private Point badFood = new Point(0, 0);
    private Point obstacle = new Point(0, 0);
    private Handler handler;
    private Map<String, Integer> scores = new ConcurrentHashMap<>();

    public GameSession(List<Handler> players) {
        this.players = players;
        initGame();
    }

    private void initGame() {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        Random rand = new Random();

        for (int i=0; i<players.size(); i++) {
            this.handler = players.get(i);
            Snake s = new Snake();
            s.setColor(colors[i]);
            s.setPosition(rand.nextInt(32)*GRID_SIZE, rand.nextInt(24)*GRID_SIZE);
            snakes.put(handler.getIdentifier(), s);
            handler.send("COLOR:" + colors[i]);
            scores.put(handler.getIdentifier(), 0);
        }
        spawnAllItems();
    }

    private void spawnAllItems() {
        spawnApple();
        spawnBadFood();
        spawnObstacle();
    }

    private void spawnApple() {
        Random rand = new Random();
        Set<Point> occupied = new HashSet<>();

        snakes.values().forEach(s -> occupied.addAll(s.body));

        do {
            apple = new Point(
                    rand.nextInt(32) * GRID_SIZE,
                    rand.nextInt(24) * GRID_SIZE
            );
        } while (occupied.contains(apple));
    }

    private void spawnBadFood() {
        Random rand = new Random();
        Set<Point> occupied = new HashSet<>();
        snakes.values().forEach(s -> occupied.addAll(s.body));
        occupied.add(apple);
        occupied.add(obstacle);

        do {
            badFood = new Point(
                    rand.nextInt(32) * GRID_SIZE,
                    rand.nextInt(24) * GRID_SIZE
            );
        } while (occupied.contains(badFood));
    }

    private void spawnObstacle() {
        Random rand = new Random();
        Set<Point> occupied = new HashSet<>();
        snakes.values().forEach(s -> occupied.addAll(s.body));
        occupied.add(apple);
        occupied.add(badFood);

        do {
            obstacle = new Point(
                    rand.nextInt(32) * GRID_SIZE,
                    rand.nextInt(24) * GRID_SIZE
            );
        } while (occupied.contains(obstacle));
    }

    @Override
    public void run() {
        while (snakes.size() > 1 && !Thread.currentThread().isInterrupted()) {
            handleDisconnections();
            updateGame();
            broadcastState();
            checkCollisions();
            try { Thread.sleep(100); }
            catch (InterruptedException e) { break; }
        }
        declareWinner();
    }

    private void updateGame() {
        snakes.values().forEach(snake -> {
            snake.move();
            Point head = snake.body.get(0);
            if(head.x < 0) head.x = Game.SCREEN_WIDTH - Game.GRID_SIZE;
            if(head.x >= Game.SCREEN_WIDTH) head.x = 0;
            if(head.y < 0) head.y = Game.SCREEN_HEIGHT - Game.GRID_SIZE;
            if(head.y >= Game.SCREEN_HEIGHT) head.y = 0;
        });
    }

    private void broadcastState() {
        if (players.isEmpty() || snakes.isEmpty()) return;

        StringJoiner sj = new StringJoiner(";");
        sj.add("APPLE:" + apple.x + ":" + apple.y);
        sj.add("BADFOOD:" + badFood.x + ":" + badFood.y);
        sj.add("OBSTACLE:" + obstacle.x + ":" + obstacle.y);

        snakes.forEach((id, s) -> {
            StringBuilder sb = new StringBuilder("PLAYER:")
                    .append(id).append(":")
                    .append(colorToHex(s.color)).append(":")
                    .append(scores.get(id)).append(":");

            for (Point p : s.body) {
                sb.append(p.x).append(":").append(p.y).append(":");
            }
            if (!s.body.isEmpty()) sb.setLength(sb.length() - 1);

            sj.add(sb.toString());
        });

        String state = sj.toString();
        players.forEach(h -> h.send("STATE:" + state));
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public void updateSnakeDirection(String playerId, KeyCode keyCode) {
        Snake snake = snakes.get(playerId);
        if(snake != null) {
            switch(keyCode) {
                case UP: case W:
                    snake.updateDirection(Snake.Direction.UP);
                    break;
                case DOWN: case S:
                    snake.updateDirection(Snake.Direction.DOWN);
                    break;
                case LEFT: case A:
                    snake.updateDirection(Snake.Direction.LEFT);
                    break;
                case RIGHT: case D:
                    snake.updateDirection(Snake.Direction.RIGHT);
                    break;
            }
        }
    }

    private void checkCollisions() {
        Set<String> toRemove = new HashSet<>();

        snakes.forEach((id, snake) -> {
            Point head = snake.body.get(0);

            snakes.forEach((otherId, otherSnake) -> {
                if (!id.equals(otherId) && otherSnake.body.stream().anyMatch(p -> p.equals(head))) {
                    toRemove.add(id);
                }
            });

            // Столкновение с яблоком
            if (head.equals(apple)) {
                snake.grow();
                scores.put(id, snake.getLength() - 1);
                players.stream()
                        .filter(h -> h.getIdentifier().equals(id))
                        .findFirst()
                        .ifPresent(h -> h.sendScore(scores.get(id)));
                spawnApple();
            }

            if (head.equals(badFood)) {
                snake.grow();
                scores.put(id, snake.getLength() - 1);
                players.stream()
                        .filter(h -> h.getIdentifier().equals(id))
                        .findFirst()
                        .ifPresent(h -> h.sendScore(scores.get(id)));
                spawnApple();
            }

            if (head.equals(obstacle)) {
                snake.reset();
                scores.put(id, 0);
                handler.sendScore(0);
                spawnObstacle();
            }
        });

        toRemove.forEach(id -> {
            snakes.remove(id);
            players.removeIf(h -> h.getIdentifier().equals(id));
        });
    }

    private void handleDisconnections() {
        players.removeIf(h -> !h.isConnected());
        snakes.keySet().removeIf(id ->
                players.stream().noneMatch(h -> h.getIdentifier().equals(id))
        );
    }

    private void declareWinner() {
        if (snakes.isEmpty()) return;

        String winnerId = snakes.keySet().iterator().next();
        String winnerName = players.stream()
                .filter(h -> h.getIdentifier().equals(winnerId))
                .findFirst()
                .map(h -> h.username)
                .orElse("Unknown Player");

        players.forEach(h -> h.send("WINNER:" + winnerName));
        try { Thread.sleep(5000); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }
}
