package rgl;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player {
    int x, y;
    int deltaX, deltaY;
    final Image[] frames;
    int frameIndex = 0;
    int frameDelay = 5;
    int frameDelayCounter = 0;
    boolean eliminated = false;
    boolean moving = false;
    final int playerNumber;
    private static final int SPEED = 2;
    private static final int TILE_SIZE = 40;
    private static final int GRID_WIDTH = 18;
    private static final int GRID_HEIGHT = 12;

    public Player(int x, int y, String[] imagePaths, int playerNumber) {
        this.x = x;
        this.y = y;
        this.playerNumber = playerNumber;

        frames = new Image[imagePaths.length];
        for (int i = 0; i < imagePaths.length; i++) {
            frames[i] = new ImageIcon(getClass().getResource(imagePaths[i])).getImage();
        }
    }
    
    public Rectangle getBoundingRectangle() {
        int padding = 5;
        return new Rectangle(x + padding, y + padding, TILE_SIZE - 2 * padding, TILE_SIZE - 2 * padding);
    }

    public void updatePosition(java.util.List<Obstacle> obstacles) {
        if (eliminated) return;

        int newX = Math.max(0, Math.min(x + deltaX, TILE_SIZE * (GRID_WIDTH - 1)));
        int newY = Math.max(0, Math.min(y + deltaY, TILE_SIZE * (GRID_HEIGHT - 1)));

        Rectangle playerRect = new Rectangle(newX, newY, TILE_SIZE, TILE_SIZE);
        boolean collisionDetected = false;

        for (Obstacle obstacle : obstacles) {
            if (playerRect.intersects(obstacle.getBoundingRectangle())) {
                collisionDetected = true;
                break;
            }
        }

        if (!collisionDetected) {
            x = newX;
            y = newY;
        }

        if (moving) {
            if (frameDelayCounter >= frameDelay) {
                frameIndex = (frameIndex + 1) % frames.length;
                frameDelayCounter = 0;
            } else {
               frameDelayCounter++;
            }
        } else {
            frameIndex = 0;
        }
    }
    
    public void setMovement(int keyCode, boolean pressed) {
        switch (playerNumber) {
            case 1 -> {
                if (keyCode == KeyEvent.VK_UP) deltaY = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_DOWN) deltaY = pressed ? SPEED : 0;
                if (keyCode == KeyEvent.VK_LEFT) deltaX = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_RIGHT) deltaX = pressed ? SPEED : 0;
            }
            case 2 -> {
                if (keyCode == KeyEvent.VK_W) deltaY = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_S) deltaY = pressed ? SPEED : 0;
                if (keyCode == KeyEvent.VK_A) deltaX = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_D) deltaX = pressed ? SPEED : 0;
            }
            case 3 -> {
                if (keyCode == KeyEvent.VK_I) deltaY = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_K) deltaY = pressed ? SPEED : 0;
                if (keyCode == KeyEvent.VK_J) deltaX = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_L) deltaX = pressed ? SPEED : 0;
            }
            case 4 -> {
                if (keyCode == KeyEvent.VK_T) deltaY = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_G) deltaY = pressed ? SPEED : 0;
                if (keyCode == KeyEvent.VK_F) deltaX = pressed ? -SPEED : 0;
                if (keyCode == KeyEvent.VK_H) deltaX = pressed ? SPEED : 0;
            }
        }

        moving = (deltaX != 0) || (deltaY != 0);
    }

    public boolean isMovingKey(int keyCode) {
        return switch (playerNumber) {
            case 1 -> keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || 
                     keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT;
            case 2 -> keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_S || 
                     keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D;
            case 3 -> keyCode == KeyEvent.VK_I || keyCode == KeyEvent.VK_K || 
                     keyCode == KeyEvent.VK_J || keyCode == KeyEvent.VK_L;
            case 4 -> keyCode == KeyEvent.VK_T || keyCode == KeyEvent.VK_G || 
                     keyCode == KeyEvent.VK_F || keyCode == KeyEvent.VK_H;
            default -> false;
        };
    }
}