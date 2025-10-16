package src.entity;

import src.main.GamePanel;
import src.object.OBJ_Axe;
import src.object.OBJ_Key;
import src.object.OBJ_Potion_Red;
import src.object.OBJ_Shield_Blue;

public class NPC_Merchant extends Entity {
    public NPC_Merchant(GamePanel gamePanel) {
        super(gamePanel);

        direction = "down";
        speed = 1;

        getImage();
        setDialogue();
        setItems();
    }

    public void getImage() {
        try {
            int width = gamePanel.tileSize;
            int height = gamePanel.tileSize;
            // Scale the player's image
            up1 = setup("res/npc/merchant_down_1.png", width, height);
            up2 = setup("res/npc/merchant_down_2.png", width, height);
            down1 = setup("res/npc/merchant_down_1.png", width, height);
            down2 = setup("res/npc/merchant_down_2.png", width, height);
            left1 = setup("res/npc/merchant_down_1.png", width, height);
            left2 = setup("res/npc/merchant_down_2.png", width, height);
            right1 = setup("res/npc/merchant_down_1.png", width, height);
            right2 = setup("res/npc/merchant_down_2.png", width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDialogue() {
        dialogue[0] = "Hello Bones! Would you like to \ntrade?";
    }

    public void setItems() {
        inventory.add(new OBJ_Potion_Red(gamePanel));
        inventory.add(new OBJ_Axe(gamePanel));
        inventory.add(new OBJ_Shield_Blue(gamePanel));
        inventory.add(new OBJ_Key(gamePanel));
    }

    public void speak() {
        super.speak();
        gamePanel.gameState = gamePanel.tradeState;
        gamePanel.ui.npc = this;
    }
}
