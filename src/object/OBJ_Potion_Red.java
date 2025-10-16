package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Potion_Red extends Entity {

    private GamePanel gamePanel;

    public OBJ_Potion_Red(GamePanel gamePanel) {
        super(gamePanel);
        this.gamePanel = gamePanel;

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        type = type_consumable;
        name = "Red Potion";
        value = 5;
        down1 = setup("res/objects/potion_red.png", width, height);
        description = "[" + name + "] \n A red potion. That Heals" + value + "HP.";
        price = 25;
    }

    public void use(Entity entity) {
        gamePanel.gameState = gamePanel.dialogueState;
        gamePanel.ui.currentDialogue = "You drink the " + name + " and heal " + value + "HP.";
        entity.health += value;
        gamePanel.play(2);
    }
}
