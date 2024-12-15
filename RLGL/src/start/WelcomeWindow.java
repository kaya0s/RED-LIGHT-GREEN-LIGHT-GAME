package start;

import rgl.Main;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.Color;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WelcomeWindow extends JFrame {
	
    private static final long serialVersionUID = 1L;
    private JLayeredPane layeredPane;
    private Clip backgroundMusicClip;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    WelcomeWindow frame = new WelcomeWindow();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public WelcomeWindow() {
        this.setTitle("DULA SA NUKOS");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 810, 540); // Set frame size
        setLocationRelativeTo(null);

        // JLayeredPane to hold components
        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
        layeredPane.setLayout(null); // No layout to manually manage components

        // Custom panel with background image
        BackgroundPanel backgroundPanel = new BackgroundPanel("background.gif");
        backgroundPanel.setBounds(0, 0, getWidth(), getHeight()); // Match frame size
        layeredPane.add(backgroundPanel, Integer.valueOf(1)); // Background layer

        // Play Now Button
        JButton btnPlayNow = new JButton("PLAY NOW!");
        btnPlayNow.setForeground(Color.WHITE);
        btnPlayNow.setFocusable(false);
        btnPlayNow.setBackground(new Color(87, 17, 49));
        btnPlayNow.setFocusPainted(false);
        btnPlayNow.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnPlayNow.setBounds(327, 298, 141, 39);
        btnPlayNow.setFocusable(false);
        layeredPane.add(btnPlayNow, Integer.valueOf(2));
        
        JLabel lblNewLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon(WelcomeWindow.class.getResource("/zzz/Logo.png"));

        // Scale the image to fit within the label
        Image scaledImage = originalIcon.getImage().getScaledInstance(417, 124, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Set the scaled icon to the label
        lblNewLabel.setIcon(scaledIcon);
        lblNewLabel.setBounds(229, 150, 417, 124);
        layeredPane.add(lblNewLabel, Integer.valueOf(2));
        
        
        // Button Action Listener
        btnPlayNow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stopBackgroundMusic(); // Stop the music when the game starts
                Main m = new Main();
                
             // Load the original icon
                ImageIcon originalIcon = new ImageIcon("C:/Users/erwin/eclipse-workspace/RLGL/src/start/cropped-Temp-Brand-Logo-Black.png");

                // Check if the image is loaded
                if (originalIcon.getIconWidth() == -1) {
                    JOptionPane.showMessageDialog(null, "Failed to load the image!");
                    return;
                }

                // Scale the icon
                int newWidth = 50;
                int newHeight = 50;
                Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                // Show JOptionPane with the scaled icon
            
                JOptionPane.showMessageDialog(
                        null,
                        "Players must cross the finish line without being caught moving during \"Red Light.\"",
                        "Objective of the Game",
                        JOptionPane.INFORMATION_MESSAGE,
                        scaledIcon
                );
            
               	
                m.main(null);
			   dispose();
            }
        });

        playBackgroundMusic("icecream.wav"); // Play the background music

        // Resize listener to adjust backgroundPanel when the frame is resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                backgroundPanel.setSize(getWidth(), getHeight());
            }
        });
    }

    /**
     * Method to play background music in a loop
     */
    private void playBackgroundMusic(String audioFilePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(audioFilePath));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioStream);
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to stop the background music
     */
    private void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
        }
    }

    // Inner class to create a JPanel with a scaled background image
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw the background image, scaled to fill the entire panel
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
