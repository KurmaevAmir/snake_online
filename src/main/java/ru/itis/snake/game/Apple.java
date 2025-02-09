package main.java.ru.itis.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

public class Apple extends GameObject {
    public Apple() {
        super(0, 0, Color.RED);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x, y, Game.GRID_SIZE, Game.GRID_SIZE);
    }
}
