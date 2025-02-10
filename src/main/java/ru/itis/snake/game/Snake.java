package main.java.ru.itis.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Snake extends GameObject {
    public Color color;
    public final List<Point> body = new ArrayList<>();
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private int length = 1;

    public Snake() {
        super(Game.SCREEN_WIDTH/2, Game.SCREEN_HEIGHT/2, Color.GREEN);
        body.add(new Point(Game.SCREEN_WIDTH/2, Game.SCREEN_HEIGHT/2)); // Инициализация тела
        reset();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public synchronized void updateDirection(Direction newDir) {
        if((direction == Direction.UP && newDir != Direction.DOWN) ||
                (direction == Direction.DOWN && newDir != Direction.UP) ||
                (direction == Direction.LEFT && newDir != Direction.RIGHT) ||
                (direction == Direction.RIGHT && newDir != Direction.LEFT)) {
            nextDirection = newDir;
        }
    }

    public void updateDirection(KeyCode keyCode) {
        switch(keyCode) {
            case UP: case W: updateDirection(Direction.UP); break;
            case DOWN: case S: updateDirection(Direction.DOWN); break;
            case LEFT: case A: updateDirection(Direction.LEFT); break;
            case RIGHT: case D: updateDirection(Direction.RIGHT); break;
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

    public int getLength() {
        return length;
    }

    public Set<Point> getOccupiedCells() {
        return new HashSet<>(body);
    }

    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx, dy;
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    public void setPosition(int x, int y) {
        if (!body.isEmpty()) {
            Point head = body.get(0);
            head.x = x;
            head.y = y;
        } else {
            body.add(new Point(x, y));
        }
    }

}
