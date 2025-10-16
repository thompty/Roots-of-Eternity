package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Coin_Bronze extends Entity {

    GamePanel gamePanel;

    public OBJ_Coin_Bronze(GamePanel gamePanel) {
        super(gamePanel);
        this.gamePanel = gamePanel;
        // TODO Auto-generated constructor stub

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        type = type_pickupOnly;
        name = "Bronze Coin";
        value = 1;
        down1 = setup("res/objects/coin_bronze.png", width, height);
    }

    public void use(Entity entity) {
        gamePanel.play(1);
        gamePanel.ui.addMessage("You found a Bronze Coin");
        gamePanel.player.coin += value;
    }

}
