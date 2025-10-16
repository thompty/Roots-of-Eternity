package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Heart extends Entity {

    GamePanel gamePanel;

    public OBJ_Heart(GamePanel gamePanel) {

        super(gamePanel);
        this.gamePanel = gamePanel;

        type = type_pickupOnly;
        name = "Heart";
        value = 2;

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        down1 = setup("res/objects/heart_full.png", width, height);
        image1 = setup("res/objects/heart_full.png", width, height);
        image2 = setup("res/objects/heart_half.png", width, height);
        image3 = setup("res/objects/heart_blank.png", width, height);

    }

    public void use(Entity entity) {
        gamePanel.play(2);
        gamePanel.ui.addMessage("You found a Heart");
        entity.health += value;
    }

}
