package main.java.ru.itis.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Set;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Set;

public abstract class GameObject {
    protected int x;
    protected int y;
    protected Color color;

    public GameObject(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public abstract void draw(GraphicsContext gc);

    public void randomizePosition(Set<Point> occupied) {
        Set<Point> available = new HashSet<>();
        for (int x = 0; x < Game.GRID_WIDTH; x++) {
            for (int y = 0; y < Game.GRID_HEIGHT; y++) {
                Point p = new Point(x * Game.GRID_SIZE, y * Game.GRID_SIZE);
                if (!occupied.contains(p)) available.add(p);
            }
        }
        if (!available.isEmpty()) {
            Point selected = available.stream().skip((int) (Math.random() * available.size())).findFirst().get();
            this.x = selected.x;
            this.y = selected.y;
        }
    }

    public Point getPosition() {
        return new Point(x, y);
    }
}