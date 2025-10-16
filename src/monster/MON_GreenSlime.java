package src.monster;

import java.util.Random;

import src.entity.Entity;
import src.main.GamePanel;
import src.object.OBJ_Coin_Bronze;
import src.object.OBJ_Heart;
import src.object.OBJ_Mana_Crystal;
import src.object.OBJ_Rock;

public class MON_GreenSlime extends Entity {

    GamePanel gamePanel;

    public MON_GreenSlime(GamePanel gamePanel) {
        super(gamePanel);

        this.gamePanel = gamePanel;

        type = type_monster;
        name = "GreenSlime";
        speed = 1;
        maxHealth = 6;
        health = maxHealth;
        maxAmmo = 10;
        ammo = maxAmmo;
        attack = 5;
        defense = 0;
        exp = 2;
        projectile = new OBJ_Rock(gamePanel);

        // Center a slightly wide but low hitbox for the slime (42x30) aligned to sprite
        // bottom
        int hitW = 40;
        int hitH = 28;
        solidArea.x = (gamePanel.tileSize - hitW) / 2;
        solidArea.y = gamePanel.tileSize - hitH - 4; // slightly smaller offset
        solidArea.width = hitW;
        solidArea.height = hitH;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        up1 = setup("res/monsters/greenslime_down_1.png", width, height);
        up2 = setup("res/monsters/greenslime_down_2.png", width, height);
        down1 = setup("res/monsters/greenslime_down_1.png", width, height);
        down2 = setup("res/monsters/greenslime_down_2.png", width, height);
        left1 = setup("res/monsters/greenslime_down_1.png", width, height);
        left2 = setup("res/monsters/greenslime_down_2.png", width, height);
        right1 = setup("res/monsters/greenslime_down_1.png", width, height);
        right2 = setup("res/monsters/greenslime_down_2.png", width, height);
    }

    public void setAction() {
        actionLockCounter++;

        if (actionLockCounter == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1; // Random number between 1 and 100

            if (i <= 25) {
                direction = "up";
            } else if (i > 25 && i <= 50) {
                direction = "down";
            } else if (i > 50 && i <= 75) {
                direction = "left";
            } else if (i > 75 && i <= 100) {
                direction = "right";
            }
            actionLockCounter = 0;
        }
        int i = new Random().nextInt(100) + 1; // Random number between 1 and 100
        if (i >= 99 && projectile.alive == false && shotAvailableCounter == 30) {
            projectile.set(worldX, worldY, direction, true, this);
            gamePanel.projectileList.add(projectile);
            shotAvailableCounter = 0;
        }
    }

    public void damageReaction() {
        actionLockCounter = 0;
        direction = gamePanel.player.direction;
    }

    public void checkDrop() {
        int i = new Random().nextInt(100) + 1; // Random number between 1 and 100

        if (i < 50) {
            dropItem(new OBJ_Coin_Bronze(gamePanel));
        }
        if (i >= 50 && i < 75) {
            dropItem(new OBJ_Heart(gamePanel));
        }
        if (i >= 75 && i < 100) {
            dropItem(new OBJ_Mana_Crystal(gamePanel));
        }
    }
}