package src.object;

import src.entity.Entity;
import src.entity.Projectile;
import src.main.GamePanel;

public class OBJ_Rock extends Projectile {

    GamePanel gamePanel;

    public OBJ_Rock(GamePanel gamePanel) {
        super(gamePanel);
        this.gamePanel = gamePanel;

        name = "Rock";
        speed = 8;
        maxHealth = 80;
        attack = 4;
        manaCost = 1;
        ammoCost = 1;
        alive = false;
        getImage();
    }

    public void getImage() {

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        up1 = setup("res/projectiles/rock_down_1.png", width, height);
        up2 = setup("res/projectiles/rock_down_1.png", width, height);
        down1 = setup("res/projectiles/rock_down_1.png", width, height);
        down2 = setup("res/projectiles/rock_down_1.png", width, height);
        left1 = setup("res/projectiles/rock_down_1.png", width, height);
        left2 = setup("res/projectiles/rock_down_1.png", width, height);
        right1 = setup("res/projectiles/rock_down_1.png", width, height);
        right2 = setup("res/projectiles/rock_down_1.png", width, height);
    }

    public boolean haveResource(Entity user) {
        boolean haveResource;
        if (user.ammo >= ammoCost) {
            haveResource = true;
        } else {
            haveResource = false;
        }
        return haveResource;
    }

    public void subtractResource(Entity user) {
        user.ammo -= ammoCost;
    }
}
