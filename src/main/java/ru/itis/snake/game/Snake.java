package main.java.ru.itis.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Snake extends GameObject {
    private final List<Point> body = new ArrayList<>();
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private int length = 1;

    public Snake() {
        super(Game.SCREEN_WIDTH/2, Game.SCREEN_HEIGHT/2, Color.GREEN);
        reset();
    }

    public void updateDirection(KeyCode key) {
        switch (key) {
            case UP -> { if (direction != Direction.DOWN) nextDirection = Direction.UP; }
            case DOWN -> { if (direction != Direction.UP) nextDirection = Direction.DOWN; }
            case LEFT -> { if (direction != Direction.RIGHT) nextDirection = Direction.LEFT; }
            case RIGHT -> { if (direction != Direction.LEFT) nextDirection = Direction.RIGHT; }
        }
    }

    public void move() {
        direction = nextDirection;
        Point head = body.get(0);
        int newX = (head.x + direction.dx * Game.GRID_SIZE + Game.SCREEN_WIDTH) % Game.SCREEN_WIDTH;
        int newY = (head.y + direction.dy * Game.GRID_SIZE + Game.SCREEN_HEIGHT) % Game.SCREEN_HEIGHT;
        body.add(0, new Point(newX, newY));
        if (body.size() > length) {
            body.remove(body.size() - 1);
        }
    }

    public boolean checkSelfCollision() {
        Point head = getHead();
        return body.stream().skip(1).anyMatch(p -> p.equals(head));
    }

    public void grow() {
        length++;
    }

    public void shrink() {
        if (length > 1) {
            length--;
            if (body.size() > length) {
                body.remove(body.size() - 1);
            }
        }
    }

    public void reset() {
        body.clear();
        body.add(new Point(Game.SCREEN_WIDTH/2, Game.SCREEN_HEIGHT/2));
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        length = 1;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        body.forEach(p -> {
            gc.fillRect(p.x, p.y, Game.GRID_SIZE-1, Game.GRID_SIZE-1);
            gc.setStroke(Color.CYAN);
            gc.strokeRect(p.x, p.y, Game.GRID_SIZE, Game.GRID_SIZE);
        });
    }

    public Point getHead() {
        return body.get(0);
    }

    public Set<Point> getOccupiedCells() {
        return new HashSet<>(body);
    }

    enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx, dy;
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }
}
