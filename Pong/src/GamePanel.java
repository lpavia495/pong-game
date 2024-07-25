import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    static final int GAME_WIDTH = 1000;
    static final int GAME_HEIGHT = (int) (GAME_WIDTH * 0.5555);
    static final Dimension SCREEN_SIZE = new Dimension(GAME_WIDTH, GAME_HEIGHT);
    static final int BALL_DIAMETER = 20;
    static final int PADDLE_WIDTH = 25;
    static final int PADDLE_HEIGHT = 100;
    Thread gameThread;
    Image image;
    Graphics graphics;
    Random random;
    Paddle paddle1;
    Paddle paddle2;
    Ball ball;
    Score score;
    boolean gameOver; // Declare the gameOver variable

    GamePanel() {
        newPaddles();
        newBall();
        score = new Score(GAME_WIDTH, GAME_HEIGHT);
        this.setFocusable(true);
        this.addKeyListener(new AL());
        this.setPreferredSize(SCREEN_SIZE);

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void newBall() {
        random = new Random();
        ball = new Ball((GAME_WIDTH / 2) - (BALL_DIAMETER / 2), random.nextInt(GAME_HEIGHT - BALL_DIAMETER), BALL_DIAMETER, BALL_DIAMETER);
    }

    public void newPaddles() {
        paddle1 = new Paddle(0, (GAME_HEIGHT / 2) - (PADDLE_HEIGHT / 2), PADDLE_WIDTH, PADDLE_HEIGHT, 1);
        paddle2 = new Paddle(GAME_WIDTH - PADDLE_WIDTH, (GAME_HEIGHT / 2) - (PADDLE_HEIGHT / 2), PADDLE_WIDTH, PADDLE_HEIGHT, 2);
    }

    public void paint(Graphics g) {
        image = createImage(getWidth(), getHeight());
        graphics = image.getGraphics();
        draw(graphics);
        g.drawImage(image, 0, 0, this);
    }

    public void draw(Graphics g) {
        if (!gameOver) {
            paddle1.draw(g);
            paddle2.draw(g);
            ball.draw(g);
            score.draw(g);
        } else {
            drawGameOver(g);
        }
    }

    public void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Consolas", Font.BOLD, 50));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String gameOverMessage = "Game Over";
        g.drawString(gameOverMessage, (GAME_WIDTH - metrics.stringWidth(gameOverMessage)) / 2, GAME_HEIGHT / 2 - 50);

        g.setFont(new Font("Consolas", Font.PLAIN, 30));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        String restartMessage = "Press Enter to start a new game or Escape to exit";
        g.drawString(restartMessage, (GAME_WIDTH - metrics2.stringWidth(restartMessage)) / 2, GAME_HEIGHT / 2 + 50);
    }

    public void move() {
        paddle1.move();
        paddle2.move();
        ball.move();
    }

    public void checkCollision() {
        // bounce ball off top & bottom window edges
        if (ball.y <= 0) {
            ball.setYDirection(-ball.yVelocity);
        }
        if (ball.y >= GAME_HEIGHT - BALL_DIAMETER) {
            ball.setYDirection(-ball.yVelocity);
        }
        // bounce ball off paddles
        if (ball.intersects(paddle1)) {
            ball.xVelocity = Math.abs(ball.xVelocity);
            ball.xVelocity++; // optional for more difficulty
            if (ball.yVelocity > 0)
                ball.yVelocity++; // optional for more difficulty
            else
                ball.yVelocity--;
            ball.setXDirection(ball.xVelocity);
            ball.setYDirection(ball.yVelocity);
        }
        if (ball.intersects(paddle2)) {
            ball.xVelocity = Math.abs(ball.xVelocity);
            ball.xVelocity++; // optional for more difficulty
            if (ball.yVelocity > 0)
                ball.yVelocity++; // optional for more difficulty
            else
                ball.yVelocity--;
            ball.setXDirection(-ball.xVelocity);
            ball.setYDirection(ball.yVelocity);
        }
        // stops paddles at window edges
        if (paddle1.y <= 0)
            paddle1.y = 0;
        if (paddle1.y >= (GAME_HEIGHT - PADDLE_HEIGHT))
            paddle1.y = GAME_HEIGHT - PADDLE_HEIGHT;
        if (paddle2.y <= 0)
            paddle2.y = 0;
        if (paddle2.y >= (GAME_HEIGHT - PADDLE_HEIGHT))
            paddle2.y = GAME_HEIGHT - PADDLE_HEIGHT;
        // give a player 1 point and creates new paddles & ball
        if (ball.x <= 0) {
            score.player2++;
            checkGameOver();
            newPaddles();
            newBall();

        }
        if (ball.x >= GAME_WIDTH - BALL_DIAMETER) {
            score.player1++;
            checkGameOver();
            newPaddles();
            newBall();
           
        }
    }

    public void checkGameOver() {
        if (score.player1 == 10 || score.player2 == 10) {
            gameOver = true;
            gameThread = null; // Stop the game loop
        }
    }

    public void run() {
        // game loop
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while (!gameOver) { // Check if game is not over
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            if (delta >= 1) {
                move();
                checkCollision();
                repaint();
                delta--;
            }
        }
        repaint(); // Ensure final repaint to show game over screen
    }

    public void restartGame() {
        score.player1 = 0;
        score.player2 = 0;
        gameOver = false;
        newPaddles();
        newBall();
        gameThread = new Thread(this);
        gameThread.start();
    }

    public class AL extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (gameOver) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    restartGame();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            } else {
                paddle1.keyPressed(e);
                paddle2.keyPressed(e);
            }
        }

        public void keyReleased(KeyEvent e) {
            if (!gameOver) {
                paddle1.keyReleased(e);
                paddle2.keyReleased(e);
            }
        }
    }
}
