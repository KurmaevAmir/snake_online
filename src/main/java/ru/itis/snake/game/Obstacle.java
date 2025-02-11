package main.java.ru.itis.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Obstacle extends GameObject {
    public Obstacle() {
        super(0, 0, Color.GRAY);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRoundRect(x, y, Game.GRID_SIZE, Game.GRID_SIZE, 10, 10);
    }
}
