package src.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    GamePanel gamePanel;
    // Key State Flags
    public boolean upPressed, downPressed, leftPressed, rightPressed, actionPressed, shotPressed, enterPressed;

    // Constructor init with gamePanel
    public KeyHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    };

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // Handles Key presses based on game state
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (gamePanel.gameState == gamePanel.titleState) {
            titleState(code);
        } else if (gamePanel.gameState == gamePanel.playState) {
            playState(code);
        } else if (gamePanel.gameState == gamePanel.pauseState) {
            pauseState(code);
        } else if (gamePanel.gameState == gamePanel.dialogueState) {
            dialogueState(code);
        } else if (gamePanel.gameState == gamePanel.characterStatus) {
            characterStatus(code);
        } else if (gamePanel.gameState == gamePanel.optionsState) {
            optionsState(code);
        } else if (gamePanel.gameState == gamePanel.gameOverState) {
            gameOverState(code);
        } else if (gamePanel.gameState == gamePanel.tradeState) {
            tradeState(code);
        }
    }

    // Handles title screen inputs
    public void titleState(int code) {
        if (gamePanel.ui.titleScreenState == 0) {
            if (code == KeyEvent.VK_W) {
                gamePanel.ui.commandNum--;
                if (gamePanel.ui.commandNum < 0) {
                    gamePanel.ui.commandNum = 2; // wrap to last (Quit)
                }
            }
            if (code == KeyEvent.VK_S) {
                gamePanel.ui.commandNum++;
                if (gamePanel.ui.commandNum > 2) {
                    gamePanel.ui.commandNum = 0;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gamePanel.ui.commandNum == 0) {
                    gamePanel.ui.titleScreenState = 1;
                }
                if (gamePanel.ui.commandNum == 1) {
                    // Open the save slots screen so player picks which save to load
                    gamePanel.ui.titleScreenState = 2; // custom save slots view
                    gamePanel.ui.commandNum = 0;
                }
                if (gamePanel.ui.commandNum == 2) {
                    System.exit(0);
                }
            }
        } else if (gamePanel.ui.titleScreenState == 1) {
            if (code == KeyEvent.VK_W) {
                gamePanel.ui.commandNum--;
                if (gamePanel.ui.commandNum < 0) {
                    gamePanel.ui.commandNum = 2;
                }
            }
            if (code == KeyEvent.VK_S) {
                gamePanel.ui.commandNum++;
                if (gamePanel.ui.commandNum > 3) {
                    gamePanel.ui.commandNum = 0;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gamePanel.ui.commandNum == 0) {
                    System.out.println("Fighter Stuff");
                    gamePanel.selectedClass = "Fighter";
                    gamePanel.gameState = gamePanel.playState;
                    // this is a new game session: reset world and create a fresh save
                    gamePanel.restart();
                    // Apply tuned hitboxes after spawning default entities
                    try {
                        gamePanel.keyHandler.loadTunerConfig();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    gamePanel.currentSaveName = null;
                    try {
                        String newName = gamePanel.saveManager.saveAndReturnName(null);
                        gamePanel.currentSaveName = newName;
                        gamePanel.ui.addMessage("New game created: " + newName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        gamePanel.ui.addMessage("Failed to create initial save.");
                    }
                }
                if (gamePanel.ui.commandNum == 1) {
                    System.out.println("Mage Stuff");
                    gamePanel.selectedClass = "Mage";
                    gamePanel.gameState = gamePanel.playState;
                    // new game session: reset and create fresh save
                    gamePanel.restart();
                    // Apply tuned hitboxes after spawning default entities
                    try {
                        gamePanel.keyHandler.loadTunerConfig();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    gamePanel.currentSaveName = null;
                    try {
                        String newName = gamePanel.saveManager.saveAndReturnName(null);
                        gamePanel.currentSaveName = newName;
                        gamePanel.ui.addMessage("New game created: " + newName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        gamePanel.ui.addMessage("Failed to create initial save.");
                    }
                }
                if (gamePanel.ui.commandNum == 2) {
                    System.out.println("Rogue Stuff");
                    gamePanel.selectedClass = "Rogue";
                    gamePanel.gameState = gamePanel.playState;
                    // new game session: reset and create fresh save
                    gamePanel.restart();
                    // Apply tuned hitboxes after spawning default entities
                    try {
                        gamePanel.keyHandler.loadTunerConfig();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    gamePanel.currentSaveName = null;
                    try {
                        String newName = gamePanel.saveManager.saveAndReturnName(null);
                        gamePanel.currentSaveName = newName;
                        gamePanel.ui.addMessage("New game created: " + newName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        gamePanel.ui.addMessage("Failed to create initial save.");
                    }
                }
                if (gamePanel.ui.commandNum == 3) {
                    System.out.println("Cleric Stuff");
                    gamePanel.selectedClass = "Cleric";
                    gamePanel.gameState = gamePanel.playState;
                    // new game session: reset and create fresh save
                    gamePanel.restart();
                    // Apply tuned hitboxes after spawning default entities
                    try {
                        gamePanel.keyHandler.loadTunerConfig();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    gamePanel.currentSaveName = null;
                    try {
                        String newName = gamePanel.saveManager.saveAndReturnName(null);
                        gamePanel.currentSaveName = newName;
                        gamePanel.ui.addMessage("New game created: " + newName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        gamePanel.ui.addMessage("Failed to create initial save.");
                    }
                }
                if (gamePanel.ui.commandNum == 4) {
                    gamePanel.ui.titleScreenState = 0;
                }
            }
        } else if (gamePanel.ui.titleScreenState == 2) {
            // Save slots screen navigation
            // Determine number of entries
            String[] saves;
            try {
                saves = gamePanel.saveManager.listSaves();
            } catch (Exception ex) {
                saves = new String[0];
            }
            int total = saves.length + 1; // +1 for Back

            // If a delete confirmation modal is active, handle it first so Enter doesn't
            // accidentally trigger a load.
            if (gamePanel.ui.deleteConfirmActive) {
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    gamePanel.ui.deleteConfirmChoice = 0; // Yes
                }
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    gamePanel.ui.deleteConfirmChoice = 1; // No
                }

                if (code == KeyEvent.VK_ENTER) {
                    // guard against accidental confirmation from the same key press that
                    // opened the modal (common when using Enter to open and confirm)
                    long now = System.currentTimeMillis();
                    if (now - gamePanel.ui.deleteConfirmOpenedAt < 200) {
                        // accidental confirmation, ignore
                    } else {
                        if (gamePanel.ui.deleteConfirmChoice == 0) {
                            // Confirm deletion
                            String name = gamePanel.ui.deleteConfirmName;
                            // ...removed debug output...
                            try {
                                gamePanel.saveManager.deleteSave(name);
                                gamePanel.ui.addMessage("Deleted save: " + name);
                                // ...removed debug output...
                                if (name.equals(gamePanel.currentSaveName)) {
                                    gamePanel.currentSaveName = null;
                                }
                                if (gamePanel.ui.commandNum > 0)
                                    gamePanel.ui.commandNum--;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                gamePanel.ui.addMessage("Failed to delete save.");
                            }
                        }
                        // Close confirmation in either case
                        gamePanel.ui.deleteConfirmActive = false;
                        gamePanel.ui.deleteConfirmName = null;
                        gamePanel.ui.deleteConfirmChoice = 0;
                    }
                }

                if (code == KeyEvent.VK_ESCAPE) {
                    // Cancel
                    gamePanel.ui.deleteConfirmActive = false;
                    gamePanel.ui.deleteConfirmName = null;
                    gamePanel.ui.deleteConfirmChoice = 0;
                }

                return; // don't process other keys while confirm is active
            }

            if (code == KeyEvent.VK_W) {
                gamePanel.ui.commandNum--;
                if (gamePanel.ui.commandNum < 0)
                    gamePanel.ui.commandNum = total - 1;
            }
            if (code == KeyEvent.VK_S) {
                gamePanel.ui.commandNum++;
                if (gamePanel.ui.commandNum > total - 1)
                    gamePanel.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                int sel = gamePanel.ui.commandNum;
                if (sel < saves.length) {
                    // load selected save name
                    String name = saves[sel];
                    // ...removed debug output...
                    try {
                        // Load will clear and repopulate world arrays from the DB.
                        // Don't call the AssetSetter spawners here or they'll overwrite
                        // saved entities and cause monsters to reappear.
                        gamePanel.saveManager.load(name);
                        // Apply any tuned hitbox values to restored entities
                        // (NPCs/monsters/tiles/player)
                        try {
                            gamePanel.keyHandler.loadTunerConfig();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        gamePanel.player.getPlayerAttackImage();
                        gamePanel.gameState = gamePanel.playState;
                        gamePanel.ui.addMessage("Save loaded: " + name);
                        gamePanel.currentSaveName = name;
                        // ...removed debug output...
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        gamePanel.ui.addMessage("Failed to load save.");
                    }
                } else {
                    // Back
                    gamePanel.ui.titleScreenState = 0;
                    gamePanel.ui.commandNum = 1; // highlight Load Game on title again
                }
            }
            if (code == KeyEvent.VK_D) {
                int sel = gamePanel.ui.commandNum;
                if (sel < saves.length) {
                    // Open confirmation dialog
                    String name = saves[sel];
                    gamePanel.ui.deleteConfirmActive = true;
                    gamePanel.ui.deleteConfirmName = name;
                    gamePanel.ui.deleteConfirmChoice = 1; // default to No
                    gamePanel.ui.deleteConfirmOpenedAt = System.currentTimeMillis();
                }
            }
        }
    }

    // Handles gameplay inputs
    public void playState(int code) {
        if (code == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = true;
        }
        if (code == KeyEvent.VK_C) {
            gamePanel.gameState = gamePanel.characterStatus;
        }
        if (code == KeyEvent.VK_P) {
            if (gamePanel.gameState == gamePanel.playState) {
                gamePanel.gameState = gamePanel.pauseState;
            } else if (gamePanel.gameState == gamePanel.pauseState) {
                gamePanel.gameState = gamePanel.playState;
            }
        }
        if (code == KeyEvent.VK_V) {
            actionPressed = true;
        }
        if (code == KeyEvent.VK_F) {
            shotPressed = true;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            // Open the options menu and initialize its UI state so we don't inherit a
            // stale commandNum/subState from previous menus which can cause
            // duplicate indicators.
            gamePanel.ui.subState = 0;
            gamePanel.ui.commandNum = 0;
            gamePanel.gameState = gamePanel.optionsState;
        }
        if (code == KeyEvent.VK_I) {
            if (gamePanel.inventoryUI != null) {
                gamePanel.inventoryUI.toggle();
            }
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }
        if (code == KeyEvent.VK_T) {
            gamePanel.debugMode = !gamePanel.debugMode;
            gamePanel.showHitBoxes = gamePanel.debugMode;
            System.out.println("Debug Mode: " + (gamePanel.debugMode ? "ON" : "OFF"));
        }
        // Toggle minimap with M
        if (code == KeyEvent.VK_M) {
            gamePanel.tileManager.showMiniMap = !gamePanel.tileManager.showMiniMap;
            System.out.println("Minimap: " + (gamePanel.tileManager.showMiniMap ? "ON" : "OFF"));
            if (gamePanel.tileManager.showMiniMap) {
                try {
                    gamePanel.tileManager.ensureMiniMapBuilt(gamePanel.currentMap);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        // Toggle tuner when debug mode is active
        if (code == KeyEvent.VK_Y && gamePanel.debugMode) {
            gamePanel.debugTuner = !gamePanel.debugTuner;
            System.out.println("Hitbox Tuner: " + (gamePanel.debugTuner ? "ON" : "OFF"));
        }
        // When tuner active, additional controls
        if (gamePanel.debugTuner) {
            // TAB handled by GamePanel-level WHEN_IN_FOCUSED_WINDOW binding to avoid focus
            // traversal issues
            if (code == KeyEvent.VK_COMMA) { // cycle sub-index down
                gamePanel.tunerSubIndex = Math.max(0, gamePanel.tunerSubIndex - 1);
            }
            if (code == KeyEvent.VK_PERIOD) { // cycle sub-index up
                gamePanel.tunerSubIndex++;
            }
            if (code == KeyEvent.VK_1)
                gamePanel.tunerParam = 0; // x
            if (code == KeyEvent.VK_2)
                gamePanel.tunerParam = 1; // y
            if (code == KeyEvent.VK_3)
                gamePanel.tunerParam = 2; // w
            if (code == KeyEvent.VK_4)
                gamePanel.tunerParam = 3; // h
            if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_EQUALS)
                gamePanel.tunerStep = Math.max(1, gamePanel.tunerStep * 2);
            if (code == KeyEvent.VK_MINUS)
                gamePanel.tunerStep = Math.max(1, gamePanel.tunerStep / 2);
            // Adjust values directly with arrow keys
            switch (code) {
                case KeyEvent.VK_UP:
                    adjustTuner(-gamePanel.tunerStep);
                    break;
                case KeyEvent.VK_DOWN:
                    adjustTuner(gamePanel.tunerStep);
                    break;
                case KeyEvent.VK_LEFT:
                    adjustTuner(-gamePanel.tunerStep);
                    break;
                case KeyEvent.VK_RIGHT:
                    adjustTuner(gamePanel.tunerStep);
                    break;
                case KeyEvent.VK_L:
                    saveTunerConfig();
                    break;
                case KeyEvent.VK_R:
                    loadTunerConfig();
                    break;
                default:
                    break;
            }
        }
    }

    // Handles pause state inputs
    public void pauseState(int code) {
        if (code == KeyEvent.VK_P) {
            gamePanel.gameState = gamePanel.playState;
        }
    }

    // Handles dialogue state inputs
    public void dialogueState(int code) {
        if (code == KeyEvent.VK_V) {
            gamePanel.gameState = gamePanel.playState;
        }
    }

    // Handles character screen inputs
    public void characterStatus(int code) {
        if (code == KeyEvent.VK_C) {
            gamePanel.gameState = gamePanel.playState;
        }
        if (code == KeyEvent.VK_V) {
            gamePanel.player.selectItem();
        }
        playerInventory(code);
    }

    // Handles options menu inputs
    public void optionsState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gamePanel.gameState = gamePanel.playState;
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }

        int maxCommandNum = 0;
        switch (gamePanel.ui.subState) {
            case 0:
                // optionsTop now has: FullScreen(0), Music(1), Sound(2), Save(3), Controls(4),
                // EndGame(5), Close(6)
                maxCommandNum = 6;
                break;
            case 3:
                maxCommandNum = 1;
                break;
        }

        if (code == KeyEvent.VK_W) {
            gamePanel.ui.commandNum--;
            gamePanel.play(8);
            if (gamePanel.ui.commandNum < 0) {
                gamePanel.ui.commandNum = maxCommandNum;
            }
        }
        if (code == KeyEvent.VK_S) {
            gamePanel.ui.commandNum++;
            gamePanel.play(8);
            if (gamePanel.ui.commandNum > maxCommandNum) {
                gamePanel.ui.commandNum = 0;
            }
        }
        if (code == KeyEvent.VK_A) {
            if (gamePanel.ui.subState == 0) {
                if (gamePanel.ui.commandNum == 1 && gamePanel.music.volumeScale > 0) {
                    gamePanel.music.volumeScale--;
                    gamePanel.music.checkVolume();
                    gamePanel.play(8);
                }
                if (gamePanel.ui.commandNum == 2 && gamePanel.soundEffect.volumeScale > 0) {
                    gamePanel.soundEffect.volumeScale--;
                    gamePanel.play(8);
                }
            }
        }
        if (code == KeyEvent.VK_D) {
            if (gamePanel.ui.subState == 0) {
                if (gamePanel.ui.commandNum == 1 && gamePanel.music.volumeScale < 100) {
                    gamePanel.music.volumeScale++;
                    gamePanel.music.checkVolume();
                    gamePanel.play(8);
                }
                if (gamePanel.ui.commandNum == 2 && gamePanel.soundEffect.volumeScale < 100) {
                    gamePanel.soundEffect.volumeScale++;
                    gamePanel.play(8);
                }
            }
        }

        // Handle Enter actions for options (save, end game, etc.)
        if (code == KeyEvent.VK_ENTER && gamePanel.ui.subState == 0) {
            int cmd = gamePanel.ui.commandNum;
            if (cmd == 3) { // Save Game
                try {
                    String usedName;
                    if (gamePanel.currentSaveName == null || gamePanel.currentSaveName.isBlank()) {
                        usedName = gamePanel.saveManager.saveAndReturnName(null);
                        gamePanel.currentSaveName = usedName;
                    } else {
                        gamePanel.saveManager.save(gamePanel.currentSaveName);
                        usedName = gamePanel.currentSaveName;
                    }
                    gamePanel.ui.addMessage("Game saved: " + usedName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    gamePanel.ui.addMessage("Failed to save game.");
                }
            }
        }
    }

    // Handles game over state
    public void gameOverState(int code) {
        if (code == KeyEvent.VK_W) {
            gamePanel.ui.commandNum--;
            if (gamePanel.ui.commandNum < 0) {
                gamePanel.ui.commandNum = 1;
            }
            gamePanel.play(8);
        }
        if (code == KeyEvent.VK_S) {
            gamePanel.ui.commandNum++;
            if (gamePanel.ui.commandNum > 1) {
                gamePanel.ui.commandNum = 0;
            }
            gamePanel.play(8);
        }
        if (code == KeyEvent.VK_ENTER) {
            if (gamePanel.ui.commandNum == 0) {
                gamePanel.gameState = gamePanel.playState;
                gamePanel.retry();
                gamePanel.play(0);
            }
            if (gamePanel.ui.commandNum == 1) {
                gamePanel.ui.titleScreenState = 0;
                gamePanel.ui.commandNum = 0;
                gamePanel.gameState = gamePanel.titleState;
                gamePanel.restart();
            }
        }
    }

    // Handles trade menu inputs
    public void tradeState(int code) {
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }

        if (gamePanel.ui.subState == 0) {
            if (code == KeyEvent.VK_W) {
                gamePanel.ui.commandNum--;
                if (gamePanel.ui.commandNum < 0) {
                    gamePanel.ui.commandNum = 2;
                }
                gamePanel.play(8);
            }
            if (code == KeyEvent.VK_S) {
                gamePanel.ui.commandNum++;
                if (gamePanel.ui.commandNum > 2) {
                    gamePanel.ui.commandNum = 0;
                }
                gamePanel.play(8);
            }
        }
        if (gamePanel.ui.subState == 1) {
            npcInventory(code);
            if (code == KeyEvent.VK_ESCAPE) {
                gamePanel.ui.subState = 0;
            }
        }
        if (gamePanel.ui.subState == 2) {
            playerInventory(code);
            if (code == KeyEvent.VK_ESCAPE) {
                gamePanel.ui.subState = 0;
            }
        }
    }

    public void playerInventory(int code) {
        if (code == KeyEvent.VK_W) {
            if (gamePanel.ui.playerSlotRow != 0) {
                gamePanel.ui.playerSlotRow--;
                gamePanel.play(8);
            }

        }
        if (code == KeyEvent.VK_A) {
            if (gamePanel.ui.playerSlotCol != 0) {
                gamePanel.ui.playerSlotCol--;
                gamePanel.play(8);
            }
        }
        if (code == KeyEvent.VK_S) {
            if (gamePanel.ui.playerSlotRow != 3) {
                gamePanel.ui.playerSlotRow++;
                gamePanel.play(8);
            }
        }
        if (code == KeyEvent.VK_D) {
            if (gamePanel.ui.playerSlotCol != 4) {
                gamePanel.ui.playerSlotCol++;
                gamePanel.play(8);
            }
        }
    }

    public void npcInventory(int code) {
        if (code == KeyEvent.VK_W) {
            if (gamePanel.ui.npcSlotRow != 0) {
                gamePanel.ui.npcSlotRow--;
                gamePanel.play(8);
            }

        }
        if (code == KeyEvent.VK_A) {
            if (gamePanel.ui.npcSlotCol != 0) {
                gamePanel.ui.npcSlotCol--;
                gamePanel.play(8);
            }
        }
        if (code == KeyEvent.VK_S) {
            if (gamePanel.ui.npcSlotRow != 3) {
                gamePanel.ui.npcSlotRow++;
                gamePanel.play(8);
            }
        }
        if (code == KeyEvent.VK_D) {
            if (gamePanel.ui.npcSlotCol != 4) {
                gamePanel.ui.npcSlotCol++;
                gamePanel.play(8);
            }
        }
    }

    // Reset key inputs when key is released
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }
        if (code == KeyEvent.VK_F) {
            shotPressed = false;
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = false;
        }
    }

    // --- Tuner helper methods ---
    private void adjustTuner(int delta) {
        switch (gamePanel.tunerTarget) {
            case 0: // player
                applyToRectAndDefaults(gamePanel.player, gamePanel.player.solidArea, gamePanel.tunerParam, delta);
                break;
            case 1: // water tiles (35, 45, 46, 47)
                // Apply to all water tiles - use tile 35 as the representative for tuning
                applyToTileHitBox(35, gamePanel.tunerParam, delta);
                applyToTileHitBox(45, gamePanel.tunerParam, delta);
                applyToTileHitBox(46, gamePanel.tunerParam, delta);
                applyToTileHitBox(47, gamePanel.tunerParam, delta);
                break;
            case 2: // tile 40 (wall)
                applyToTileHitBox(40, gamePanel.tunerParam, delta);
                break;
            case 3: // tile 44 (table)
                applyToTileHitBox(44, gamePanel.tunerParam, delta);
                break;
            case 4: // oldman npc
                // collect matching OldMan NPC indices
                java.util.ArrayList<Integer> npcMatches = new java.util.ArrayList<>();
                for (int i = 0; i < gamePanel.npc[gamePanel.currentMap].length; i++) {
                    if (gamePanel.npc[gamePanel.currentMap][i] != null
                            && "OldMan".equals(gamePanel.npc[gamePanel.currentMap][i].name)) {
                        npcMatches.add(i);
                    }
                }
                if (npcMatches.size() > 0) {
                    int pick = Math.min(gamePanel.tunerSubIndex, npcMatches.size() - 1);
                    int actualIndex = npcMatches.get(pick);
                    applyToRectAndDefaults(gamePanel.npc[gamePanel.currentMap][actualIndex],
                            gamePanel.npc[gamePanel.currentMap][actualIndex].solidArea, gamePanel.tunerParam, delta);
                }
                break;
            case 5: // green slime
                // collect matching GreenSlime monster indices
                java.util.ArrayList<Integer> monMatches = new java.util.ArrayList<>();
                for (int i = 0; i < gamePanel.monster[gamePanel.currentMap].length; i++) {
                    if (gamePanel.monster[gamePanel.currentMap][i] != null
                            && "GreenSlime".equals(gamePanel.monster[gamePanel.currentMap][i].name)) {
                        monMatches.add(i);
                    }
                }
                if (monMatches.size() > 0) {
                    int pick = Math.min(gamePanel.tunerSubIndex, monMatches.size() - 1);
                    int actualIndex = monMatches.get(pick);
                    applyToRectAndDefaults(gamePanel.monster[gamePanel.currentMap][actualIndex],
                            gamePanel.monster[gamePanel.currentMap][actualIndex].solidArea, gamePanel.tunerParam,
                            delta);
                }
                break;
            case 6: // world objects
                java.util.ArrayList<Integer> objMatches = new java.util.ArrayList<>();
                for (int i = 0; i < gamePanel.obj[gamePanel.currentMap].length; i++) {
                    if (gamePanel.obj[gamePanel.currentMap][i] != null) {
                        objMatches.add(i);
                    }
                }
                if (objMatches.size() > 0) {
                    int pick = Math.min(gamePanel.tunerSubIndex, objMatches.size() - 1);
                    int actualIndex = objMatches.get(pick);
                    applyToRectAndDefaults(gamePanel.obj[gamePanel.currentMap][actualIndex],
                            gamePanel.obj[gamePanel.currentMap][actualIndex].solidArea, gamePanel.tunerParam,
                            delta);
                }
                break;
            case 7: // BoneMender NPC(s)
                java.util.ArrayList<Integer> boneMatches = new java.util.ArrayList<>();
                for (int i = 0; i < gamePanel.npc[gamePanel.currentMap].length; i++) {
                    if (gamePanel.npc[gamePanel.currentMap][i] != null
                            && "BoneMender".equals(gamePanel.npc[gamePanel.currentMap][i].name)) {
                        boneMatches.add(i);
                    }
                }
                if (boneMatches.size() > 0) {
                    int pick = Math.min(gamePanel.tunerSubIndex, boneMatches.size() - 1);
                    int actualIndex = boneMatches.get(pick);
                    applyToRectAndDefaults(gamePanel.npc[gamePanel.currentMap][actualIndex],
                            gamePanel.npc[gamePanel.currentMap][actualIndex].solidArea, gamePanel.tunerParam, delta);
                }
                break;
            default:
                break;
        }
    }

    // Apply to rectangle without entity default updates
    private void applyToRect(java.awt.Rectangle r, int param, int delta) {
        switch (param) {
            case 0:
                r.x += delta;
                break;
            case 1:
                r.y += delta;
                break;
            case 2:
                r.width = Math.max(1, r.width + delta);
                break;
            case 3:
                r.height = Math.max(1, r.height + delta);
                break;
            default:
                break;
        }
    }

    // Apply changes and update the entity's default solidArea offsets so changes
    // persist
    private void applyToRectAndDefaults(src.entity.Entity e, java.awt.Rectangle r, int param, int delta) {
        applyToRect(r, param, delta);
        // Update defaults so any reset logic (that uses defaults) will preserve tuned
        // values
        e.solidAreaDefaultX = r.x;
        e.solidAreaDefaultY = r.y;
    }

    private void applyToTileHitBox(int index, int param, int delta) {
        if (index < 0 || index >= gamePanel.tileManager.tile.length)
            return;
        if (gamePanel.tileManager.tile[index] == null) {
            System.out.println("[Tuner] tile[" + index + "] is null, cannot adjust hitbox");
            return;
        }
        java.awt.Rectangle r = gamePanel.tileManager.tile[index].hitBox;
        if (r == null) {
            System.out.println("[Tuner] tile[" + index + "].hitBox is null, cannot adjust hitbox");
            return;
        }
        applyToRect(r, param, delta);
        // Only log first adjustment to avoid spam
        if (delta != 0) {
            System.out.println("[Tuner] Adjusted tile[" + index + "] hitBox: (" + r.x + "," + r.y + "," + r.width + ","
                    + r.height + ")");
        }
    }

    public void saveTunerConfig() {
        // Only save to DB as global defaults (saveName null). Build a map keyed by the
        // same keys
        try {
            java.util.Map<String, int[]> map = new java.util.HashMap<>();
            java.awt.Rectangle p2 = gamePanel.player.solidArea;
            map.put("player", new int[] { p2.x, p2.y, p2.width, p2.height });
            // Save water tiles and other tiles with null checks
            if (gamePanel.tileManager.tile[35] != null && gamePanel.tileManager.tile[35].hitBox != null) {
                java.awt.Rectangle tt35 = gamePanel.tileManager.tile[35].hitBox;
                map.put("tile35", new int[] { tt35.x, tt35.y, tt35.width, tt35.height });
            }
            if (gamePanel.tileManager.tile[45] != null && gamePanel.tileManager.tile[45].hitBox != null) {
                java.awt.Rectangle tt45 = gamePanel.tileManager.tile[45].hitBox;
                map.put("tile45", new int[] { tt45.x, tt45.y, tt45.width, tt45.height });
            }
            if (gamePanel.tileManager.tile[46] != null && gamePanel.tileManager.tile[46].hitBox != null) {
                java.awt.Rectangle tt46 = gamePanel.tileManager.tile[46].hitBox;
                map.put("tile46", new int[] { tt46.x, tt46.y, tt46.width, tt46.height });
            }
            if (gamePanel.tileManager.tile[47] != null && gamePanel.tileManager.tile[47].hitBox != null) {
                java.awt.Rectangle tt47 = gamePanel.tileManager.tile[47].hitBox;
                map.put("tile47", new int[] { tt47.x, tt47.y, tt47.width, tt47.height });
            }
            if (gamePanel.tileManager.tile[40] != null && gamePanel.tileManager.tile[40].hitBox != null) {
                java.awt.Rectangle tt40 = gamePanel.tileManager.tile[40].hitBox;
                map.put("tile40", new int[] { tt40.x, tt40.y, tt40.width, tt40.height });
            }
            if (gamePanel.tileManager.tile[44] != null && gamePanel.tileManager.tile[44].hitBox != null) {
                java.awt.Rectangle tt44 = gamePanel.tileManager.tile[44].hitBox;
                map.put("tile44", new int[] { tt44.x, tt44.y, tt44.width, tt44.height });
            }
            // class-wide npc defaults
            java.util.Set<String> done = new java.util.HashSet<>();
            for (int m = 0; m < gamePanel.maxMap; m++) {
                for (int i = 0; i < gamePanel.npc[m].length; i++) {
                    if (gamePanel.npc[m][i] != null) {
                        String key = gamePanel.npc[m][i].getClass().getSimpleName();
                        if (!done.contains(key)) {
                            java.awt.Rectangle r = gamePanel.npc[m][i].solidArea;
                            map.put("class_npc=" + key, new int[] { r.x, r.y, r.width, r.height });
                            done.add(key);
                        }
                    }
                }
            }
            // class-wide item/object defaults: include any present in world plus a
            // canonical list
            done.clear();
            for (int m = 0; m < gamePanel.maxMap; m++) {
                for (int i = 0; i < gamePanel.obj[m].length; i++) {
                    if (gamePanel.obj[m][i] != null) {
                        String key = gamePanel.obj[m][i].getClass().getSimpleName();
                        if (!done.contains(key)) {
                            java.awt.Rectangle r = gamePanel.obj[m][i].solidArea;
                            map.put("class_item=" + key, new int[] { r.x, r.y, r.width, r.height });
                            done.add(key);
                        }
                    }
                }
            }
            // Also ensure common object classes are saved even if not currently present
            String[] commonObjects = new String[] {
                    "OBJ_Coin_Bronze", "OBJ_Heart", "OBJ_Potion_Red", "OBJ_Mana_Crystal",
                    "OBJ_Sword_Normal", "OBJ_Shield_Wood", "OBJ_Boots", "OBJ_Axe", "OBJ_Key"
            };
            for (String className : commonObjects) {
                if (done.contains(className))
                    continue;
                try {
                    src.entity.Entity sample = gamePanel.assetSetter.createItemByName(className);
                    if (sample != null) {
                        java.awt.Rectangle r = sample.solidArea;
                        map.put("class_item=" + className, new int[] { r.x, r.y, r.width, r.height });
                        done.add(className);
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
            // class-wide monster defaults
            done.clear();
            for (int m = 0; m < gamePanel.maxMap; m++) {
                for (int i = 0; i < gamePanel.monster[m].length; i++) {
                    if (gamePanel.monster[m][i] != null) {
                        String key = gamePanel.monster[m][i].getClass().getSimpleName();
                        if (!done.contains(key)) {
                            java.awt.Rectangle r = gamePanel.monster[m][i].solidArea;
                            map.put("class_mon=" + key, new int[] { r.x, r.y, r.width, r.height });
                            done.add(key);
                        }
                    }
                }
            }
            // save global (saveName = null)
            gamePanel.saveManager.saveTunerConfigToDb(map, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ...existing code...

    public void loadTunerConfig() {
        // Only load from DB-config, do not fall back to file
        try {
            java.util.Map<String, int[]> map = gamePanel.saveManager.loadTunerConfigFromDb(null);
            if (map != null && !map.isEmpty()) {
                // apply entries
                for (java.util.Map.Entry<String, int[]> e : map.entrySet()) {
                    String key = e.getKey();
                    int[] v = e.getValue();
                    try {
                        if (key.equals("player")) {
                            java.awt.Rectangle p = gamePanel.player.solidArea;
                            p.x = v[0];
                            p.y = v[1];
                            p.width = v[2];
                            p.height = v[3];
                            gamePanel.player.solidAreaDefaultX = p.x;
                            gamePanel.player.solidAreaDefaultY = p.y;
                        } else if (key.startsWith("tile")) {
                            int idx = Integer.parseInt(key.substring(4));
                            java.awt.Rectangle r = gamePanel.tileManager.tile[idx].hitBox;
                            r.x = v[0];
                            r.y = v[1];
                            r.width = v[2];
                            r.height = v[3];
                        } else if (key.startsWith("class_npc=")) {
                            String className = key.substring("class_npc=".length());
                            for (int m = 0; m < gamePanel.maxMap; m++) {
                                for (int i = 0; i < gamePanel.npc[m].length; i++) {
                                    if (gamePanel.npc[m][i] != null
                                            && gamePanel.npc[m][i].getClass().getSimpleName().equals(className)) {
                                        java.awt.Rectangle r = gamePanel.npc[m][i].solidArea;
                                        r.x = v[0];
                                        r.y = v[1];
                                        r.width = v[2];
                                        r.height = v[3];
                                        gamePanel.npc[m][i].solidAreaDefaultX = r.x;
                                        gamePanel.npc[m][i].solidAreaDefaultY = r.y;
                                    }
                                }
                            }
                        } else if (key.startsWith("class_item=")) {
                            String className = key.substring("class_item=".length());
                            for (int m = 0; m < gamePanel.maxMap; m++) {
                                for (int i = 0; i < gamePanel.obj[m].length; i++) {
                                    if (gamePanel.obj[m][i] != null
                                            && gamePanel.obj[m][i].getClass().getSimpleName().equals(className)) {
                                        java.awt.Rectangle r = gamePanel.obj[m][i].solidArea;
                                        r.x = v[0];
                                        r.y = v[1];
                                        r.width = v[2];
                                        r.height = v[3];
                                    }
                                }
                            }
                        } else if (key.startsWith("class_mon=")) {
                            String className = key.substring("class_mon=".length());
                            for (int m = 0; m < gamePanel.maxMap; m++) {
                                for (int i = 0; i < gamePanel.monster[m].length; i++) {
                                    if (gamePanel.monster[m][i] != null
                                            && gamePanel.monster[m][i].getClass().getSimpleName().equals(className)) {
                                        java.awt.Rectangle r = gamePanel.monster[m][i].solidArea;
                                        r.x = v[0];
                                        r.y = v[1];
                                        r.width = v[2];
                                        r.height = v[3];
                                        gamePanel.monster[m][i].solidAreaDefaultX = r.x;
                                        gamePanel.monster[m][i].solidAreaDefaultY = r.y;
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // skip malformed entries
                    }
                }
                System.out.println("Tuner config loaded.");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
