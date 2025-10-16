package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Door extends Entity {

    public OBJ_Door(GamePanel gamePanel) {

        super(gamePanel);

        name = "Door";

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        down1 = setup("res/objects/door.png", width, height);
        collision = true;

        solidArea.x = 0;
        solidArea.y = 16;
        solidArea.width = 48;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

}
