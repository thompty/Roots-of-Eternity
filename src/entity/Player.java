package src.entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import src.main.GamePanel;
import src.main.KeyHandler;
import src.object.OBJ_Fireball;
import src.object.OBJ_Key;
import src.object.OBJ_Shield_Wood;
import src.object.OBJ_Sword_Normal;

public class Player extends Entity {
    KeyHandler keyHandler;

    public final int screenX;
    public final int screenY;
    public boolean attackCanceled = false;
    public boolean moving = false; // true while movement keys are pressed

    public Player(GamePanel gamePanel, KeyHandler keyHandler) {
        super(gamePanel);
        this.keyHandler = keyHandler;

        screenX = gamePanel.screenWidth / 2 - gamePanel.tileSize / 2;
        screenY = gamePanel.screenHeight / 2 - gamePanel.tileSize / 2;

        // Tuned player hitbox: narrower and bottom-aligned so feet/collision match
        // sprite
        int hitW = 28; // slightly narrower than tile
        int hitH = 36; // a bit shorter for better collision feel
        solidArea = new java.awt.Rectangle((gamePanel.tileSize - hitW) / 2, gamePanel.tileSize - hitH - 6, hitW, hitH);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        setDefaultValues();
        getPlayerImage();
        getPlayerAttackImage();
        setItems();
    }

