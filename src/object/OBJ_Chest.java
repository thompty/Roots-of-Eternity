package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Chest extends Entity{
    
    public OBJ_Chest(GamePanel gamePanel){

        super(gamePanel);

        name = "Chest";

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        down1 = setup("/objects/chest.png", width, height); 
        collision = true;
    }

}
