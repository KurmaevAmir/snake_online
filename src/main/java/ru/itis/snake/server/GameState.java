package main.java.ru.itis.snake.server;

import main.java.ru.itis.snake.game.Point;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public Map<String, SnakeState> snakes = new ConcurrentHashMap<>();
    public Point apple = new Point(0,0);

    public void updateFromString(String state) {
        String[] parts = state.split(";");
        Arrays.stream(parts).forEach(p -> {
            String[] data = p.split(":");
            if (data[0].equals("APPLE")) {
                apple = new Point(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
            } else {
                snakes.put(data[0], new SnakeState(data));
            }
        });
    }

    public Map<String, SnakeState> getSnakes() { return snakes; }
}
