package src.entity;

import java.awt.image.BufferedImage;

import src.main.GamePanel;
import src.main.UtilityTool;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.Rectangle; // Add this import statement
import src.ai.Node;

public class Entity {
    GamePanel gamePanel;
    public int worldX, worldY;
    public int speed;

    // Direction and sprite variables
    public String direction = "down";
    public int spriteCounter = 0;
    public int spriteNum = 1;
    public boolean moving = false; // true when entity position changes this frame

    // Collision and action variables
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
    public Rectangle attackArea = new Rectangle(0, 0, 0, 0);
    public int solidAreaDefaultX, solidAreaDefaultY;
    public boolean collisionOn = false;
    public int actionLockCounter = 0;
    public int shotAvailableCounter = 0;

    // Immunity and dialogue variables
    public boolean immunity = false;
    public int immunityFrames = 0;
    public String dialogue[] = new String[20];
    public int dialogueIndex = 0;
    boolean attacking = false;
    int dyingCounter = 0;
    boolean hpBarOn = false;
    int hpBarCounter = 0;

    // Name and collision variables
    public String name;
    public boolean collision = false;
    public boolean onPath = false;
    public ArrayList<Node> pathList = new ArrayList<>();

    // Type and health variables
    public int maxHealth;
    public int health;
    public int maxMana;
    public int mana;
    public int maxAmmo;
    public int ammo;
    public boolean alive = true;
    public boolean dead = false;

    // Player Status Variables
    public int level;
    public int attack;
    public int strength;
    public int defense;
    public int dexterity;
    public int exp;
    public int nextLevelExp;
    public int coin;
    public Entity currentWeapon;
    public Entity currentShield;
    public Projectile projectile;

    // Item Attributes
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int inventorySize = 20;
    public int value = 0;
    public int attackValue = 0;
    public int defenseValue = 0;
    public String description = "";
    public int manaCost = 0;
    public int ammoCost = 0;
    public int price;

    // Type
    public int type; // 0 = player, 1 = npc, 2 = monster
    public final int type_player = 0;
    public final int type_npc = 1;
    public final int type_monster = 2;
    public final int type_sword = 3;
    public final int type_axe = 4;
    public final int type_shield = 5;
    public final int type_consumable = 6;
    public final int type_pickupOnly = 7;

    // Image variables
    public BufferedImage up0, up1, up2, up3, down0, down1, down2, down3, left0, left1, left2, left3, right0, right1,
            right2, right3, image1, image2, image3;
    public BufferedImage attackUp1, attackUp2, attackDown1, attackDown2, attackLeft1, attackLeft2, attackRight1,
            attackRight2;

