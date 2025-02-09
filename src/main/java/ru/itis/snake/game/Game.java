package main.java.ru.itis.snake.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class Game {
    public static final int SCREEN_WIDTH = 640;
    public static final int SCREEN_HEIGHT = 480;
    public static final int GRID_SIZE = 20;
    public static final int GRID_WIDTH = SCREEN_WIDTH / GRID_SIZE;
    public static final int GRID_HEIGHT = SCREEN_HEIGHT / GRID_SIZE;

    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Snake snake;
    private final Apple apple;
    private final BadFood badFood;
    private final Obstacle obstacle;
    private int score = 0;
    private int record = 0;
    private long lastUpdate = 0;

    public Game(Stage primaryStage) {
        stage = primaryStage;
        canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        snake = new Snake();
        apple = new Apple();
        badFood = new BadFood();
        obstacle = new Obstacle();

        setupScene(scene);
        stage.setScene(scene);
        stage.setTitle("Змейка: Score 0");
    }

    private void setupScene(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.UP || code == KeyCode.DOWN ||
                    code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                snake.updateDirection(code);
            }
        });
    }

    public void start() {
        resetGame();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000 / 15) {
                    update();
                    render();
                    lastUpdate = now;
                }
            }
        }.start();
        stage.show();
    }

    private void update() {
        snake.move();
        checkCollisions();
        checkApple();
        checkBadFood();
        checkObstacle();
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        apple.draw(gc);
        badFood.draw(gc);
        obstacle.draw(gc);
        snake.draw(gc);
    }

    private void checkCollisions() {
        if (snake.checkSelfCollision()) {
            resetGame();
        }
    }

    private void checkApple() {
        if (snake.getHead().equals(apple.getPosition())) {
            snake.grow();
            score++;
            apple.randomizePosition(snake.getOccupiedCells());
            updateTitle();
        }
    }

    private void checkBadFood() {
        if (snake.getHead().equals(badFood.getPosition())) {
            snake.shrink();
            score = Math.max(0, score - 1);
            badFood.randomizePosition(snake.getOccupiedCells());
            updateTitle();
        }
    }

    private void checkObstacle() {
        if (snake.getHead().equals(obstacle.getPosition())) {
            resetGame();
        }
    }

    private void updateTitle() {
        stage.setTitle("Змейка: Score " + score);
    }

    private void resetGame() {
        score = 0;
        snake.reset();
        apple.randomizePosition(snake.getOccupiedCells());
        badFood.randomizePosition(snake.getOccupiedCells());
        obstacle.randomizePosition(snake.getOccupiedCells());
        updateTitle();
    }
}
