package rgl;

import javax.swing.*;

import java.awt.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class Boss {
    private int x, y;
    private Image bossImage;
    private boolean mirrored = true;
    private static final int TILE_SIZE = 40;
    private Clip eliminatedClip;
    private AudioInputStream eliminatedAudio;

    public Boss(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.bossImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int bossWidth = TILE_SIZE * 4;
        int bossHeight = TILE_SIZE * 4;

        if (mirrored) {
            g2d.drawImage(bossImage, x + bossWidth, y, -bossWidth, bossHeight, null);
        } else {
            g2d.drawImage(bossImage, x, y, bossWidth, bossHeight, null);
        }
    }

    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }
    
    public void eliminatePlayers(List<Player> players, List<Player> finishedPlayers, boolean isGreenLight) {
        for (Player player : players) {
            if (!player.eliminated && !finishedPlayers.contains(player) && player.moving && !isGreenLight) {
                player.eliminated = true;
                player.deltaX = 1;
                player.deltaY = 1;
                new Thread(this::playEliminatedAudio).start();
                System.out.println("Player " + player.playerNumber + " has been eliminated!");
            }
        }
    }

    private void playEliminatedAudio() {
        try {
            if (eliminatedClip != null) {
                eliminatedClip.stop();
                eliminatedClip.close();
            }
            if (eliminatedAudio != null) {
                eliminatedAudio.close();
            }
            
            eliminatedAudio = AudioSystem.getAudioInputStream(
                getClass().getResourceAsStream("/zzz/bang.wav"));
            eliminatedClip = AudioSystem.getClip();
            eliminatedClip.open(eliminatedAudio);
            eliminatedClip.setFramePosition(0);
            eliminatedClip.start();

            eliminatedClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    eliminatedClip.close();
                }
            });
        } catch (Exception ex) {
            System.err.println("Error playing audio: " + ex.getMessage());
        }
    }
}