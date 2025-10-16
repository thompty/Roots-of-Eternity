package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Key extends Entity {

    public OBJ_Key(GamePanel gamePanel) {

        super(gamePanel);

        name = "Key";

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        down1 = setup("res/objects/key.png", width, height);
        collision = true;
        description = "[" + name + "] \n A key.";
        price = 100;
    }

}
