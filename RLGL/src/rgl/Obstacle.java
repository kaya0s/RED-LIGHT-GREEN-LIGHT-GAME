package rgl;	

import javax.swing.*;



import java.awt.*;

public class Obstacle {
    private int x, y, size;
    private Image image;

    public Obstacle(int x, int y, int size, String imagePath) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.image = new ImageIcon(getClass().getResource(imagePath)).getImage();
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, size, size, null);
    }

    public Rectangle getBoundingRectangle() {
        int obstaclePadding = size / 10;
        return new Rectangle(
            x + obstaclePadding, 
            y + obstaclePadding, 
            size - 2 * obstaclePadding, 
            size - 2 * obstaclePadding
        );
    }
}