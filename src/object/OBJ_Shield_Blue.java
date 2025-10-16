package src.object;

import src.main.GamePanel;
import src.entity.Entity;

public class OBJ_Shield_Blue extends Entity {
    public OBJ_Shield_Blue(GamePanel gamePanel) {

        super(gamePanel);

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        type = type_shield;
        name = "Blue Shield";
        down1 = setup("res/objects/shield_blue.png", width, height);
        defenseValue = 2;
        description = "[" + name + "]\n A shiny blue shield.";
        price = 250;
    }
}
