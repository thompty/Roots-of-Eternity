package src.object;

import src.entity.Projectile;
import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Fireball extends Projectile {

    GamePanel gamePanel;

    public OBJ_Fireball(GamePanel gamePanel) {
        super(gamePanel);
        this.gamePanel = gamePanel;

        name = "Fireball";
        speed = 5;
        maxHealth = 80;
        attack = 2;
        manaCost = 1;
        alive = false;
        getImage();
    }

    public void getImage() {

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        up1 = setup("res/projectiles/fireball_up_1.png", width, height);
        up2 = setup("res/projectiles/fireball_up_2.png", width, height);
        down1 = setup("res/projectiles/fireball_down_1.png", width, height);
        down2 = setup("res/projectiles/fireball_down_2.png", width, height);
        left1 = setup("res/projectiles/fireball_left_1.png", width, height);
        left2 = setup("res/projectiles/fireball_left_2.png", width, height);
        right1 = setup("res/projectiles/fireball_right_1.png", width, height);
        right2 = setup("res/projectiles/fireball_right_2.png", width, height);
    }

    public boolean haveResource(Entity user) {
        boolean haveResource;
        if (user.mana >= manaCost) {
            haveResource = true;
        } else {
            haveResource = false;
        }
        return haveResource;
    }

    public void subtractResource(Entity user) {
        user.mana -= manaCost;
    }
}
