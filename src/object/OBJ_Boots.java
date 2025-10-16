package src.object;

import src.entity.Entity;
import src.main.GamePanel;


public class OBJ_Boots extends Entity{

    public OBJ_Boots(GamePanel gamePanel){

        super(gamePanel);

        name = "Boots";

        int width = gamePanel.tileSize;
        int height = gamePanel.tileSize;

        down1 = setup("/objects/boots.png", width, height); 
        collision = true;
    }

}
