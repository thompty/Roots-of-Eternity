package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Shield_Wood extends Entity {

    public OBJ_Shield_Wood(GamePanel gamePanel) {

        super(gamePanel);

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        type = type_shield;
        name = "Wooden Shield";
        down1 = setup("res/objects/shield_wood.png", width, height);
        defenseValue = 1;
        description = "[" + name + "]\n A wooden shield.";
        price = 25;
    }

}