    public void getPlayerImage() {
        try {
            int width = gamePanel.tileSize;
            int height = gamePanel.tileSize;
            // Scale the player's image
            up0 = setup("res/player/char1/Character_1-3.png", width, height);
            up1 = setup("res/player/char1/Character_1-4.png", width, height);
            up2 = setup("res/player/char1/Character_1-5.png", width, height);
            down0 = setup("res/player/char1/Character_1-0.png", width, height);
            down1 = setup("res/player/char1/Character_1-1.png", width, height);
            down2 = setup("res/player/char1/Character_1-2.png", width, height);
            left0 = setup("res/player/char1/Character_1-9.png", width, height);
            left1 = setup("res/player/char1/Character_1-10.png", width, height);
            left2 = setup("res/player/char1/Character_1-11.png", width, height);
            right0 = setup("res/player/char1/Character_1-6.png", width, height);
            right1 = setup("res/player/char1/Character_1-7.png", width, height);
            right2 = setup("res/player/char1/Character_1-8.png", width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPlayerAttackImage() {

        if (currentWeapon.type == type_sword) {
            try {
                int width = gamePanel.tileSize;
                int height = gamePanel.tileSize * 2;
                // Scale the player's image
                attackUp1 = setup("res/player/char1/Up_Attack_1.png", width, height);
                attackUp2 = setup("res/player/char1/Up_Attack_2.png", width, height);
                attackDown1 = setup("res/player/char1/Down_Attack_1.png", width, height);
                attackDown2 = setup("res/player/char1/Down_Attack_2.png", width, height);

                width = gamePanel.tileSize * 2;
                height = gamePanel.tileSize;

                attackLeft1 = setup("res/player/char1/Left_Attack_1.png", width, height);
                attackLeft2 = setup("res/player/char1/Left_Attack_2.png", width, height);
                attackRight1 = setup("res/player/char1/Right_Attack_1.png", width, height);
                attackRight2 = setup("res/player/char1/Right_Attack_2.png", width, height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (currentWeapon.type == type_axe) {
            try {
                int width = gamePanel.tileSize;
                int height = gamePanel.tileSize * 2;
                // Scale the player's image
                attackUp1 = setup("res/player/boy_axe_up_1.png", width, height);
                attackUp2 = setup("res/player/boy_axe_up_2.png", width, height);
                attackDown1 = setup("res/player/boy_axe_down_1.png", width, height);
                attackDown2 = setup("res/player/boy_axe_down_2.png", width, height);

                width = gamePanel.tileSize * 2;
                height = gamePanel.tileSize;

                attackLeft1 = setup("res/player/boy_axe_left_1.png", width, height);
                attackLeft2 = setup("res/player/boy_axe_left_2.png", width, height);
                attackRight1 = setup("res/player/boy_axe_right_1.png", width, height);
                attackRight2 = setup("res/player/boy_axe_right_2.png", width, height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setDefaultValues() { // Set the player's default values
        worldX = gamePanel.tileSize * 657; // Set the player's position to the center of the map
        worldY = gamePanel.tileSize * 526; // Set the player's position to the center of the map
        speed = 10;
        direction = "down";

        // Player Status
        maxHealth = 6;
        health = maxHealth;
        maxMana = 4;
        mana = maxMana;
        maxAmmo = 10;
        ammo = maxAmmo;
        level = 1;
        strength = 1; // Attack Scaler
        dexterity = 1; // Defense Scaler
        exp = 0;
        nextLevelExp = 2;
        coin = 500;
        currentWeapon = new OBJ_Sword_Normal(gamePanel);
        currentShield = new OBJ_Shield_Wood(gamePanel);
        projectile = new OBJ_Fireball(gamePanel);
        // Apply class-specific modifiers set at the title screen
        if (gamePanel.selectedClass != null) {
            switch (gamePanel.selectedClass) {
                case "Warden":
                    maxHealth += 4; // more health
                    strength += 2; // stronger
                    dexterity += 0;
                    break;
                case "Invoker":
                    maxMana += 6; // more mana
                    strength -= 0; // base weapon remains
                    // give the mage a magic projectile by default
                    projectile = new OBJ_Fireball(gamePanel);
                    break;
                case "Stalker":
                    speed += 1; // faster movement
                    dexterity += 1; // slightly better defense
                    break;
                case "Ravager":
                    maxHealth += 2;
                    dexterity += 2; // better defense scaling
                    break;
                default:
                    break;
            }
        }

        // Ensure starting current values match the (possibly modified) maximums
        health = maxHealth;
        mana = maxMana;

        attack = getAttack();
        defense = getDefense();
    }

    public void setDefaultPositions() {
        worldX = gamePanel.tileSize * 657;
        worldY = gamePanel.tileSize * 526;
        direction = "down";
    }

    public void restoreLifeAndMana() {
        health = maxHealth;
        mana = maxMana;
        immunity = false;
    }

    public int getAttack() {
        attackArea = currentWeapon.attackArea;
        return attack = strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return defense = dexterity * currentShield.defenseValue;
    }

    public void setItems() {
        inventory.clear();
        inventory.add(currentWeapon);
        inventory.add(currentShield);
        inventory.add(new OBJ_Key(gamePanel));
    }

    public void update() { // Update the player's position
        // Reset moving each frame; we'll set it true if movement keys are pressed
        moving = false;

        if (attacking == true) {
            // while attacking, keep attack animation
            attacking();
            moving = false;
        } else {
            boolean anyMoveKey = keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed
                    || keyHandler.rightPressed;
            moving = anyMoveKey;

            if (anyMoveKey || keyHandler.actionPressed == true) {
                if (keyHandler.upPressed == true) {
                    direction = "up";
                } else if (keyHandler.downPressed == true) {
                    direction = "down";
                } else if (keyHandler.leftPressed == true) {
                    direction = "left";
                } else if (keyHandler.rightPressed == true) {
                    direction = "right";
                }

                // Check for collision
                collisionOn = false;
                gamePanel.collisionChecker.checkTile(this);

                int objectIndex = gamePanel.collisionChecker.checkObject(this, true);
                pickupObject(objectIndex);

                int npcIndex = gamePanel.collisionChecker.checkEntity(this, gamePanel.npc);
                interactNPC(npcIndex);

                int monsterIndex = gamePanel.collisionChecker.checkEntity(this, gamePanel.monster);
                interactMonster(monsterIndex);

                gamePanel.eventHandler.checkEvent();

                // If there is no collision and not using action to interact, move the player
                if (collisionOn == false && keyHandler.actionPressed == false) {
                    switch (direction) {
                        case "up":
                            worldY -= speed;
                            break;
                        case "down":
                            worldY += speed;
                            break;
                        case "left":
                            worldX -= speed;
                            break;
                        case "right":
                            worldX += speed;
                            break;
                    }
                }

                if (keyHandler.actionPressed == true && attackCanceled == false) {
                    gamePanel.play(7);
                    attacking = true;
                    spriteCounter = 0;
                }

                attackCanceled = false;

                gamePanel.keyHandler.actionPressed = false;

                // Update walking animation only when moving
                if (moving) {
                    spriteCounter++;
                    // Change the sprite every 15 frames
                    if (spriteCounter > 15) {
                        if (spriteNum == 1) {
                            spriteNum = 2;
                        } else {
                            spriteNum = 1;
                        }
                        spriteCounter = 0;
                    }
                } else {
                    // not moving: reset to idle frame
                    spriteNum = 1;
                    spriteCounter = 0;
                }
            } else {
                // no input: ensure idle frame
                spriteNum = 1;
                spriteCounter = 0;
            }
        }

        if (gamePanel.keyHandler.shotPressed == true && projectile.alive == false && shotAvailableCounter == 30
                && projectile.haveResource(this) == true) {

            // Set the projectile's position
            projectile.set(worldX - 25, worldY - 25, direction, true, this);

            // Subtract the projectile's cost from the player's mana
            projectile.subtractResource(this);

            // Add the projectile to the projectile list
            gamePanel.projectileList.add(projectile);

            shotAvailableCounter = 0;

            gamePanel.play(9);
        }

        if (immunity == true) {
            immunityFrames++;
            if (immunityFrames > 60) {
                immunity = false;
                immunityFrames = 0;
            }
        }
        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }

        if (health > maxHealth) {
            health = maxHealth;
        }
        if (mana > maxMana) {
            mana = maxMana;
        }
        if (health <= 0) {
            gamePanel.gameState = gamePanel.gameOverState;
            gamePanel.stopMusic();
            gamePanel.play(10);
        }
    }

    public void attacking() {
        spriteCounter++;

        if (spriteCounter <= 5) {
            spriteNum = 1;
        }
        if (spriteCounter > 5 && spriteCounter <= 25) {
            spriteNum = 2;

            // Save Current Position
            int currentWorldX = worldX;
            int currentWorldY = worldY;
            int solidAreaWidth = solidArea.width;
            int solidAreaHeight = solidArea.height;

            // Adjust Player's Position
            switch (direction) {
                case "up":
                    worldY -= attackArea.height;
                    break;
                case "down":
                    worldY += attackArea.height;
                    break;
                case "left":
                    worldX -= attackArea.width;
                    break;
                case "right":
                    worldX += attackArea.width;
                    break;
            }
            // Attack Area to Solid Area
            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;

            // Check Monster Collision with new position
            int monsterIndex = gamePanel.collisionChecker.checkEntity(this, gamePanel.monster);
            damageMonster(monsterIndex, attack);

            worldX = currentWorldX;
            worldY = currentWorldY;
            solidArea.width = solidAreaWidth;
            solidArea.height = solidAreaHeight;
        }
        if (spriteCounter > 25) {
            spriteNum = 1;
            spriteCounter = 0;
            attacking = false;
        }
    }

    public void pickupObject(int index) {
        if (index != 999) {

            if (gamePanel.obj[gamePanel.currentMap][index].type == type_pickupOnly) {
                gamePanel.obj[gamePanel.currentMap][index].use(this);
                gamePanel.obj[gamePanel.currentMap][index] = null;
            } else {
                String text;

                if (inventory.size() != inventorySize) {
                    inventory.add(gamePanel.obj[gamePanel.currentMap][index]);
                    gamePanel.play(1);
                    text = "Picked up " + gamePanel.obj[gamePanel.currentMap][index].name;
                } else {
                    text = "Inventory Full!";
                }
                gamePanel.ui.addMessage(text);
                gamePanel.obj[gamePanel.currentMap][index] = null;
            }
        }
    }

    public void interactNPC(int index) {
        if (gamePanel.keyHandler.actionPressed == true) {
            if (index != 999) {
                attackCanceled = true;
                gamePanel.gameState = gamePanel.dialogueState;
                gamePanel.npc[gamePanel.currentMap][index].speak();
            }
        }
    }

    public void interactMonster(int index) {
        if (index != 999) {
            if (immunity == false && gamePanel.monster[gamePanel.currentMap][index].dead == false) {
                gamePanel.play(6);

                int damage = gamePanel.monster[gamePanel.currentMap][index].attack - defense;
                if (damage < 0) {
                    damage = 0;
                }

                health -= damage;
                immunity = true;
            }
        }
    }

    public void damageMonster(int index, int attack) {
        if (index != 999) {
            if (gamePanel.monster[gamePanel.currentMap][index].immunity == false) {
                gamePanel.play(5);

                int damage = attack - gamePanel.monster[gamePanel.currentMap][index].defense;
                if (damage < 0) {
                    damage = 0;
                }

                gamePanel.monster[gamePanel.currentMap][index].health -= damage;
                gamePanel.ui.addMessage(String.valueOf(" "));
                gamePanel.monster[gamePanel.currentMap][index].immunity = true;
                gamePanel.monster[gamePanel.currentMap][index].damageReaction();

                if (gamePanel.monster[gamePanel.currentMap][index].health <= 0) {
                    gamePanel.monster[gamePanel.currentMap][index].dead = true;
                    gamePanel.ui.addMessage(
                            "Monster Defeated!" + " +" + gamePanel.monster[gamePanel.currentMap][index].exp + " EXP");
                    exp += gamePanel.monster[gamePanel.currentMap][index].exp;
                    checkLevelUp();
                }
            }
        }
    }

    public void checkLevelUp() {
        if (exp >= nextLevelExp) {
            gamePanel.play(4);
            level++;
            exp = 0;
            nextLevelExp = nextLevelExp * 2;
            strength++;
            dexterity++;
            maxHealth += 2;
            health = maxHealth;
            attack = getAttack();
            defense = getDefense();

            gamePanel.gameState = gamePanel.dialogueState;
            gamePanel.ui.currentDialogue = "You Leveled Up to " + level + "!";
        }
    }

    public void selectItem() {
        int itemIndex = gamePanel.ui.getItemSlotIndex(gamePanel.ui.playerSlotCol, gamePanel.ui.playerSlotRow);

        if (itemIndex < inventory.size()) {
            Entity selectedItem = inventory.get(itemIndex);

            if (selectedItem.type == type_sword || selectedItem.type == type_axe) {
                currentWeapon = selectedItem;
                attack = getAttack();
                getPlayerAttackImage();
            }
            if (selectedItem.type == type_shield) {
                currentShield = selectedItem;
                defense = getDefense();
            }
            if (selectedItem.type == type_consumable) {
                selectedItem.use(this);
                inventory.remove(itemIndex);
            }
        }
    }

    public void draw(Graphics2D g2) {

        BufferedImage image = null;
        int tempScreenX = screenX;
        int tempScreenY = screenY;

        switch (direction) {
            case "up":
                if (attacking == false) {
                    if (!moving) {
                        image = up0; // idle up
                    } else {
                        if (spriteNum == 1) {
                            image = up1;
                        }
                        if (spriteNum == 2) {
                            image = up2;
                        }
                    }
                }
                if (attacking == true) {
                    tempScreenY = screenY - gamePanel.tileSize;
                    if (spriteNum == 1) {
                        image = attackUp1;
                    }
                    if (spriteNum == 2) {
                        image = attackUp2;
                    }
                }
                break;
            case "down":
                if (attacking == false) {
                    if (!moving) {
                        image = down0; // idle down
                    } else {
                        if (spriteNum == 1) {
                            image = down1;
                        }
                        if (spriteNum == 2) {
                            image = down2;
                        }
                    }
                }
                if (attacking == true) {
                    if (spriteNum == 1) {
                        image = attackDown1;
                    }
                    if (spriteNum == 2) {
                        image = attackDown2;
                    }
                }
                break;
            case "left":
                if (attacking == false) {
                    if (!moving) {
                        image = left0; // idle left
                    } else {
                        if (spriteNum == 1) {
                            image = left1;
                        }
                        if (spriteNum == 2) {
                            image = left2;
                        }
                    }
                }
                if (attacking == true) {
                    tempScreenX = tempScreenX - gamePanel.tileSize;
                    if (spriteNum == 1) {
                        image = attackLeft1;
                    }
                    if (spriteNum == 2) {
                        image = attackLeft2;
                    }
                }
                break;
            case "right":
                if (attacking == false) {
                    if (!moving) {
                        image = right0; // idle right
                    } else {
                        if (spriteNum == 1) {
                            image = right1;
                        }
                        if (spriteNum == 2) {
                            image = right2;
                        }
                    }
                }
                if (attacking == true) {
                    if (spriteNum == 1) {
                        image = attackRight1;
                    }
                    if (spriteNum == 2) {
                        image = attackRight2;
                    }
                }
                break;
        }
        if (immunity == true) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }
        g2.drawImage(image, tempScreenX, tempScreenY, null);

        // Reset Alpha
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
