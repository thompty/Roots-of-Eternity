package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Sword_Normal extends Entity {

    public OBJ_Sword_Normal(GamePanel gamePanel) {
        super(gamePanel);

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        type = type_sword;
        name = "Normal Sword";
        down1 = setup("res/objects/sword_normal.png", width, height);
        attackValue = 1;
        attackArea.width = 36;
        attackArea.height = 36;
        description = "[" + name + "] \n A normal sword.";
        price = 20;
    }

}
