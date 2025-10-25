package src.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.ArrayList;

import src.entity.Entity;
import src.object.OBJ_Coin_Bronze;
import src.object.OBJ_Heart;
import src.object.OBJ_Mana_Crystal;

public class UI {
    GamePanel gamePanel;
    Graphics2D g2;
    // Base game font (Alkhemikal). All UI text derives from this when available.
    Font alkhemikal;
    // Legacy fonts kept for fallback only; no longer used as primary.
    Font purisa;
    Font P22DaVinci;
    Font Thomson;
    Font Frozen_Solid;
    Font Arial;
    Font Windsong;
    Font Hobbiton;
    Font Roots;
    BufferedImage full_heart, half_heart, empty_heart, full_crystal, empty_crystal, coin, titleStanding,
            titleBackground;
    public boolean messageOn = false;
    ArrayList<String> message = new ArrayList<String>();
    ArrayList<Integer> messageCounter = new ArrayList<Integer>();
    public boolean gameFinished = false; // Game Over State
    public String currentDialogue = ""; // Dialogue
    public int commandNum = 0; // Command Selection
    public int titleScreenState = 0; // Screen Selection
    public int playerSlotCol = 0, playerSlotRow = 0; // Inventory Slot
    public int npcSlotCol = 0, npcSlotRow = 0; // NPC Slot
    int subState = 0; // Sub Screen Selection
    int counter = 0;
    // Save-delete confirmation modal
    public boolean deleteConfirmActive = false;
    public String deleteConfirmName = null;
    public int deleteConfirmChoice = 0; // 0 = Yes, 1 = No
    // time (ms) when the confirmation dialog was opened. Used to ignore accidental
    // immediate Enter presses (key-repeat) that would confirm the dialog.
    public long deleteConfirmOpenedAt = 0L;
    public Entity npc;

