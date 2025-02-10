package main.java.ru.itis.snake.server;

import javafx.scene.paint.Color;
import main.java.ru.itis.snake.game.Point;

import java.util.ArrayList;
import java.util.List;

public class SnakeState {
    public Color color;
    public List<Point> body = new ArrayList<>();

    public SnakeState(String[] data) {
        try {
            this.color = Color.web(data[2]); // Берем цвет из data[2]
        } catch (IllegalArgumentException e) {
            this.color = Color.RED;
        }
        // Начинаем с индекса 4 (после id, цвета и счета)
        for (int i = 4; i < data.length; i += 2) {
            body.add(new Point(
                    Integer.parseInt(data[i]),
                    Integer.parseInt(data[i + 1])
            ));
        }
    }
}
