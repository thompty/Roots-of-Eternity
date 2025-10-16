package src.main;

import src.entity.Entity;

public class EventHandler {
    GamePanel gamePanel;
    //3D array to track event areas across the map
    EventRect eventRect[][][];
    //Temp Vars for teleporting
    int tempMap, tempCol, tempRow;

    //Initializes Event Handler and Event Rectangles
    public EventHandler(GamePanel gamePanel){
        this.gamePanel = gamePanel;
        eventRect = new EventRect[gamePanel.maxMap][gamePanel.maxWorldCols][gamePanel.maxWorldRows];

        int map = 0;
        int col = 0;
        int row = 0;
        //Initialize event rectangles for maps, cols, and rows
        while(map < gamePanel.maxMap && col < gamePanel.maxWorldCols && row < gamePanel.maxWorldRows){
            eventRect[map][col][row] = new EventRect();
            eventRect[map][col][row].x = 23;
            eventRect[map][col][row].y = 23;
            eventRect[map][col][row].width = 2;
            eventRect[map][col][row].height = 2;
            eventRect[map][col][row].eventRectDefaultX = eventRect[map][col][row].x;
            eventRect[map][col][row].eventRectDefaultY = eventRect[map][col][row].y;

            col++;
            if(col == gamePanel.maxWorldCols){
                col = 0;
                row++;
                if(row == gamePanel.maxWorldRows){
                    row = 0;
                    map++;
                }
            }
        }
    }

    //Check for specific event based on player pos
    public void checkEvent(){
        if(eventHit(0,27,16,"right") == true){
            damagePit(gamePanel.dialogueState);
        }
        else if(eventHit(0,23,12,"up") == true){
            healingPool(gamePanel.dialogueState);
        }
        else if(eventHit(0, 22, 38, "any") == true){
            teleport(1, 13, 12);
        }
        else if(eventHit(1, 12, 13, "any") == true){
            teleport(0, 23, 39);
        }
        else if(eventHit(1,12,9,"up") == true){
            speak(gamePanel.npc[1][0]);
        }
    }

    //Determines if player hits an event area
    public boolean eventHit(int map, int col, int row, String reqDirection){
        boolean hit = false;

        //Adjust play pos and event rect for collision check
        if(map == gamePanel.currentMap){
        gamePanel.player.solidArea.x = gamePanel.player.worldX + gamePanel.player.solidArea.x;
        gamePanel.player.solidArea.y = gamePanel.player.worldY + gamePanel.player.solidArea.y;

        eventRect[map][col][row].x = col * gamePanel.tileSize + eventRect[map][col][row].x;
        eventRect[map][col][row].y = row * gamePanel.tileSize + eventRect[map][col][row].y;
        
        //Check collision and dir requirement
        if(gamePanel.player.solidArea.intersects(eventRect[map][col][row])){
            if(gamePanel.player.direction.equals(reqDirection) || reqDirection.equals("any")){
                hit = true;
            }
        }

        //Reset positions
        gamePanel.player.solidArea.x = gamePanel.player.solidAreaDefaultX;
        gamePanel.player.solidArea.y = gamePanel.player.solidAreaDefaultY;
        eventRect[map][col][row].x = eventRect[map][col][row].eventRectDefaultX;
        eventRect[map][col][row].y = eventRect[map][col][row].eventRectDefaultY;
        }        
        return hit;
    }

    //Pit Damage and Announce
    public void damagePit(int gameState){
        gamePanel.gameState = gameState;
        gamePanel.ui.currentDialogue = "You fell into the pit!";
        gamePanel.player.health -= 1;
    }

    //Healing Pool and Announce
    public void healingPool(int gameState){
        if(gamePanel.keyHandler.actionPressed == true){
            gamePanel.gameState = gameState;
            gamePanel.player.attackCanceled = true;
            gamePanel.ui.currentDialogue = "You found a healing pool!";
            gamePanel.player.health = gamePanel.player.maxHealth;
            gamePanel.player.mana = gamePanel.player.maxMana;
            gamePanel.assetSetter.setMonster();
        }
        
        gamePanel.keyHandler.actionPressed = false;
    }

    //Teleport Init to new map and pos
    public void teleport(int map, int col, int row){
        gamePanel.gameState = gamePanel.transitionState;
        tempMap = map;
        tempCol = col;
        tempRow = row;
    }

    //Dialogue Init 
    public void speak(Entity entity){
        if(gamePanel.keyHandler.actionPressed == true){
            gamePanel.gameState = gamePanel.dialogueState;
            gamePanel.player.attackCanceled = true;
            entity.speak();
        }
    }
}
