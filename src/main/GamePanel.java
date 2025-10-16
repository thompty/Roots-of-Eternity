package src.main;

import javax.swing.JPanel;
import javax.swing.JFrame;
import src.ai.Pathfinder;
import src.entity.Entity;
import src.entity.Player;
import src.tiles.TileManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GamePanel extends JPanel implements Runnable {
    // Screen Settings
    final int originalTileSize = 16; // 16x16 pixels for character base size
    final int scale = 3; // Scale of the game
    public final int tileSize = originalTileSize * scale; // 48x48 pixels for character size

    // Screen Size
    public final int maxScreenCols = 20; // 16 tiles wide
    public final int maxScreenRows = 12; // 12 tiles tall
    public final int screenWidth = tileSize * maxScreenCols; // 960 pixels wide
    public final int screenHeight = tileSize * maxScreenRows; // 576 pixels tall

    // World Settings
    public final int maxWorldCols = 1025; // 50 tiles wide
    public final int maxWorldRows = 625; // 50 tiles tall
    public final int maxMap = 10;
    public int currentMap = 0;
    // Player class selected at title screen (e.g. "Fighter", "Mage", ...)
    public String selectedClass = null;

    // Full Screen Settings
    int screenWidthFS = screenWidth;
    int screenHeightFS = screenHeight;
    BufferedImage tempScreen;
    Graphics2D g2;
    public boolean fullScreenOn = false;

    int FPS = 60;

    public TileManager tileManager = new TileManager(this);
    public KeyHandler keyHandler = new KeyHandler(this);
    Sound soundEffect = new Sound();
    Sound music = new Sound();
    public SaveManager saveManager = new SaveManager(this);
    // currently selected save name (nullable). If null, a new save name will be
    // generated
    public String currentSaveName = null;
    Thread gameThread;
    public CollisionChecker collisionChecker = new CollisionChecker(this);
    public AssetSetter assetSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    public EventHandler eventHandler = new EventHandler(this);
    Config config = new Config(this);
    public Pathfinder pathfinder = new Pathfinder(this);

    // Objects & Entities
    public Player player = new Player(this, keyHandler);
    public Entity obj[][] = new Entity[maxMap][20];
    public Entity npc[][] = new Entity[maxMap][10];
    public Entity monster[][] = new Entity[maxMap][20];
    public ArrayList<Entity> projectileList = new ArrayList<>();
    ArrayList<Entity> entityList = new ArrayList<>();

    // Game State
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;
    public final int characterStatus = 4;
    public final int optionsState = 5;
    public final int gameOverState = 6;
    public final int transitionState = 7;
    public final int tradeState = 8;
    // Debug: remember last state to log changes
    public int lastGameState = -1;

    // Debug Mode
    public boolean debugMode = false;
    public boolean showHitBoxes = false;
    // Hitbox tuner
    public boolean debugTuner = false; // toggled with Y (when debugMode is on)
    public int tunerTarget = 0; // 0=player,1=tile41,2=tile40,3=tile44,4=oldman,5=greenslime
    public int tunerSubIndex = 0; // when tuning NPC/monster/objects, which instance index
    public int tunerParam = 0; // 0=x,1=y,2=width,3=height
    public int tunerStep = 1;

    // Debug collision feedback
    public int lastCollisionWorldX = -1;
    public int lastCollisionWorldY = -1;
    public long lastCollisionTime = 0;
    // Press 'T' during gameplay to toggle debug hitbox visualization

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        // Allow Tab key events to be received by this panel (disable focus traversal)
        this.setFocusTraversalKeysEnabled(false);
        // Try to grab keyboard focus right away so keys like TAB are received
        this.requestFocusInWindow();
        // If the panel loses focus, clicking it will request focus again
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                GamePanel.this.requestFocusInWindow();
            }
        });

        // Also bind TAB at the window level so it works even if the panel doesn't have
        // focus
        javax.swing.KeyStroke tabKey = javax.swing.KeyStroke.getKeyStroke("TAB");
        this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(tabKey, "cycleTuner");
        this.getActionMap().put("cycleTuner", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (debugTuner) {
                    // expand tuner targets to include objects
                    tunerTarget = (tunerTarget + 1) % 7; // 0..6
                    String name;
                    switch (tunerTarget) {
                        case 0:
                            name = "player";
                            break;
                        case 1:
                            name = "Water";
                            break;
                        case 2:
                            name = "tile40 (wall)";
                            break;
                        case 3:
                            name = "tile44 (table)";
                            break;
                        case 4:
                            name = "npc OldMan";
                            break;
                        case 5:
                            name = "monster GreenSlime";
                            break;
                        case 6:
                            name = "object (world)";
                            break;
                        default:
                            name = "unknown";
                            break;
                    }
                    System.out.println("[Tuner] target -> " + tunerTarget + " (" + name + ")");
                }
            }
        });
    }

    public void setupGame() {
        assetSetter.setObject();
        assetSetter.setNPC();
        assetSetter.setMonster();
        pathfinder.instantiateNode();
        playMusic(0);
        gameState = titleState;

        // Load any saved hitbox tuner values so tuned hitboxes persist across runs
        keyHandler.loadTunerConfig();

        tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) tempScreen.getGraphics();

        if (fullScreenOn == true) {
            setFullScreen();
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        // Ensure this panel has keyboard focus so KeyHandler receives keys like TAB
        // use requestFocus which is more forceful when the window is visible
        this.requestFocus();
        gameThread.start();
    }

    public void retry() {
        player.setDefaultPositions();
        player.restoreLifeAndMana();
        clearWorldEntities();
        assetSetter.setNPC();
        assetSetter.setMonster();
    }

    public void restart() {
        player.setDefaultValues();
        player.setDefaultPositions();
        clearWorldEntities();
        assetSetter.setObject();
        assetSetter.setNPC();
        assetSetter.setMonster();
        playMusic(0);
    }

    // Clear world entity arrays to avoid duplicate spawns when re-initializing the
    // world (restart/retry/load). This mirrors the SaveManager load clearing logic
    // so newly-set entities won't be appended on top of existing ones.
    public void clearWorldEntities() {
        for (int m = 0; m < maxMap; m++) {
            for (int i = 0; i < obj[m].length; i++)
                obj[m][i] = null;
            for (int i = 0; i < npc[m].length; i++)
                npc[m][i] = null;
            for (int i = 0; i < monster[m].length; i++)
                monster[m][i] = null;
        }
    }

    public void setFullScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        Main.window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        screenWidthFS = (int) width;
        screenHeightFS = (int) height;
    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS; // 1 second / FPS or 0.0166666666666667 seconds
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            // Update information
            update();
            // Draw the screen with the updated information
            drawToTempScreen();

            drawToScreen();

            // repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                long sleepTime = (long) (remainingTime / 1000000); // Convert nanoseconds to milliseconds

                if (sleepTime > 0) {
                    Thread.sleep(sleepTime); // Sleep for the remaining time
                }

                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            while (System.nanoTime() > nextDrawTime) {
                nextDrawTime += drawInterval; // Skip to the next interval
            }
        }
    }

    public void update() {
        // Debug: log state changes (helps diagnose unexpected transitions)
        if (lastGameState != gameState) {
            lastGameState = gameState;
        }
        if (gameState == playState) {
            player.update();

            for (int i = 0; i < npc[currentMap].length; i++) {
                if (npc[currentMap][i] != null) {
                    npc[currentMap][i].update();
                }
            }

            for (int i = 0; i < monster[1].length; i++) {
                if (monster[currentMap][i] != null) {
                    if (monster[currentMap][i].alive == true && monster[currentMap][i].dead == false) {
                        monster[currentMap][i].update();
                    }
                    if (monster[currentMap][i].alive == false) {
                        monster[currentMap][i].checkDrop();
                        monster[currentMap][i] = null;
                    }
                }
            }
            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    if (projectileList.get(i).alive == true && projectileList.get(i).dead == false) {
                        projectileList.get(i).update();
                    }
                    if (projectileList.get(i).alive == false) {
                        projectileList.remove(i);
                    }
                }
            }
        }
        if (gameState == pauseState) {
            // Pause the game
        }
    }

    private String getStateName(int s) {
        switch (s) {
            case titleState:
                return "titleState";
            case playState:
                return "playState";
            case pauseState:
                return "pauseState";
            case dialogueState:
                return "dialogueState";
            case characterStatus:
                return "characterStatus";
            case optionsState:
                return "optionsState";
            case gameOverState:
                return "gameOverState";
            case transitionState:
                return "transitionState";
            case tradeState:
                return "tradeState";
            default:
                return "unknown(" + s + ")";
        }
    }

    public void drawToTempScreen() {
        // Draw the title screen

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        if (gameState == titleState) {
            ui.draw(g2);
        } else {
            tileManager.draw(g2);

            entityList.add(player);

            for (int i = 0; i < npc[1].length; i++) {
                if (npc[currentMap][i] != null) {
                    entityList.add(npc[currentMap][i]);
                }
            }

            for (int i = 0; i < obj[1].length; i++) {
                if (obj[currentMap][i] != null) { // Null check
                    entityList.add(obj[currentMap][i]);
                }
            }

            for (int i = 0; i < monster[1].length; i++) {
                if (monster[currentMap][i] != null) { // Null check
                    entityList.add(monster[currentMap][i]);
                }
            }

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) { // Null check
                    entityList.add(projectileList.get(i));
                }
            }

            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    int result = Integer.compare(e1.worldY, e2.worldY);
                    return result;
                }
            });

            for (int i = 0; i < entityList.size(); i++) {
                entityList.get(i).draw(g2);
            }
            // Empty
            entityList.clear();

            // Draw debug hit boxes if enabled
            if (debugMode && showHitBoxes) {
                drawDebugHitBoxes(g2);
            }

            // Draw tuner status overlay if tuner is active
            if (debugTuner) {
                int sx = 8;
                int sy = 8;
                int w = 260;
                int h = 84;
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(sx, sy, w, h);
                g2.setColor(Color.WHITE);
                g2.drawRect(sx, sy, w, h);
                g2.setFont(g2.getFont().deriveFont(14f));
                String targetName;
                switch (tunerTarget) {
                    case 0:
                        targetName = "player";
                        break;
                    case 1:
                        targetName = "Water";
                        break;
                    case 2:
                        targetName = "tile40 (wall)";
                        break;
                    case 3:
                        targetName = "tile44 (table)";
                        break;
                    case 4:
                        targetName = "npc OldMan";
                        break;
                    case 5:
                        targetName = "monster GreenSlime";
                        break;
                    default:
                        targetName = "unknown";
                        break;
                }
                g2.drawString("Tuner: " + targetName + "  (idx=" + tunerSubIndex + ")", sx + 8, sy + 20);
                String paramName = tunerParam == 0 ? "x" : tunerParam == 1 ? "y" : tunerParam == 2 ? "w" : "h";
                g2.drawString("Param: " + paramName + "   Step: " + tunerStep, sx + 8, sy + 40);
                g2.drawString("Use TAB to cycle target, ,/. to change index, arrows to adjust", sx + 8, sy + 62);
            }

            ui.draw(g2);
            // Draw minimap overlay on top if enabled
            if (tileManager != null && tileManager.showMiniMap) {
                tileManager.drawMiniMap(g2);
            }
        }
    }

    public void drawToScreen() {
        Graphics g = this.getGraphics();
        if (g != null && tempScreen != null) {
            g.drawImage(tempScreen, 0, 0, screenWidthFS, screenHeightFS, null);
            Toolkit.getDefaultToolkit().sync(); // Synchronize for smooth rendering
            g.dispose();
        }
    }

    public void drawDebugHitBoxes(Graphics2D g2) {
        // Save current graphics settings
        java.awt.Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();

        // Set debug drawing style
        g2.setStroke(new java.awt.BasicStroke(2));

        // Draw tile hit boxes (only for tiles with collision = true)
        g2.setColor(Color.RED);
        for (int worldCol = 0; worldCol < maxWorldCols; worldCol++) {
            for (int worldRow = 0; worldRow < maxWorldRows; worldRow++) {
                int tileNum = tileManager.mapTileNum[currentMap][worldCol][worldRow];
                if (tileNum >= 0 && tileNum < tileManager.tile.length &&
                        tileManager.tile[tileNum] != null && tileManager.tile[tileNum].collision) {
                    int worldX = worldCol * tileSize;
                    int worldY = worldRow * tileSize;
                    int screenX = worldX - player.worldX + player.screenX;
                    int screenY = worldY - player.worldY + player.screenY;

                    // Only draw if on screen
                    if (screenX > -tileSize && screenX < screenWidth &&
                            screenY > -tileSize && screenY < screenHeight) {
                        // Draw the actual hitBox if available, otherwise use full tile
                        if (tileManager.tile[tileNum].hitBox != null &&
                                tileManager.tile[tileNum].hitBox.width > 0
                                && tileManager.tile[tileNum].hitBox.height > 0) {
                            g2.drawRect(screenX + tileManager.tile[tileNum].hitBox.x,
                                    screenY + tileManager.tile[tileNum].hitBox.y,
                                    tileManager.tile[tileNum].hitBox.width,
                                    tileManager.tile[tileNum].hitBox.height);
                            // If tuner is active and this tile corresponds to a tunerTarget
                            // (1->41,2->40,3->44), highlight it
                            if (debugTuner && ((tunerTarget == 1
                                    && tileNum == 45) || (tunerTarget == 1 && tileNum == 46)
                                    || (tunerTarget == 1 && tileNum == 47) || (tunerTarget == 1 && tileNum == 35))) {
                                // Highlight the EXACT hitbox area with thick magenta border and
                                // semi-transparent fill
                                g2.setColor(new java.awt.Color(255, 0, 255, 100));
                                g2.fillRect(screenX + tileManager.tile[tileNum].hitBox.x,
                                        screenY + tileManager.tile[tileNum].hitBox.y,
                                        tileManager.tile[tileNum].hitBox.width,
                                        tileManager.tile[tileNum].hitBox.height);
                                g2.setColor(new java.awt.Color(255, 0, 255, 255));
                                g2.setStroke(new java.awt.BasicStroke(4));
                                g2.drawRect(screenX + tileManager.tile[tileNum].hitBox.x,
                                        screenY + tileManager.tile[tileNum].hitBox.y,
                                        tileManager.tile[tileNum].hitBox.width,
                                        tileManager.tile[tileNum].hitBox.height);
                                g2.setStroke(new java.awt.BasicStroke(2));
                                g2.setColor(Color.RED);
                            } else if (debugTuner && ((tunerTarget == 2 && tileNum == 7)
                                    || (tunerTarget == 2 && tileNum == 40) || (tunerTarget == 3 && tileNum == 44))) {
                                g2.setColor(new java.awt.Color(255, 0, 255, 180));
                                g2.drawRect(screenX + tileManager.tile[tileNum].hitBox.x,
                                        screenY + tileManager.tile[tileNum].hitBox.y,
                                        tileManager.tile[tileNum].hitBox.width,
                                        tileManager.tile[tileNum].hitBox.height);
                                g2.setColor(Color.RED);
                            }
                        } else {
                            g2.drawRect(screenX, screenY, tileSize, tileSize);
                        }
                    }
                }
            }
        }

        // Draw object hit boxes
        g2.setColor(Color.BLUE);
        for (int i = 0; i < obj[currentMap].length; i++) {
            if (obj[currentMap][i] != null) {
                Entity object = obj[currentMap][i];
                int screenX = object.worldX - player.worldX + player.screenX;
                int screenY = object.worldY - player.worldY + player.screenY;

                // Only draw if on screen
                if (screenX > -tileSize && screenX < screenWidth &&
                        screenY > -tileSize && screenY < screenHeight) {
                    g2.drawRect(screenX + object.solidArea.x,
                            screenY + object.solidArea.y,
                            object.solidArea.width,
                            object.solidArea.height);
                    // Highlight the selected object when tuner is set to objects
                    if (debugTuner && tunerTarget == 6) {
                        // collect indices to find the one matching tunerSubIndex
                        java.util.ArrayList<Integer> matches = new java.util.ArrayList<>();
                        for (int j = 0; j < obj[currentMap].length; j++) {
                            if (obj[currentMap][j] != null)
                                matches.add(j);
                        }
                        if (matches.size() > 0) {
                            int pick = Math.min(tunerSubIndex, matches.size() - 1);
                            int actual = matches.get(pick);
                            if (actual == i) {
                                g2.setColor(new java.awt.Color(255, 0, 255, 180));
                                g2.drawRect(screenX + object.solidArea.x, screenY + object.solidArea.y,
                                        object.solidArea.width, object.solidArea.height);
                                g2.setColor(Color.BLUE);
                            }
                        }
                    }
                }
            }
        }

        // Draw entity hit boxes (NPCs, monsters, player)
        g2.setColor(Color.GREEN);

        // Player hit box
        g2.drawRect(player.screenX + player.solidArea.x,
                player.screenY + player.solidArea.y,
                player.solidArea.width,
                player.solidArea.height);

        // If tuner is active, draw tuner overlay for the player
        if (debugTuner && tunerTarget == 0) {
            g2.setColor(new java.awt.Color(255, 0, 255, 180));
            g2.drawRect(player.screenX + player.solidArea.x, player.screenY + player.solidArea.y,
                    player.solidArea.width, player.solidArea.height);
            g2.setColor(Color.GREEN);
        }

        // Always show player sprite boundary (for comparison with hitbox)
        if (debugMode) {
            g2.setColor(new Color(0, 255, 0, 100));
            g2.drawRect(player.screenX, player.screenY, tileSize, tileSize);
            g2.setColor(Color.GREEN);
        }

        // NPC hit boxes
        for (int i = 0; i < npc[currentMap].length; i++) {
            if (npc[currentMap][i] != null) {
                Entity entity = npc[currentMap][i];
                int screenX = entity.worldX - player.worldX + player.screenX;
                int screenY = entity.worldY - player.worldY + player.screenY;

                if (screenX > -tileSize && screenX < screenWidth &&
                        screenY > -tileSize && screenY < screenHeight) {
                    g2.drawRect(screenX + entity.solidArea.x,
                            screenY + entity.solidArea.y,
                            entity.solidArea.width,
                            entity.solidArea.height);

                    if (debugTuner && tunerTarget == 4) {
                        g2.setColor(new java.awt.Color(255, 0, 255, 180));
                        g2.drawRect(screenX + entity.solidArea.x, screenY + entity.solidArea.y,
                                entity.solidArea.width, entity.solidArea.height);
                        g2.setColor(Color.GREEN);
                    }
                }
            }
        }

        // Monster hit boxes
        for (int i = 0; i < monster[currentMap].length; i++) {
            if (monster[currentMap][i] != null) {
                Entity entity = monster[currentMap][i];
                int screenX = entity.worldX - player.worldX + player.screenX;
                int screenY = entity.worldY - player.worldY + player.screenY;

                if (screenX > -tileSize && screenX < screenWidth &&
                        screenY > -tileSize && screenY < screenHeight) {
                    g2.drawRect(screenX + entity.solidArea.x,
                            screenY + entity.solidArea.y,
                            entity.solidArea.width,
                            entity.solidArea.height);

                    if (debugTuner && tunerTarget == 5) {
                        g2.setColor(new java.awt.Color(255, 0, 255, 180));
                        g2.drawRect(screenX + entity.solidArea.x, screenY + entity.solidArea.y,
                                entity.solidArea.width, entity.solidArea.height);
                        g2.setColor(Color.GREEN);
                    }
                }
            }
        }

        // Projectile hit boxes
        g2.setColor(Color.YELLOW);
        for (int i = 0; i < projectileList.size(); i++) {
            if (projectileList.get(i) != null) {
                Entity projectile = projectileList.get(i);
                int screenX = projectile.worldX - player.worldX + player.screenX;
                int screenY = projectile.worldY - player.worldY + player.screenY;

                if (screenX > -tileSize && screenX < screenWidth &&
                        screenY > -tileSize && screenY < screenHeight) {
                    g2.drawRect(screenX + projectile.solidArea.x,
                            screenY + projectile.solidArea.y,
                            projectile.solidArea.width,
                            projectile.solidArea.height);

                    // projectiles not part of tuner currently
                }
            }
        }

        // Draw collision debug marker (red X where collision happened)
        if (lastCollisionTime > 0 && System.currentTimeMillis() - lastCollisionTime < 2000) {
            int screenX = lastCollisionWorldX - player.worldX + player.screenX;
            int screenY = lastCollisionWorldY - player.worldY + player.screenY;
            g2.setColor(new Color(255, 0, 0, 200));
            g2.setStroke(new java.awt.BasicStroke(3));
            // Draw an X marker
            g2.drawLine(screenX - 8, screenY - 8, screenX + 8, screenY + 8);
            g2.drawLine(screenX - 8, screenY + 8, screenX + 8, screenY - 8);
        }

        // Restore original graphics settings
        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
    }

    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stopMusic() {
        music.stop();
    }

    public void play(int i) {
        soundEffect.setFile(i);
        soundEffect.play();
    }
}
