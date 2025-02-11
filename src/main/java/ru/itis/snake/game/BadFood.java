package main.java.ru.itis.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BadFood extends GameObject {
    public BadFood() {
        super(0, 0, Color.ORANGE);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, Game.GRID_SIZE, Game.GRID_SIZE);
    }
}