package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Mana_Crystal extends Entity {

    GamePanel gamePanel;

    public OBJ_Mana_Crystal(GamePanel gamePanel) {
        super(gamePanel);
        this.gamePanel = gamePanel;

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        name = "Mana Crystal";
        type = type_pickupOnly;
        value = 1;

        down1 = setup("res/objects/mana_crystal_full.png", width, height);
        image1 = setup("res/objects/mana_crystal_full.png", width, height);
        image2 = setup("res/objects/mana_crystal_empty.png", width, height);
    }

    public void use(Entity entity) {
        gamePanel.play(2);
        gamePanel.ui.addMessage("You found a Heart");
        entity.mana += value;
    }

}
