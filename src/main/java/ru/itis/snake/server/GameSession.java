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

    public GameSession(List<Handler> players) {
        this.players = players;
        initGame();
    }

    private void initGame() {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        Random rand = new Random();

        for (int i=0; i<players.size(); i++) {
            Handler h = players.get(i);
            Snake s = new Snake();
            s.setColor(colors[i]);
            s.setPosition(rand.nextInt(32)*GRID_SIZE, rand.nextInt(24)*GRID_SIZE);
            snakes.put(h.getIdentifier(), s);
            h.send("COLOR:" + colors[i]);
        }
        spawnApple();
    }

    private void spawnApple() {
        Random rand = new Random();
        Set<Point> occupied = new HashSet<>();

        snakes.values().forEach(s -> occupied.addAll(s.body));

        do {
            // Создаем новый объект Point вместо изменения существующего
            apple = new Point(
                    rand.nextInt(32) * GRID_SIZE,
                    rand.nextInt(24) * GRID_SIZE
            );
        } while (occupied.contains(apple));
    }

    @Override
    public void run() {
        while (snakes.size() > 1) {
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
            // Добавляем проверку границ
            Point head = snake.body.get(0);
            if(head.x < 0) head.x = Game.SCREEN_WIDTH - Game.GRID_SIZE;
            if(head.x >= Game.SCREEN_WIDTH) head.x = 0;
            if(head.y < 0) head.y = Game.SCREEN_HEIGHT - Game.GRID_SIZE;
            if(head.y >= Game.SCREEN_HEIGHT) head.y = 0;
        });
    }

    private void broadcastState() {
        if (apple == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("APPLE:").append(apple.x).append(":").append(apple.y).append(";");

        snakes.forEach((id, s) -> {
            sb.append(id).append(":").append(s.color.toString()).append(":");
            for(Point p : s.body) {
                sb.append(p.x).append(":").append(p.y).append(":");
            }
            if(!s.body.isEmpty()) sb.deleteCharAt(sb.length()-1);
            sb.append(";");
        });

        players.forEach(h -> h.send("STATE:" + sb));
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

        // Проверка столкновений с другими змейками
        snakes.forEach((id, snake) -> {
            Point head = snake.body.get(0);

            // Столкновение с другими змейками
            snakes.forEach((otherId, otherSnake) -> {
                if (!id.equals(otherId) && otherSnake.body.stream().anyMatch(p -> p.equals(head))) {
                    toRemove.add(id);
                }
            });

            // Столкновение с яблоком
            if (head.equals(apple)) {
                snake.grow();
                spawnApple();
            }
        });

        // Удаление проигравших
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
        String winner = snakes.keySet().iterator().next();
        players.forEach(h -> h.send("WINNER:" + winner));
    }
}
