package main.java.ru.itis.snake.server;

import javafx.scene.paint.Color;
import main.java.ru.itis.snake.game.Point;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public Map<String, SnakeState> snakes = new ConcurrentHashMap<>();
    public Point apple = new Point(0,0);
    public Point badFood = new Point(0,0);
    public Point obstacle = new Point(0,0);

    public void updateFromString(String state) {
        String[] parts = state.split(";");
        Arrays.stream(parts).forEach(p -> {
            String[] data = p.split(":");
            if (data[0].equals("APPLE")) {
                apple = new Point(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
            } else if (data[0].equals("BADFOOD")) {
                badFood = new Point(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
            } else if (data[0].equals("OBSTACLE")) {
                obstacle = new Point(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
            } else if (data[0].equals("PLAYER")) {
                String playerId = data[1];
                Color color = Color.web(data[2]);
                int score = Integer.parseInt(data[3]);
                snakes.put(playerId, new SnakeState(data));
            }
        });
    }

    public Map<String, SnakeState> getSnakes() { return snakes; }
}