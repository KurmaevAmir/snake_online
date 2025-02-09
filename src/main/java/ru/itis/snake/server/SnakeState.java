package main.java.ru.itis.snake.server;

import javafx.scene.paint.Color;
import main.java.ru.itis.snake.game.Point;

import java.util.ArrayList;
import java.util.List;

public class SnakeState {
    public Color color;
    public List<Point> body = new ArrayList<>();

    public SnakeState(String[] data) {
        this.color = Color.valueOf(data[1]);
        for (int i=2; i<data.length; i+=2) {
            body.add(new Point(
                    Integer.parseInt(data[i]),
                    Integer.parseInt(data[i+1])
            ));
        }
    }
}
