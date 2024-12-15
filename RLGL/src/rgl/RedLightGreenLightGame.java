package rgl;

import javax.swing.*;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class RedLightGreenLightGame extends JPanel implements ActionListener {

    //Fixed game constants
    private static final int TILE_SIZE = 40;
    private static final int GRID_WIDTH = 18; 
    private static final int GRID_HEIGHT = 12;
    private static final int FINISH_LINE_COL = GRID_WIDTH - 3;
    private static final int SPEED = 2; 
    private final int TARGET_FPS = 60;
    private final int TARGET_DELTA = 1000 / TARGET_FPS;

    // Game state
    private boolean isGreenLight = false;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private String winnerMessage = "";
    private int activePlayers;
    private int remainingTime = 30;
    private int countdownSeconds = 3;
    private boolean showStopMessage = false;
    private List<Player> finishedPlayers = new ArrayList<>();
    
    // Game objects
    private final ArrayList<Player> players = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();
    private Boss boss;
    private Obstacle obstacle;
    private final Image tiles;
    private int numPlayers = 4; // Set default to 4 players

    // Timers
    private Timer lightSwitchTimer;
    private Timer stopTimer;
    private Timer gameTimer;
    private Timer countdownTimer;

    // Audio
    private Clip greenLightClip;
    private Clip eliminatedClip;
    private Clip introMusicClip;
    private AudioInputStream greenLightAudio;
    private AudioInputStream eliminatedAudio;
    private AudioInputStream introMusicAudio;
    

    // Frame timing
    private long lastUpdateTime = 0;
    private long lastRenderTime = 0;
    
    private static JFrame frame;
    private JButton startButton;
    private JButton instructionButton; 
    private JButton exitButton;
    private JDialog instructionDialog;
    private JDialog playerSelectDialog;
    
    

    public RedLightGreenLightGame() {
    	
        // Set up the game panel
        setPreferredSize(new Dimension(TILE_SIZE * GRID_WIDTH, TILE_SIZE * GRID_HEIGHT));
        setBackground(Color.BLACK);

        // Load background tiles
        tiles = new ImageIcon(getClass().getResource("/zzz/tileable-IMG_0062.png")).getImage();
        
        // Initialize game components
        boss = new Boss(TILE_SIZE * (GRID_WIDTH - 3), TILE_SIZE * 3, "/zzz/butch.png");

        // INSTRUCTION DIALOG
        instructionDialog = new JDialog();
        instructionDialog.setTitle("How to Play");
        instructionDialog.setSize(500, 400);
        instructionDialog.setLocationRelativeTo(null);
        instructionDialog.setModal(true);
        instructionDialog.setBackground(new Color(40, 40, 40));

        // Create player select dialog
        playerSelectDialog = new JDialog();
        playerSelectDialog.setTitle("Select Number of Players");
        playerSelectDialog.setSize(300, 200);
        playerSelectDialog.setLocationRelativeTo(null);
        playerSelectDialog.setModal(true);
        playerSelectDialog.setBackground(new Color(40, 40, 40));
        
        playerSelectDialog.setFocusable(false);
        playerSelectDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Prevent default close behavior
        playerSelectDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // Exit the application when the "X" button is clicked
                int response = JOptionPane.showConfirmDialog(
                        playerSelectDialog,
                        "Are you sure you want to exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    System.exit(0); // Exit the program
                }
            }
        });
        
        //OPTION PANE MAGPILI UG PLAYER SELECT
        JPanel playerSelectPanel = new JPanel();
        playerSelectPanel.setLayout(new BoxLayout(playerSelectPanel, BoxLayout.Y_AXIS));
        playerSelectPanel.setBackground(new Color(40, 40, 40));
        playerSelectPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel selectLabel = new JLabel("Select number of players:");
        selectLabel.setForeground(Color.WHITE);
        selectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerSelectPanel.add(selectLabel);
        playerSelectPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(40, 40, 40));
        for (int i = 2; i <= 4; i++) {
            final int players = i;
            JButton playerButton = new JButton(i + " Players");
            styleButton(playerButton);
            playerButton.addActionListener(e -> {
                numPlayers = players;
                playerSelectDialog.dispose();
                initializePlayers();
                countdownTimer.start();
            });
            buttonPanel.add(playerButton);
        }
        playerSelectPanel.add(buttonPanel);
        playerSelectDialog.add(playerSelectPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(40, 40, 40));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // INSTRUCTION CONTENT
        JTextArea instructionText = new JTextArea(
            "OBJECTIVE:\n" +
            "Race to the finish line while while the Butch(Boss) not Looking!\n\n" +
            "RULES:\n" +
            "• Boss Not Looking - Move quickly!\n" +
            "• Boss is Looking - Freeze immediately!\n" +
            "• Getting caught moving during Butch Look - Butch will Kill you\n" +
            "• Watch out for obstacles in your path\n" +
            "• Beat the timer to win\n\n" +
            "CONTROLS:\n" +
            "↑ ↓ ← → Arrow Keys: Move your player\n" +
            "TIPS:\n" +
            "• Stay alert for the Boss\n" +
            "• Plan your route around obstacles\n" +
            "• Take your movements carefully"
        );
        
        instructionText.setEditable(false);
        instructionText.setLineWrap(true);
        instructionText.setWrapStyleWord(true);
        instructionText.setBackground(new Color(50, 50, 50));
        instructionText.setForeground(Color.WHITE);
        instructionText.setFont(new Font("Arial", Font.PLAIN, 16));
        instructionText.setMargin(new Insets(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(instructionText);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        instructionDialog.add(contentPanel);

        // START BUTTON
        startButton = new JButton("START GAME") {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add glow effect
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(2, 2, getWidth()-4, getHeight()-4);
                
                // Draw text
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), textX, textY);
            }
        };
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setContentAreaFilled(false);
        startButton.addActionListener(e -> {
            startButton.setVisible(false);
            instructionButton.setVisible(false);
            exitButton.setVisible(false);
            playerSelectDialog.setVisible(true);
        });
        
        // START BUTTON LAYOUT POSITION
        setLayout(null);
        startButton.setBounds(
            (TILE_SIZE * GRID_WIDTH - 200) / 2,
            (TILE_SIZE * GRID_HEIGHT - 50) / 2 + 50,
            200,
            50
        );
        add(startButton);

        //INSTRUCTION BUTTON
        instructionButton = new JButton("INSTRUCTIONS") {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(2, 2, getWidth()-4, getHeight()-4);
                
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), textX, textY);
            }
        };
        instructionButton.setPreferredSize(new Dimension(200, 50));
        instructionButton.setFocusPainted(false);
        instructionButton.setBorderPainted(false);
        instructionButton.setContentAreaFilled(false);
        instructionButton.addActionListener(e -> instructionDialog.setVisible(true));
        instructionButton.setBounds(
            (TILE_SIZE * GRID_WIDTH - 200) / 2,
            (TILE_SIZE * GRID_HEIGHT - 50) / 2 + 125,
            200,
            50
        );
        add(instructionButton);

        //EXIT BUTTON
        exitButton = new JButton("EXIT") {


            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(2, 2, getWidth()-4, getHeight()-4);
                
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), textX, textY);
            }
        };
        
        exitButton.setPreferredSize(new Dimension(200, 50));
        exitButton.setFocusPainted(false);
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setBounds(
            (TILE_SIZE * GRID_WIDTH - 200) / 2,
            (TILE_SIZE * GRID_HEIGHT - 50) / 2 + 200,
            200,
            50
        );
        add(exitButton);

        setFocusable(true);
        addKeyListener(new PlayerKeyAdapter());

        //SET UP GAME TIMER
        gameTimer = new Timer(16, this);
        lightSwitchTimer = new Timer(2000, new LightSwitch());
        
        // Initialize countdown timer
        countdownTimer = new Timer(1000, e -> {
            if (countdownSeconds > 0) {
                countdownSeconds--;
                repaint();
            } else {
                countdownTimer.stop();
                gameStarted = true;
                if (introMusicClip != null && introMusicClip.isRunning()) {
                    introMusicClip.stop();
                }
                gameTimer.start();
                lightSwitchTimer.start();
            }
        });
        
        // Initialize obstacles
        obstacles.add(new Obstacle(TILE_SIZE * 5, TILE_SIZE * 12, TILE_SIZE*1,"/zzz/obs3.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 10, TILE_SIZE * 1, TILE_SIZE*1,"/zzz/Rock2.png"));
        obstacles.add(new Obstacle(TILE_SIZE *12, TILE_SIZE * 9, TILE_SIZE*1,"/zzz/Rock4.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 6, TILE_SIZE * 4, TILE_SIZE*1,"/zzz/Rock3.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 7, TILE_SIZE *1, TILE_SIZE*2,"/zzz/obs3.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 10, TILE_SIZE *6, TILE_SIZE*2,"/zzz/ob4.png"));
        obstacles.add(new Obstacle(TILE_SIZE *10, TILE_SIZE * 2, TILE_SIZE*1,"/zzz/Rock1.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 4, TILE_SIZE * 4, TILE_SIZE*1,"/zzz/obs2.png"));
        obstacles.add(new Obstacle(TILE_SIZE *8, TILE_SIZE * 8, TILE_SIZE*2,"/zzz/Rock1.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 12, TILE_SIZE *4, TILE_SIZE*1,"/zzz/ob4.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 10, TILE_SIZE * 5, TILE_SIZE*1,"/zzz/Rock1.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 10, TILE_SIZE * 11, TILE_SIZE*1,"/zzz/Rock4.png"));
        obstacles.add(new Obstacle(TILE_SIZE * 3, TILE_SIZE * 8, TILE_SIZE*1,"/zzz/obs2.png"));

        // Load audio
        try {
            // Load and configure green light sound
            greenLightAudio = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/zzz/sneakySound.wav"));
            greenLightClip = AudioSystem.getClip();
            greenLightClip.open(greenLightAudio);
            
            // Load and configure eliminated sound
            eliminatedAudio = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/zzz/bang.wav"));
            eliminatedClip = AudioSystem.getClip();
            eliminatedClip.open(eliminatedAudio);
            
            // Load and configure intro music
            introMusicAudio = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/zzz/introMusic.wav"));
            introMusicClip = AudioSystem.getClip();
            introMusicClip.open(introMusicAudio);
            introMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Start playing intro music immediately
            
            // Add listeners to properly close clips
            greenLightClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    greenLightClip.setFramePosition(0);
                }
            });
            
            eliminatedClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    eliminatedClip.setFramePosition(0);
                }
            });
            
            introMusicClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    introMusicClip.setFramePosition(0);
                }
            });
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading audio: " + e.getMessage());
        }
        
        // Initialize stop timer
        stopTimer = new Timer(200, e -> {
            showStopMessage = false;
            repaint();
            stopTimer.stop();
        });
        
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(100, 40));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 80));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
        });
    }

    private void initializePlayers() {
        for (int i = 0; i < numPlayers; i++) {
            String[] imagePaths = switch (i) {
                case 0 -> new String[]{"/zzz/1-1.png","/zzz/1-2.png", "/zzz/1-3.png","/zzz/1-4.png","/zzz/1-5.png","/zzz/1-6.png","/zzz/1-4.png"};
                case 1 -> new String[]{"/zzz/2-1.png","/zzz/2-2.png", "/zzz/2-3.png","/zzz/2-4.png","/zzz/2-5.png","/zzz/2-6.png","/zzz/2-4.png"};
                case 2 -> new String[]{"/zzz/3-1.png","/zzz/3-2.png", "/zzz/3-3.png","/zzz/3-4.png","/zzz/3-5.png","/zzz/3-6.png","/zzz/3-4.png"};
                case 3 -> new String[]{"/zzz/1-1.png","/zzz/1-2.png", "/zzz/1-3.png","/zzz/1-4.png","/zzz/1-5.png","/zzz/1-6.png","/zzz/1-4.png"};
                default -> new String[]{"/zzz/1-1.png"};
            };
            int startY = TILE_SIZE * (2 + i * 2);
            players.add(new Player(TILE_SIZE, startY, imagePaths, i + 1));
            
        }
        activePlayers = numPlayers;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background tiles
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                int x = i * TILE_SIZE;
                int y = j * TILE_SIZE;
                g.drawImage(tiles, x, y, TILE_SIZE * 2, TILE_SIZE, this);
            }
        }

        // Draw upper and lower design blocks
        Image designBlock = new ImageIcon(getClass().getResource("/zzz/tile_0021.png")).getImage();
        for (int i = 0; i < getWidth(); i += TILE_SIZE/2) {
            g.drawImage(designBlock, i, 0, TILE_SIZE/2, TILE_SIZE, this); // Top row
            g.drawImage(designBlock, i, getHeight() - TILE_SIZE/2, TILE_SIZE/2, TILE_SIZE/2, this); // Bottom row
        }

        // Draw the finish line
        g.setColor(Color.YELLOW);
        for (int j = 0; j < GRID_HEIGHT; j++) {
            int y = j * TILE_SIZE;
            g.fillRect(FINISH_LINE_COL * TILE_SIZE + TILE_SIZE / 4, y, TILE_SIZE / 8, TILE_SIZE);
        }

        // Draw the obstacles
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g);
        }

        // Draw the players
        for (Player player : players) {
            if (player.eliminated) {
                drawEliminatedIndicator(g, player);
            } else {
                g.drawImage(player.frames[player.frameIndex], player.x, player.y, TILE_SIZE, TILE_SIZE, this);
            }
        }

        // Draw the boss
        boss.draw(g);

        if (!gameStarted) {
            // Draw intro screen
            g.setColor(new Color(0, 0, 0,180));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw subtitles
            g.setColor(new Color(181, 179, 179));

            // Draw subtitle 1
            g.setFont(new Font("Broadway", Font.BOLD, 12));
            String subtitle1 = "GREEN LIGHT RED LIGHT GAME";
            FontMetrics fm21 = g.getFontMetrics();
            int subtitleX1 = (getWidth() - fm21.stringWidth(subtitle1)) / 2;
            g.drawString(subtitle1, subtitleX1, getHeight() / 3 + 75);

            
            try {
            	BufferedImage logo =ImageIO.read(new File("src/zzz/gLogo.png"));
            	   int logoX = (getWidth() - logo.getWidth()) / 2;
            	   int logoY = getHeight() / 3 - logo.getHeight() / 2 - 20;
            	   g.drawImage(logo, logoX, logoY, null);
            	   
            }catch(Exception e ) {
            		System.out.println("Game Logo not Found: "+e.getMessage());
            }
         // Draw subtitle 2
            g.setFont(new Font("Broadway", Font.BOLD, 20));
            String subtitle2 = "THE WALK OF DEATH:BUTCH EDITION";
            FontMetrics fm22 = g.getFontMetrics();
            int subtitleX2 = (getWidth() - fm22.stringWidth(subtitle2)) / 2;
            g.drawString(subtitle2, subtitleX2, getHeight() / 3 + 95);
            
            if (!startButton.isVisible()) {
                // Draw countdown
                g.setColor(Color.WHITE);
                g.setFont(new Font("Broadway", Font.BOLD, 85));
                String countdownText = countdownSeconds > 0 ? Integer.toString(countdownSeconds) : "";
                fm21 = g.getFontMetrics();
                int textX = (getWidth() - fm21.stringWidth(countdownText)) / 2;
                int textY = (getHeight() + fm21.getAscent()) / 2 + 50;
                
                // Draw text shadow
                g.setColor(new Color(82, 32, 18));
                g.drawString(countdownText, textX + 3, textY + 3);
                
                // Draw main text
                g.setColor(Color.WHITE);
                g.drawString(countdownText, textX, textY);
            }
            return;
        }

        // Display remaining time
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Time: " + remainingTime, 10, 30);

        // Draw control instructions
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString("Player 1:W/A/S/D     Player 2:Arrrows     Player 3:I/J/K/L     Player 4:T/F/G/H", 10, getHeight() - 5);
        g.drawString("Controls:", 10, getHeight() - 30);

        // Draw "Stop!" message if needed
        if (showStopMessage) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            FontMetrics stopMetrics = g.getFontMetrics();
            
            String stopText = "Stop!";
            
            int stopX = (getWidth() - stopMetrics.stringWidth(stopText)) / 2;
            int stopY = (getHeight() + stopMetrics.getAscent()) / 2;
            g.drawString(stopText, stopX, stopY);
        }
    }

    //ELIMINATED INDICATOR X
    private void drawEliminatedIndicator(Graphics g, Player player) {
        Image grayscaleImage = convertToGrayscale(player.frames[player.frameIndex]);
        g.drawImage(grayscaleImage, player.x, player.y, TILE_SIZE, TILE_SIZE, this);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 3));
        g.drawLine(player.x, player.y, player.x + TILE_SIZE, player.y + TILE_SIZE);
        g.drawLine(player.x + TILE_SIZE, player.y, player.x, player.y + TILE_SIZE);
    }
    //GRAYSCALE ON ELIMINATED PLAYERS
    private Image convertToGrayscale(Image originalImage) {
        if (originalImage instanceof BufferedImage) {
            BufferedImage bufferedImage = (BufferedImage) originalImage;
            ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
            return colorConvert.filter(bufferedImage, null);
        }
        BufferedImage bufferedImage = new BufferedImage(originalImage.getWidth(null), originalImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return colorConvert.filter(bufferedImage, null);
    }
    
    //END GAME
    private void endGame(String message) {
        gameTimer.stop();
        lightSwitchTimer.stop();
        
        // Stop all audio
        if (greenLightClip != null && greenLightClip.isRunning()) {
            greenLightClip.stop();
        }
        
        // Create custom game over dialog
        JDialog gameOverDialog = new JDialog(frame, "Game Over", true);
        gameOverDialog.setLayout(new BorderLayout());
        
        // Create panel with gradient background
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 25, 112), 0, h, new Color(70, 130, 180));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add message label
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(messageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        // Style buttons
        JButton playAgainButton = createStyledButton("Play Again");
        JButton exitButton = createStyledButton("Exit");
        
        playAgainButton.addActionListener(e -> {
            gameOverDialog.dispose();
            frame.dispose();
            StartGame();
        });
        
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(playAgainButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(exitButton);
        
        panel.add(buttonPanel);
        
        gameOverDialog.add(panel);
        gameOverDialog.pack();
        gameOverDialog.setLocationRelativeTo(frame);
        gameOverDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        gameOverDialog.setVisible(true);
    }
    
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(83, 21, 92));
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(66, 15, 74));
                } else {
                    g.setColor(new Color(47, 8, 54));
                }
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(120, 40));
        return button;
    }
    
    public static void StartGame() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Game");
            RedLightGreenLightGame gamePanel = new RedLightGreenLightGame();
            frame.add(gamePanel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGreenLight) {
            players.stream()
                    .filter(player -> !player.eliminated)
                    .forEach(player -> player.updatePosition(obstacles));
        } else {
            boss.eliminatePlayers(players, 	finishedPlayers, isGreenLight);
        }

        // Check for winners
        for (Player player : players) {
            if (!player.eliminated && player.x >= FINISH_LINE_COL * TILE_SIZE && !finishedPlayers.contains(player)) {
                finishedPlayers.add(player);
                System.out.println("Player " + player.playerNumber + " has finished!");
            }
        }
        
        // Check if all non-eliminated players have finished or if no players can continue
        boolean allPlayersFinishedOrEliminated = true;
        for (Player player : players) {
            if (!player.eliminated && !finishedPlayers.contains(player)) {
                allPlayersFinishedOrEliminated = false;
                break;
            }
        }

        if (allPlayersFinishedOrEliminated && !gameOver) {
            if (!finishedPlayers.isEmpty()) {
                StringBuilder winnerList = new StringBuilder("Players ");
                for (Player winner : finishedPlayers) {
                    winnerList.append(winner.playerNumber).append(" ");
                }
                winnerMessage = winnerList.toString() + "Survived!";
            } else {
                winnerMessage = "BUTCH WON!";
            }
            gameOver = true;
        }
        
        if (gameOver) {
            endGame(winnerMessage);
        }

        repaint();
    }

    private class PlayerKeyAdapter extends KeyAdapter {


        @Override
        public void keyPressed(KeyEvent e) {
            if (gameOver || !gameStarted) return;
            int key = e.getKeyCode();

            if (isGreenLight) {
                for (Player player : players) {
                    if (!player.eliminated && !finishedPlayers.contains(player)) {
                        player.setMovement(key, true);
                    }
                }
            } else {
                for (Player player : players) {
                    if (!player.eliminated && !finishedPlayers.contains(player) && player.isMovingKey(key)) {
                        player.eliminated = true;
                        if (eliminatedClip != null) {
                            eliminatedClip.setFramePosition(0);
                            eliminatedClip.start();
                        }
                        System.out.println("Player " + player.playerNumber + " was Killed!");
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            for (Player player : players) {
                if (!finishedPlayers.contains(player)) {
                    player.setMovement(key, false);
                }
            }
        }
    }

    private class LightSwitch implements ActionListener {





        private final Random random = new Random();

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isGreenLight) {
                showStopMessage = true;
                repaint();
                stopTimer.restart();
                
                Timer delayTimer = new Timer(500, evt -> {
                    isGreenLight = false;
                    if (greenLightClip != null && greenLightClip.isRunning()) {
                        greenLightClip.stop();
                    }
                    boss.setMirrored(true);
                    ((Timer)evt.getSource()).stop();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            } else {
                isGreenLight = true;
                if (greenLightClip != null) {
                    greenLightClip.setFramePosition(0);
                    greenLightClip.loop(Clip.LOOP_CONTINUOUSLY);
                }
                boss.setMirrored(false);
            }

            lightSwitchTimer.setDelay(1000 + random.nextInt(2000));
            remainingTime--;
            
            if (remainingTime <= 0) {
                gameOver = true;
                if (greenLightClip != null && greenLightClip.isRunning()) {
                    greenLightClip.stop();
                }

                if (!finishedPlayers.isEmpty()) {
                    StringBuilder winnerList = new StringBuilder("Players ");
                    for (Player winner : finishedPlayers) {
                        winnerList.append(winner.playerNumber).append(" ");
                    }
                    winnerMessage = winnerList.toString() + "have crossed the finish line!";
                } else {
                	
                    winnerMessage = "No winner";
                }

                endGame(winnerMessage);
            }
        }
    }

}