    public Entity(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void setAction() {
    }

    public void damageReaction() {
    };

    public void speak() {
        if (dialogue[dialogueIndex] == null) {
            dialogueIndex = 0;
        }
        gamePanel.ui.currentDialogue = dialogue[dialogueIndex];
        dialogueIndex++;

        switch (gamePanel.player.direction) {
            case "up":
                direction = "down";
                break;
            case "down":
                direction = "up";
                break;
            case "left":
                direction = "right";
                break;
            case "right":
                direction = "left";
                break;
            default:
                break;
        }
    }

    public void use(Entity entity) {
    }

    public void checkDrop() {

    }

    public void dropItem(Entity itemDropped) {
        for (int i = 0; i < gamePanel.obj[1].length; i++) {
            if (gamePanel.obj[gamePanel.currentMap][i] == null) {
                gamePanel.obj[gamePanel.currentMap][i] = itemDropped;
                // The dead Monster's position
                gamePanel.obj[gamePanel.currentMap][i].worldX = worldX;
                gamePanel.obj[gamePanel.currentMap][i].worldY = worldY;
                break;
            }
        }
    }

    public void checkCollision() {
        collisionOn = false;
        gamePanel.collisionChecker.checkTile(this);
        gamePanel.collisionChecker.checkObject(this, false);
        gamePanel.collisionChecker.checkEntity(this, gamePanel.npc);
        gamePanel.collisionChecker.checkEntity(this, gamePanel.monster);
        boolean contactPlayer = gamePanel.collisionChecker.checkPlayer(this);

        if (this.type == type_monster && contactPlayer == true) {
            damagePlayer(attack);
        }
    }

    public void update() {
        int oldX = worldX;
        int oldY = worldY;

        setAction();
        checkCollision();

        if (onPath && pathList != null && pathList.size() > 0) {
            followPath();
        } else {
            if (!collisionOn) {
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
        }

        // moving = whether position actually changed this tick
        moving = (worldX != oldX) || (worldY != oldY);

        spriteCounter++;
        // Change the sprite every 10 frames
        if (spriteCounter > 15) {
            if (spriteNum == 1) {
                spriteNum = 2;
            } else {
                spriteNum = 1;
            }
            spriteCounter = 0;
        }

        if (immunity == true) {
            immunityFrames++;
            if (immunityFrames > 30) {
                immunity = false;
                immunityFrames = 0;
            }
        }
        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }
    }

    public void damagePlayer(int attack) {
        if (gamePanel.player.immunity == false) {
            gamePanel.play(6);

            int damage = attack - gamePanel.player.defense;
            if (damage < 0) {
                damage = 0;
            }

            gamePanel.player.health -= damage;
            gamePanel.player.immunity = true;
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        // Use the player's screen offsets so entity rendering and debug hitbox
        // coordinates are computed the same way.
        int screenX = worldX - gamePanel.player.worldX + gamePanel.player.screenX;
        int screenY = worldY - gamePanel.player.worldY + gamePanel.player.screenY;

        // Special-case animation rules: BoneMender has distinct idle (0/1) and walk
        // (2/3)
        // frames; most other NPCs/monsters only provide frames 1/2 per direction.
        final boolean isBoneMender = "BoneMender".equals(this.name);

        if (worldX + gamePanel.tileSize > gamePanel.player.worldX - gamePanel.player.screenX &&
                worldX - gamePanel.tileSize < gamePanel.player.worldX + gamePanel.player.screenX &&
                worldY + gamePanel.tileSize > gamePanel.player.worldY - gamePanel.player.screenY &&
                worldY - gamePanel.tileSize < gamePanel.player.worldY + gamePanel.player.screenY) {

            switch (direction) {
                case "up":
                    if (isBoneMender) {
                        if (!moving) {
                            image = (spriteNum == 1) ? up0 : up1; // idle 0/1
                        } else {
                            image = (spriteNum == 1) ? up2 : up3; // walk 2/3
                        }
                    } else {
                        if (!moving) {
                            image = up1; // idle holds frame 1 for non-BoneMender
                        } else {
                            image = (spriteNum == 1) ? up1 : up2; // walk 1/2
                        }
                    }
                    break;
                case "down":
                    if (isBoneMender) {
                        if (!moving) {
                            image = (spriteNum == 1) ? down0 : down1;
                        } else {
                            image = (spriteNum == 1) ? down2 : down3;
                        }
                    } else {
                        if (!moving) {
                            image = down1;
                        } else {
                            image = (spriteNum == 1) ? down1 : down2;
                        }
                    }
                    break;
                case "left":
                    if (isBoneMender) {
                        if (!moving) {
                            image = (spriteNum == 1) ? left0 : left1;
                        } else {
                            image = (spriteNum == 1) ? left2 : left3;
                        }
                    } else {
                        if (!moving) {
                            image = left1;
                        } else {
                            image = (spriteNum == 1) ? left1 : left2;
                        }
                    }
                    break;
                case "right":
                    if (isBoneMender) {
                        if (!moving) {
                            image = (spriteNum == 1) ? right0 : right1;
                        } else {
                            image = (spriteNum == 1) ? right2 : right3;
                        }
                    } else {
                        if (!moving) {
                            image = right1;
                        } else {
                            image = (spriteNum == 1) ? right1 : right2;
                        }
                    }
                    break;
            }

            // Fallback safety: if selected frame is missing for a given entity, try a
            // reasonable alternative to avoid drawing null.
            if (image == null) {
                switch (direction) {
                    case "up":
                        image = (up1 != null) ? up1 : (up2 != null ? up2 : (up0 != null ? up0 : up3));
                        break;
                    case "down":
                        image = (down1 != null) ? down1 : (down2 != null ? down2 : (down0 != null ? down0 : down3));
                        break;
                    case "left":
                        image = (left1 != null) ? left1 : (left2 != null ? left2 : (left0 != null ? left0 : left3));
                        break;
                    case "right":
                        image = (right1 != null) ? right1
                                : (right2 != null ? right2 : (right0 != null ? right0 : right3));
                        break;
                }
            }
            if (type == 2 && hpBarOn == true) {
                double oneScale = (double) gamePanel.tileSize / maxHealth;
                double hpBarValue = health * oneScale;

                g2.setColor(new Color(35, 35, 35));
                g2.fillRect(screenX - 1, screenY - 16, gamePanel.tileSize + 2, 12);

                g2.setColor(new Color(255, 0, 30));
                g2.fillRect(screenX, screenY - 15, (int) hpBarValue, 10);

                hpBarCounter++;

                if (hpBarCounter > 600) {
                    hpBarOn = false;
                    hpBarCounter = 0;
                }
            }

            if (immunity == true) {
                hpBarOn = true;
                hpBarCounter = 0;
                changeAlpha(g2, 0.5f);
            }
            if (dead == true) {
                dyingAnimation(g2);
            }

            g2.drawImage(image, screenX, screenY, null);

            changeAlpha(g2, 1f);
        }
    }

    public void dyingAnimation(Graphics2D g2) {
        dyingCounter++;

        int i = 5;

        if (dyingCounter <= i) {
            changeAlpha(g2, 0.5f);
        }
        if (dyingCounter > i && dyingCounter <= i * 2) {
            changeAlpha(g2, 1f);
        }
        if (dyingCounter > i * 2 && dyingCounter <= i * 3) {
            changeAlpha(g2, 0.5f);
        }
        if (dyingCounter > i * 3 && dyingCounter <= i * 4) {
            changeAlpha(g2, 1f);
        }
        if (dyingCounter > i * 5 && dyingCounter <= i * 6) {
            changeAlpha(g2, 0.5f);
        }
        if (dyingCounter > i * 6 && dyingCounter <= i * 7) {
            changeAlpha(g2, 1f);
        }
        if (dyingCounter > i * 7) {
            alive = false;
        }
    }

    public void changeAlpha(Graphics2D g2, float alpha) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    public BufferedImage setup(String imagePath, int width, int height) {
        {
            UtilityTool uTool = new UtilityTool();
            BufferedImage image = null;

            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream(imagePath);
                if (is == null) {
                    System.out.println("Image not found: " + imagePath);
                } else {
                    image = ImageIO.read(is);
                    if (image == null) {
                        throw new IOException("Failed to read image: " + imagePath);
                    }
                    image = uTool.scaleImage(image, width, height);
                }
            } catch (IOException e) {
                System.out.println("Error loading image: " + imagePath);
                e.printStackTrace();
            }

            return image;
        }
    }

    public void searchPath(int goalCol, int goalRow) {
        int startCol = (worldX + solidArea.x) / gamePanel.tileSize;
        int startRow = (worldY + solidArea.y) / gamePanel.tileSize;

        gamePanel.pathfinder.setNodes(startCol, startRow, goalCol, goalRow);

        if (gamePanel.pathfinder.search() == true) {
            // Next World X and World Y
            int nextX = gamePanel.pathfinder.pathList.get(0).col * gamePanel.tileSize;
            int nextY = gamePanel.pathfinder.pathList.get(0).row * gamePanel.tileSize;

            int enLeftX = worldX + solidArea.x;
            int enRightX = worldX + solidArea.x + solidArea.width;
            int enTopY = worldY + solidArea.y;
            int enBottomY = worldY + solidArea.y + solidArea.height;

            if (enTopY > nextY && enLeftX >= nextX && enRightX < nextX + gamePanel.tileSize) {
                // Up
                direction = "up";
            } else if (enTopY < nextY && enLeftX >= nextX && enRightX < nextX + gamePanel.tileSize) {
                // Down
                direction = "down";
            } else if (enTopY >= nextY && enBottomY < nextY + gamePanel.tileSize) {
                // Left or Right
                if (enLeftX > nextX) {
                    direction = "left";
                }
                if (enLeftX < nextX) {
                    direction = "right";
                }
            }
            // Up or Left
            else if (enTopY > nextY && enLeftX > nextX) {
                direction = "up";
                checkCollision();
                if (collisionOn == true) {
                    direction = "left";
                }
            }
            // Up or Right
            else if (enTopY > nextY && enLeftX < nextX) {
                direction = "up";
                checkCollision();
                if (collisionOn == true) {
                    direction = "right";
                }
            }
            // Down or Left
            else if (enTopY < nextY && enLeftX > nextX) {
                direction = "down";
                checkCollision();
                if (collisionOn == true) {
                    direction = "left";
                }
            }
            // Down or Right
            else if (enTopY < nextY && enLeftX < nextX) {
                direction = "down";
                checkCollision();
                if (collisionOn == true) {
                    direction = "right";
                }
            }

            // If reaches goal stop search
            int nextCol = gamePanel.pathfinder.pathList.get(0).col;
            int nextRow = gamePanel.pathfinder.pathList.get(0).row;
            if (nextCol == goalCol && nextRow == goalRow) {
                onPath = false;
            }
        }
    }

    public void followPath() {
        if (!onPath || pathList == null || pathList.isEmpty())
            return;

        Node nextNode = pathList.get(0);
        // Compute the **destination** using your solidArea offset:
        int targetX = nextNode.col * gamePanel.tileSize - solidArea.x;
        int targetY = nextNode.row * gamePanel.tileSize - solidArea.y;

        // 1) If you’ve _arrived_, pop this node
        if (worldX == targetX && worldY == targetY) {
            pathList.remove(0);
            if (pathList.isEmpty()) {
                onPath = false;
                gamePanel.pathfinder.pathList.clear();
            }
            return;
        }

        // 2) Otherwise decide which way to step:
        if (worldX < targetX) {
            direction = "right";
        } else if (worldX > targetX) {
            direction = "left";
        } else if (worldY < targetY) {
            direction = "down";
        } else if (worldY > targetY) {
            direction = "up";
        }

        // 3) Move if no collision
        checkCollision();
        if (!collisionOn) {
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
    }
}