    public UI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        // Load Alkhemikal as the global game font
        try {
            InputStream alk = getClass().getResourceAsStream("/res/fonts/Alkhemikal.ttf");
            if (alk == null) {
                System.out.println("Font resource not found: /res/fonts/Alkhemikal.ttf");
            } else {
                alkhemikal = Font.createFont(Font.TRUETYPE_FONT, alk);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        try {
            // Use resource path with forward slashes; getResourceAsStream with leading
            // slash reads from classpath inside jar
            InputStream is = getClass().getResourceAsStream("/res/fonts/Thomson.ttf");
            if (is == null) {
                System.out.println("Font resource not found: /src/res/fonts/Thomson.ttf");
            } else {
                Thomson = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream is2 = getClass().getResourceAsStream("/res/fonts/Hobbiton.ttf");
            if (is2 == null) {
                System.out.println("Font resource not found: /src/res/fonts/Hobbiton.ttf");
            } else {
                Hobbiton = Font.createFont(Font.TRUETYPE_FONT, is2);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream is3 = getClass().getResourceAsStream("/res/fonts/Roots.ttf");
            if (is3 == null) {
                System.out.println("Font resource not found: /src/res/fonts/Roots.ttf");
            } else {
                Roots = Font.createFont(Font.TRUETYPE_FONT, is3);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        // Create HUD Object
        // Get Heart Image
        Entity heart = new OBJ_Heart(gamePanel);
        full_heart = heart.image1;
        half_heart = heart.image2;
        empty_heart = heart.image3;

        // Get Mana Crystal Image
        Entity crystal = new OBJ_Mana_Crystal(gamePanel);
        full_crystal = crystal.image1;
        empty_crystal = crystal.image2;

        // Get Coin Image
        Entity bronzeCoin = new OBJ_Coin_Bronze(gamePanel);
        coin = bronzeCoin.down1;

        // Load title screen image (optional). Scales to a reasonable width.
        try {
            InputStream isTitle = getClass().getResourceAsStream("/res/title/Kneeling_Tree.png");
            if (isTitle == null) {
                System.out.println("Title image not found at /res/title/Kneeling_Tree.png");
            } else {
                BufferedImage raw = ImageIO.read(isTitle);
                if (raw != null) {
                    // Keep a small scaled 'standing' variant and a pre-scaled full-screen
                    // background for layering. Use high-quality scaling with aspect ratio
                    // preserved. The background uses 'cover' mode to fill the screen and
                    // crop excess; the small variant uses 'fit' to keep the whole image.
                    int targetW = gamePanel.tileSize * 6; // small decorative variant
                    int targetH = (int) (raw.getHeight() * (targetW / (double) raw.getWidth()));
                    titleStanding = scaleToCanvas(raw, targetW, targetH, false); // fit

                    // Pre-scale to full screen for background layering. Use cover=true to
                    // fill the screen and crop as needed for consistent background.
                    titleBackground = scaleToCanvas(raw, gamePanel.screenWidth, gamePanel.screenHeight, true);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Ensure fonts are available or fallback to system fonts
        ensureFonts();
    }

    // Ensure fonts are non-null before use; provide safe fallbacks
    private void ensureFonts() {
        if (alkhemikal == null) {
            alkhemikal = new Font("SansSerif", Font.PLAIN, 24);
        }
        if (Roots == null) {
            Roots = new Font("SansSerif", Font.PLAIN, 24);
        }
        if (Hobbiton == null) {
            Hobbiton = new Font("Serif", Font.PLAIN, 20);
        }
        if (Thomson == null) {
            Thomson = new Font("Dialog", Font.PLAIN, 20);
        }
    }

    public void addMessage(String text) {
        message.add(text);
        messageCounter.add(0);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;

        // Set the global base font to Alkhemikal; subsequent deriveFont() calls will
        // keep using this unless explicitly overridden.
        if (alkhemikal != null) {
            g2.setFont(alkhemikal);
        }
        // For pixel-style custom fonts we prefer crisp glyphs over smoothing.
        // Disable text antialiasing and fractional metrics for a sharper, pixel-perfect
        // look.
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setColor(Color.GRAY);
        // Title State
        if (gamePanel.gameState == gamePanel.titleState) {
            drawTitleScreen();
        }
        // Play State
        else if (gamePanel.gameState == gamePanel.playState) {
            drawPlayerLife();
            drawPlayerMana();
            drawMessage();
        } else if (gamePanel.gameState == gamePanel.pauseState) {
            drawPauseScreen();
            drawPlayerLife();
            drawPlayerMana();
        }
        // Dialouge State
        else if (gamePanel.gameState == gamePanel.dialogueState) {
            drawDialogueScreen();
        } else if (gamePanel.gameState == gamePanel.characterStatus) {
            drawCharacterStatus();
            drawInventory(gamePanel.player, true);
        } else if (gamePanel.gameState == gamePanel.optionsState) {
            drawOptionsScreen();
        } else if (gamePanel.gameState == gamePanel.gameOverState) {
            drawGameOverScreen();
        } else if (gamePanel.gameState == gamePanel.transitionState) {
            drawTransitionScreen();
        } else if (gamePanel.gameState == gamePanel.tradeState) {
            drawTradeScreen();
        }
    }

    public void drawPlayerLife() {
        int x = gamePanel.tileSize / 2;
        int y = gamePanel.tileSize / 2;
        int i = 0;
        // Draw Max Hearts
        while (i < gamePanel.player.maxHealth / 2) {
            g2.drawImage(empty_heart, x, y, gamePanel.tileSize, gamePanel.tileSize, null);
            i++;
            x += gamePanel.tileSize;
        }

        x = gamePanel.tileSize / 2;
        y = gamePanel.tileSize / 2;
        i = 0;

        // Draw Full Hearts
        while (i < gamePanel.player.health) {
            g2.drawImage(half_heart, x, y, gamePanel.tileSize, gamePanel.tileSize, null);
            i++;
            if (i < gamePanel.player.health) {
                g2.drawImage(full_heart, x, y, gamePanel.tileSize, gamePanel.tileSize, null);
            }
            i++;
            x += gamePanel.tileSize;
        }
    }

    public void drawPlayerMana() {
        int x = gamePanel.tileSize / 2 - 5;
        int y = (int) (gamePanel.tileSize * 1.5);
        int i = 0;
        // Draw Max Crystals
        while (i < gamePanel.player.maxMana) {
            g2.drawImage(empty_crystal, x, y, null);
            i++;
            if (i < gamePanel.player.maxMana) {
                g2.drawImage(empty_crystal, x, y, null);
            }
            x += 35;
        }

        x = gamePanel.tileSize / 2 - 5;
        y = (int) (gamePanel.tileSize * 1.5);
        i = 0;

        // Draw Full Crystals
        while (i < gamePanel.player.mana) {
            g2.drawImage(full_crystal, x, y, null);
            i++;
            if (i < gamePanel.player.mana) {
                g2.drawImage(full_crystal, x, y, null);
            }
            x += 35;
        }
    }

    public void drawMessage() {
        int messageX, messageY;
        messageX = gamePanel.tileSize;
        messageY = gamePanel.tileSize * 4;
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 36));

        for (int i = 0; i < message.size(); i++) {
            if (message.get(i) != null) {
                g2.setColor(Color.white);
                g2.drawString(message.get(i), messageX + 2, messageY + 2);

                int counter = messageCounter.get(i) + 1;
                messageCounter.set(i, counter);
                messageY += 50;

                if (messageCounter.get(i) > 180) {
                    message.remove(i);
                    messageCounter.remove(i);
                }
            }
        }
    }

    public void drawDialogueScreen() {
        int x = gamePanel.tileSize * 3;
        int y = gamePanel.tileSize / 2;
        int width = gamePanel.screenWidth - gamePanel.tileSize * 6;
        int height = gamePanel.tileSize * 4;

        drawSubWindow(x, y, width, height);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 56));
        x += gamePanel.tileSize;
        y += gamePanel.tileSize + 10;

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, x, y);
            y += gamePanel.tileSize;
        }
    }

    public void drawCharacterStatus() {
        // Draw Frame
        final int frameX, frameY, frameWidth, frameHeight;
        frameX = gamePanel.tileSize * 2;
        frameY = gamePanel.tileSize * 1;
        frameWidth = gamePanel.tileSize * 5;
        frameHeight = gamePanel.tileSize * 10;

        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // Draw Character Info
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30));

        int textX = frameX + 20;
        int textY = frameY + gamePanel.tileSize;
        final int lineHeight = 45;

        // Parameter Names
        g2.drawString("Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Experience", textX, textY);
        textY += lineHeight;
        g2.drawString("Health", textX, textY);
        textY += lineHeight;
        g2.drawString("mana", textX, textY);
        textY += lineHeight;
        g2.drawString("Attack", textX, textY);
        textY += lineHeight;
        g2.drawString("Defense", textX, textY);
        textY += lineHeight;
        g2.drawString("Speed", textX, textY);
        textY += lineHeight;
        g2.drawString("Coins", textX, textY);
        textY += lineHeight + 3;
        g2.drawString("Weapon", textX, textY);
        textY += lineHeight + 3;
        g2.drawString("Shield", textX, textY);
        textY += lineHeight;

        // Values
        int tailX = (frameX + frameWidth) - 30;
        textY = frameY + gamePanel.tileSize;

        String value;

        // Use base font for values
        if (alkhemikal != null)
            g2.setFont(alkhemikal);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30));

        // Level
        value = String.valueOf(gamePanel.player.level);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Experience
        value = String.valueOf(gamePanel.player.exp);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Health
        value = String.valueOf(gamePanel.player.maxHealth);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Mana
        value = String.valueOf(gamePanel.player.maxMana);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Attack
        value = String.valueOf(gamePanel.player.attack);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Defense
        value = String.valueOf(gamePanel.player.defense);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Speed
        value = String.valueOf(gamePanel.player.speed);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Coins
        value = String.valueOf(gamePanel.player.coin);
        textX = getXForAlignToRight(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        // Use base font for labels under the icons as well
        if (alkhemikal != null)
            g2.setFont(alkhemikal);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30));

        g2.drawImage(gamePanel.player.currentWeapon.down1, tailX - gamePanel.tileSize, textY - 35, null);
        textY += gamePanel.tileSize;

        g2.drawImage(gamePanel.player.currentShield.down1, tailX - gamePanel.tileSize, textY - 35, null);
    }

    public void drawInventory(Entity entity, boolean cursor) {

        // Draw Frame
        int frameX, frameY, frameWidth, frameHeight, slotCol, slotRow;

        if (entity == gamePanel.player) {
            frameX = gamePanel.tileSize * 12;
            frameY = gamePanel.tileSize;
            frameWidth = gamePanel.tileSize * 6;
            frameHeight = gamePanel.tileSize * 5;
            slotCol = playerSlotCol;
            slotRow = playerSlotRow;
        } else {
            frameX = gamePanel.tileSize * 2;
            frameY = gamePanel.tileSize;
            frameWidth = gamePanel.tileSize * 6;
            frameHeight = gamePanel.tileSize * 5;
            slotCol = npcSlotCol;
            slotRow = npcSlotRow;
        }

        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // Draw Inventory Slots
        final int slotXStart, slotYStart;
        int slotX, slotY, slotSize;
        slotXStart = frameX + 20;
        slotYStart = frameY + 20;
        slotX = slotXStart;
        slotY = slotYStart;
        slotSize = gamePanel.tileSize + (int) 2.5;

        // Draw Player Items
        for (int i = 0; i < entity.inventory.size(); i++) {

            // Equip Cursor
            if (entity.inventory.get(i) == entity.currentWeapon || entity.inventory.get(i) == entity.currentShield) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, gamePanel.tileSize, gamePanel.tileSize, 10, 10);
            }

            g2.drawImage(entity.inventory.get(i).down1, slotX, slotY, null);
            slotX += slotSize;

            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXStart;
                slotY += slotSize;
            }
        }

        // Cursor
        if (cursor == true) {
            int cursorX, cursorY, cursorWidth, cursorHeight;
            cursorX = slotXStart + (slotSize * slotCol);
            cursorY = slotYStart + (slotSize * slotRow);
            cursorWidth = gamePanel.tileSize;
            cursorHeight = gamePanel.tileSize;

            // Draw Cursor
            g2.setColor(Color.white);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);

            // Draw Item Description Frame
            int dFrameX, dFrameY, dFrameWidth, dFrameHeight;
            dFrameX = frameX;
            dFrameY = frameY + frameHeight;
            dFrameWidth = frameWidth;
            dFrameHeight = gamePanel.tileSize * 3;

            // Draw Item Description
            int textX, textY;
            textX = dFrameX + 20;
            textY = dFrameY + gamePanel.tileSize;
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28));

            int itemIndex = getItemSlotIndex(slotCol, slotRow);

            if (itemIndex < entity.inventory.size()) {
                drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);
                for (String line : entity.inventory.get(itemIndex).description.split("\n")) {
                    g2.drawString(line, textX, textY);
                    textY += 32;
                }
            }
        }
    }

    public void drawOptionsScreen() {
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 34));

        int frameX, frameY, frameWidth, frameHeight;
        frameX = gamePanel.tileSize * 6;
        frameY = gamePanel.tileSize;
        frameWidth = gamePanel.tileSize * 8;
        frameHeight = gamePanel.tileSize * 10;

        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        switch (subState) {
            case 0:
                optionsTop(frameX, frameY);
                break;
            case 1:
                optionsFullScreenNotification(frameX, frameY);
                break;
            case 2:
                optionsControl(frameX, frameY);
                break;
            case 3:
                optionsEndGameConfirmation(frameX, frameY);
                break;
        }

        gamePanel.keyHandler.enterPressed = false;
    }

    public void drawGameOverScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gamePanel.screenWidth, gamePanel.screenHeight);

        int x, y;
        String text;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 100));

        // Shadow Text
        text = "U Dead LUL";
        g2.setColor(Color.BLACK);
        x = getXForCenteredText(text);
        y = gamePanel.tileSize * 4;
        g2.drawString(text, x, y);

        // Main Text
        text = "U Dead LUL";
        g2.setColor(Color.WHITE);
        x = getXForCenteredText(text);
        y = gamePanel.tileSize * 4;
        g2.drawString(text, x - 4, y - 4);

        // Restart Text
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 50));
        text = "Retry";
        x = getXForCenteredText(text);
        y += gamePanel.tileSize * 4;
        g2.drawString(text, x, y);
        if (commandNum == 0) {
            g2.drawString(">", x - 50, y);
        }

        // Quit to Title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 50));
        text = "Give Up";
        x = getXForCenteredText(text);
        y += gamePanel.tileSize;
        g2.drawString(text, x, y);
        if (commandNum == 1) {
            g2.drawString(">", x - 50, y);
        }
    }

    public void optionsTop(int frameX, int frameY) {
        int textX, textY;
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 34));

        String text = "Options";
        textX = getXForCenteredText(text);
        textY = frameY + gamePanel.tileSize;
        g2.drawString(text, textX, textY);

        // Full Screen Option
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 1;
        text = "Full Screen";
        g2.drawString(text, textX, textY);
        if (commandNum == 0) {
            g2.drawString("O", textX - 28, textY);
            if (gamePanel.keyHandler.enterPressed) {
                if (gamePanel.fullScreenOn == false) {
                    gamePanel.fullScreenOn = true;
                } else if (gamePanel.fullScreenOn == true) {
                    gamePanel.fullScreenOn = false;
                }
                subState = 1;
            }
        }

        // Music Option
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 1;
        text = "Music";
        g2.drawString(text, textX, textY);
        if (commandNum == 1) {
            g2.drawString("O", textX - 28, textY);
        }

        // Sound Effects
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 1;
        text = "Sound Effects";
        g2.drawString(text, textX, textY);
        if (commandNum == 2) {
            g2.drawString("O", textX - 28, textY);
        }

        // Save Game (manual)
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 1;
        text = "Save Game";
        g2.drawString(text, textX, textY);
        if (commandNum == 3) {
            g2.drawString("O", textX - 28, textY);
            if (gamePanel.keyHandler.enterPressed) {
                // handled in KeyHandler.optionsState when Enter is pressed
            }
        }

        // Controls
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 1;
        text = "Controls";
        g2.drawString(text, textX, textY);
        if (commandNum == 4) {
            g2.drawString("O", textX - 28, textY);
            if (gamePanel.keyHandler.enterPressed) {
                subState = 2;
                commandNum = 0;
            }
        }

        // End Game
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 1;
        text = "End Game";
        g2.drawString(text, textX, textY);
        if (commandNum == 5) {
            g2.drawString("O", textX - 28, textY);
            if (gamePanel.keyHandler.enterPressed) {
                subState = 3;
                commandNum = 0;
            }
        }

        // Back
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize * 2;
        text = "Close";
        g2.drawString(text, textX, textY);
        if (commandNum == 6) {
            g2.drawString("O", textX - 28, textY);
            if (gamePanel.keyHandler.enterPressed) {
                gamePanel.gameState = gamePanel.playState;
                commandNum = 0;
            }
        }

        // Full Screen Box
        textX = frameX + gamePanel.tileSize * 6;
        textY = frameY + gamePanel.tileSize * 1 + 28;
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(textX, textY, gamePanel.tileSize / 2, gamePanel.tileSize / 2);
        if (gamePanel.fullScreenOn == true) {
            g2.fillRect(textX, textY, gamePanel.tileSize / 2, gamePanel.tileSize / 2);
        }

        // Music Volume Slider
        textY += gamePanel.tileSize;
        g2.drawRect(textX - 36, textY, 100, 24);
        int volumeWidth = (120 / 100) * gamePanel.music.volumeScale;
        g2.fillRect(textX - 36, textY, volumeWidth, 24);

        // Sound Effect Slider
        textY += gamePanel.tileSize;
        g2.drawRect(textX - 36, textY, 100, 24);
        int soundWidth = (120 / 100) * gamePanel.soundEffect.volumeScale;
        g2.fillRect(textX - 36, textY, soundWidth, 24);

        gamePanel.config.saveConfig();
    }

    public int optionsFullScreenNotification(int frameX, int frameY) {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 34));
        int textX, textY;
        textX = frameX + gamePanel.tileSize;
        textY = frameY + gamePanel.tileSize * 3;

        currentDialogue = "You must restart the \ngame for the changes \nto take effect.";

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 46;
        }

        // Back
        textY = frameY + gamePanel.tileSize * 9;
        g2.drawString("Back", textX, textY);
        if (commandNum == 0) {
            g2.drawString("O", textX - 28, textY);
            if (gamePanel.keyHandler.enterPressed) {
                subState = 0;
                commandNum = 0;
            }
        }

        return 0;
    }

    public void optionsControl(int frameX, int frameY) {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 34));
        int textX, textY;

        String text = "Control";
        textX = getXForCenteredText(text);
        textY = frameY + gamePanel.tileSize;
        g2.drawString(text, textX, textY);

        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize;
        g2.drawString("Move", textX, textY);
        textY += gamePanel.tileSize;
        g2.drawString("Attack or Select", textX, textY);
        textY += gamePanel.tileSize;
        g2.drawString("Shoot or Cast", textX, textY);
        textY += gamePanel.tileSize;
        g2.drawString("Character Screen", textX, textY);
        textY += gamePanel.tileSize;
        g2.drawString("Pause", textX, textY);
        textY += gamePanel.tileSize;
        g2.drawString("Options", textX, textY);
        textY += gamePanel.tileSize;

        textX = frameX + gamePanel.tileSize * 6;
        textY = frameY + gamePanel.tileSize * 2;
        g2.drawString("WASD", textX - 10, textY);
        textY += gamePanel.tileSize;
        g2.drawString("V", textX - 10, textY);
        textY += gamePanel.tileSize;
        g2.drawString("F", textX - 10, textY);
        textY += gamePanel.tileSize;
        g2.drawString("C", textX - 10, textY);
        textY += gamePanel.tileSize;
        g2.drawString("P", textX - 10, textY);
        textY += gamePanel.tileSize;
        g2.drawString("ESC", textX - 10, textY);
        textY += gamePanel.tileSize;

        // Back
        textX = frameX + gamePanel.tileSize;
        textY += gamePanel.tileSize;
        g2.drawString("Back", textX, textY);
        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);
            if (gamePanel.keyHandler.enterPressed == true) {
                subState = 0;
                // return to Options top and highlight Controls (index 4)
                commandNum = 4;
            }
        }
    }

    public void optionsEndGameConfirmation(int frameX, int frameY) {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 34));
        int textX, textY;
        textX = frameX + gamePanel.tileSize;
        textY = frameY + gamePanel.tileSize * 3;

        currentDialogue = "Are you sure you want \nto end the game?";

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }

        // Yes
        String text = "Yes";
        textX = getXForCenteredText(text);
        textY += gamePanel.tileSize * 3;
        g2.drawString(text, textX, textY);
        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);
            if (gamePanel.keyHandler.enterPressed == true) {
                subState = 0;
                // Reset title screen state and cursor so the title menu starts at the first
                // option (prevents off-by-one navigation when returning)
                titleScreenState = 0;
                gamePanel.ui.commandNum = 0;
                gamePanel.gameState = gamePanel.titleState;
            }
        }

        // No
        text = "No";
        textX = getXForCenteredText(text);
        textY += gamePanel.tileSize;
        g2.drawString(text, textX, textY);
        if (commandNum == 1) {
            g2.drawString(">", textX - 25, textY);
            if (gamePanel.keyHandler.enterPressed == true) {
                subState = 0;
                // return to Options top and highlight End Game (index 5)
                commandNum = 5;
            }
        }
    }

    public int getItemSlotIndex(int slotCol, int slotRow) {
        int itemIndex = slotRow * 5 + slotCol;
        return itemIndex;
    }

    public void drawTitleScreen() {

        // Title Name
        if (titleScreenState == 0) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 100));
            String text;
            int x;
            int y;

            // Draw title background (full-screen) behind text/menu if available
            if (titleBackground != null) {
                g2.drawImage(titleBackground, 0, 0, null);
                // subtle dark overlay so white text remains readable on bright images
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(0, 0, gamePanel.screenWidth, gamePanel.screenHeight);
            }

            text = "Roots of Eternity";
            x = getXForCenteredText(text);
            y = gamePanel.tileSize * 3;

            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 60));
            text = "New Game";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize * 3.5;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString("T", x - gamePanel.tileSize, y);
                g2.drawString("Y", x + gamePanel.tileSize * 5, y);
            }

            text = "Load Game";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString("L", x - gamePanel.tileSize, y);
                g2.drawString("E", x + gamePanel.tileSize * 5.4F, y);
            }

            text = "Quit";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 2) {
                g2.drawString("R", x - gamePanel.tileSize, y);
                g2.drawString("T", x + gamePanel.tileSize * 2.2F, y);
            }
        } else if (titleScreenState == 1) {
            g2.setColor(Color.white);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 70));

            String text = "Select Your Class";
            int x = getXForCenteredText(text);
            int y = gamePanel.tileSize * 3;
            g2.drawString(text, x, y);

            text = "Warrior";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize * 3;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString("O", x - gamePanel.tileSize, y);
                g2.drawString("O", x + gamePanel.tileSize * 4.4F, y);
            }

            text = "Mage";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString("O", x - gamePanel.tileSize, y);
                g2.drawString("O", x + gamePanel.tileSize * 3.1F, y);
            }

            text = "Rogue";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 2) {
                g2.drawString("O", x - gamePanel.tileSize, y);
                g2.drawString("O", x + gamePanel.tileSize * 3.5F, y);
            }

            text = "Cleric";
            x = getXForCenteredText(text);
            y += gamePanel.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 3) {
                g2.drawString("O", x - gamePanel.tileSize, y);
                g2.drawString("O", x + gamePanel.tileSize * 3.2F, y);
            }
        } else if (titleScreenState == 2) {
            drawSaveSlotsScreen();
        }
    }

    /**
     * Expose the base UI font so other renderers (e.g., GamePanel debug overlays)
     * can use the same typography.
     */
    public Font getBaseFont() {
        if (alkhemikal != null)
            return alkhemikal;
        // fallback to whatever Graphics2D currently uses or a sane default
        return new Font("SansSerif", Font.PLAIN, 12);
    }

    /**
     * Draw a scrollable list of saves returned by SaveManager.listSaves().
     * The UI.commandNum is used as the selection index; the last entry is 'Back'.
     */
    private void drawSaveSlotsScreen() {
        String[] saves;
        try {
            saves = gamePanel.saveManager.listSaves();
        } catch (Exception ex) {
            saves = new String[0];
        }

        int total = saves.length + 1; // +1 for Back option

        int frameX = gamePanel.tileSize * 2;
        int frameY = gamePanel.tileSize * 2;
        int frameWidth = gamePanel.screenWidth - gamePanel.tileSize * 4;
        int frameHeight = gamePanel.screenHeight - gamePanel.tileSize * 4;
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        int x = frameX + gamePanel.tileSize;
        int y = frameY + gamePanel.tileSize;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 44f));
        String title = "Load Game - Save Slots";
        g2.drawString(title, getXForCenteredText(title), y);

        // Use a readable font size for list items and compute spacing with FontMetrics
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 45));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight();

        // top offset under title
        y += gamePanel.tileSize * 1 + lineHeight;

        // visible window settings based on actual line height
        int paddingTop = gamePanel.tileSize;
        int paddingBottom = gamePanel.tileSize;
        int available = frameHeight - paddingTop - paddingBottom - (lineHeight * 2); // leave room for title and hint
        int visible = Math.max(3, Math.max(1, available / lineHeight));

        int sel = commandNum;
        if (sel < 0)
            sel = 0;
        if (sel >= total)
            sel = total - 1;

        int start = Math.max(0, Math.min(sel - visible / 2, Math.max(0, total - visible)));

        // draw saves (with proper spacing so nothing is clipped)
        for (int i = 0; i < visible && (start + i) < total; i++) {
            int idx = start + i;
            String text;
            if (idx < saves.length) {
                text = (idx + 1) + ". " + saves[idx];
            } else {
                text = "Back";
            }
            int tx = x;
            int ty = y + i * lineHeight + fm.getAscent();
            g2.drawString(text, tx, ty);
            if (commandNum == idx) {
                // draw selector a little left of the text
                g2.drawString(">", tx - fm.stringWidth(">") - 8, ty);
            }
        }

        // Draw hint/footer (delete help) at bottom of frame
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 45));
        String hint = "Press D to delete selected save | ENTER to load | Back to return";
        g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 18f));
        int hintY = frameY + frameHeight - gamePanel.tileSize;
        g2.drawString(hint, frameX + gamePanel.tileSize, hintY);

        // If deletion confirmation is active, draw a small confirmation dialog
        if (deleteConfirmActive && deleteConfirmName != null) {
            int w = frameWidth / 2;
            int h = gamePanel.tileSize * 6;
            int dx = frameX + (frameWidth - w) / 2;
            int dy = frameY + (frameHeight - h) / 2;
            drawSubWindow(dx, dy, w, h);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
            String q = "Delete save '" + deleteConfirmName + "'?";
            g2.drawString(q, dx + gamePanel.tileSize, dy + gamePanel.tileSize * 2);

            String yes = "Yes";
            String no = "No";
            int yx = dx + gamePanel.tileSize;
            int yy = dy + gamePanel.tileSize * 4;
            g2.drawString(yes, yx, yy);
            g2.drawString(no, yx + 120, yy);
            if (deleteConfirmChoice == 0) {
                g2.drawString(">", yx - 28, yy);
            } else {
                g2.drawString(">", yx + 120 - 28, yy);
            }
        }
    }

    public void drawPauseScreen() {
        String text = "PAUSED";
        int x = getXForCenteredText(text);
        int y = gamePanel.screenHeight / 2;

        g2.drawString(text, x, y);
    }

    public void drawTransitionScreen() {
        counter++;
        g2.setColor(new Color(0, 0, 0, counter * 5));
        g2.fillRect(0, 0, gamePanel.screenWidth, gamePanel.screenHeight);

        if (counter == 50) {
            counter = 0;
            gamePanel.gameState = gamePanel.playState;
            gamePanel.currentMap = gamePanel.eventHandler.tempMap;
            gamePanel.player.worldX = gamePanel.tileSize * gamePanel.eventHandler.tempCol;
            gamePanel.player.worldY = gamePanel.tileSize * gamePanel.eventHandler.tempRow;
        }
    }

    public void drawTradeScreen() {
        switch (subState) {
            case 0:
                tradeSelect();
                break;
            case 1:
                tradeBuy();
                break;
            case 2:
                tradeSell();
                break;
            default:
                break;
        }
        gamePanel.keyHandler.enterPressed = false;
    }

    public void tradeSelect() {
        drawDialogueScreen();

        int x, y, width, height;
        x = gamePanel.tileSize * 15;
        y = gamePanel.tileSize * 5;
        width = gamePanel.tileSize * 3;
        height = (int) (gamePanel.tileSize * 3.5);
        drawSubWindow(x, y, width, height);

        // Draw Text Options
        x += gamePanel.tileSize;
        y += gamePanel.tileSize;
        g2.drawString("Buy", x, y);
        if (commandNum == 0) {
            g2.drawString(">", x - 24, y);
            if (gamePanel.keyHandler.enterPressed == true) {
                subState = 1;
            }
        }
        y += gamePanel.tileSize;
        g2.drawString("Sell", x, y);
        if (commandNum == 1) {
            g2.drawString(">", x - 24, y);
            if (gamePanel.keyHandler.enterPressed == true) {
                subState = 2;
            }
        }
        y += gamePanel.tileSize;
        g2.drawString("Exit", x, y);
        if (commandNum == 2) {
            g2.drawString(">", x - 24, y);
            if (gamePanel.keyHandler.enterPressed == true) {
                commandNum = 0;
                gamePanel.gameState = gamePanel.dialogueState;
                currentDialogue = "Goodbye!";
            }
        }
    }

    public void tradeSell() {
        drawInventory(gamePanel.player, true);

        int x;
        int y;
        int width;
        int height;

        // Draw Tooltip
        x = gamePanel.tileSize * 2;
        y = gamePanel.tileSize * 9;
        width = gamePanel.tileSize * 6;
        height = gamePanel.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("ESC Back", x + 24, y + 60);

        // Draw Player Currency Window
        x = gamePanel.tileSize * 12;
        y = gamePanel.tileSize * 9;
        width = gamePanel.tileSize * 6;
        height = gamePanel.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("Coin " + gamePanel.player.coin, x + 24, y + 60);

        // Draw Price Window
        int itemIndex = getItemSlotIndex(playerSlotCol, playerSlotRow);
        if (itemIndex < gamePanel.player.inventory.size()) {
            x = (int) (gamePanel.tileSize * 15.5);
            y = (int) (gamePanel.tileSize * 5.5);
            width = (int) (gamePanel.tileSize * 2.5);
            height = gamePanel.tileSize;
            drawSubWindow(x, y, width, height);
            g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

            int price = gamePanel.player.inventory.get(itemIndex).price / 2;
            String text = "" + price;
            x = getXForAlignToRight(text, gamePanel.tileSize * 18 - 20);
            // Coin Price Location
            g2.drawString(text, x, y + 34);

            // Selling Item Config
            if (gamePanel.keyHandler.enterPressed == true) {
                if (gamePanel.player.inventory.get(itemIndex) == gamePanel.player.currentWeapon
                        || gamePanel.player.inventory.get(itemIndex) == gamePanel.player.currentShield) {
                    commandNum = 0;
                    subState = 0;
                    gamePanel.gameState = gamePanel.dialogueState;
                    currentDialogue = "You cannot sell and equipped item";
                } else {
                    gamePanel.player.inventory.remove(itemIndex);
                    gamePanel.player.coin += price;
                }
            }
        }
    }

    public void tradeBuy() {
        // Draw Player Inventory
        drawInventory(gamePanel.player, false);
        // Draw NPC Inventory
        drawInventory(npc, true);

        // Draw Tooltip
        int x = gamePanel.tileSize * 2;
        int y = gamePanel.tileSize * 9;
        int width = gamePanel.tileSize * 6;
        int height = gamePanel.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("ESC Back", x + 24, y + 60);

        // Draw Player Currency Window
        x = gamePanel.tileSize * 12;
        y = gamePanel.tileSize * 9;
        width = gamePanel.tileSize * 6;
        height = gamePanel.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("Coin " + gamePanel.player.coin, x + 24, y + 60);

        // Draw Price Window
        int itemIndex = getItemSlotIndex(npcSlotCol, npcSlotRow);
        if (itemIndex < npc.inventory.size()) {
            x = (int) (gamePanel.tileSize * 5.5);
            y = (int) (gamePanel.tileSize * 5.5);
            width = (int) (gamePanel.tileSize * 2.5);
            height = gamePanel.tileSize;
            drawSubWindow(x, y, width, height);
            g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

            int price = npc.inventory.get(itemIndex).price;
            String text = "" + price;
            x = getXForAlignToRight(text, gamePanel.tileSize * 8 - 20);
            // Coin Price Location
            g2.drawString(text, x, y + 34);

            // Buying Item Config
            if (gamePanel.keyHandler.enterPressed == true) {
                if (npc.inventory.get(itemIndex).price > gamePanel.player.coin) {
                    subState = 0;
                    gamePanel.gameState = gamePanel.dialogueState;
                    currentDialogue = "You need more coins to \nbuy that item!";
                    gamePanel.player.coin += npc.inventory.get(itemIndex).price;
                    drawDialogueScreen();
                }
                if (gamePanel.player.inventory.size() == gamePanel.player.inventorySize) {
                    subState = 0;
                    gamePanel.gameState = gamePanel.dialogueState;
                    currentDialogue = "Inventory is Full!";
                } else {
                    gamePanel.player.coin -= npc.inventory.get(itemIndex).price;
                    gamePanel.player.inventory.add(npc.inventory.get(itemIndex));
                }
            }
        }
    }

    public void drawSubWindow(int x, int y, int width, int height) {
        Color c = new Color(0, 0, 0, 200);
        g2.setColor(c);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255, 255, 255);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    private int getXForCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = gamePanel.screenWidth / 2 - length / 2;
        return x;
    }

    private int getXForAlignToRight(String text, int tailX) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = tailX - length;
        return x;
    }

    /**
     * Scale an image to fit or cover a target canvas while preserving aspect ratio.
     * If cover==false, the image will be fully visible (letterboxed if necessary).
     * If cover==true, the image will fill the canvas and be cropped as needed.
     */
    private BufferedImage scaleToCanvas(BufferedImage src, int canvasW, int canvasH, boolean cover) {
        if (src == null)
            return null;

        double srcW = src.getWidth();
        double srcH = src.getHeight();

        double scaleX = canvasW / srcW;
        double scaleY = canvasH / srcH;

        double scale = cover ? Math.max(scaleX, scaleY) : Math.min(scaleX, scaleY);

        int targetW = Math.max(1, (int) Math.round(srcW * scale));
        int targetH = Math.max(1, (int) Math.round(srcH * scale));

        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = scaled.createGraphics();
        // High-quality rendering hints for nice scaling
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();

        if (!cover) {
            // If fitting, return the scaled image directly; caller can draw it centered.
            return scaled;
        }

        // If covering, crop to canvas size centered
        int x = Math.max(0, (targetW - canvasW) / 2);
        int y = Math.max(0, (targetH - canvasH) / 2);

        BufferedImage cropped = scaled.getSubimage(x, y, Math.min(canvasW, targetW - x),
                Math.min(canvasH, targetH - y));

        // If the cropped size doesn't exactly match canvas (edge cases), draw into a
        // canvas-sized image
        if (cropped.getWidth() != canvasW || cropped.getHeight() != canvasH) {
            BufferedImage finalImg = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = finalImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(cropped, 0, 0, canvasW, canvasH, null);
            g2.dispose();
            return finalImg;
        }

        return cropped;
    }
}
