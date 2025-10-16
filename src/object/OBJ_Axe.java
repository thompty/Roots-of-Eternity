package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Axe extends Entity {
    public OBJ_Axe(GamePanel gamePanel) {
        super(gamePanel);

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        type = type_axe;
        name = "Woodcutter's Axe";
        down1 = setup("res/objects/axe.png", width, height);
        attackValue = 2;
        attackArea.width = 30;
        attackArea.height = 30;
        description = "[" + name + "] \n An axe to chop wood.";
        price = 75;
    }
}
