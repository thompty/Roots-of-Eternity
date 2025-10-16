package src.entity;

import java.util.ArrayList;
import java.util.Random;

import src.main.GamePanel;

public class NPC_OldMan extends Entity {

    public NPC_OldMan(GamePanel gamePanel) {
        super(gamePanel);
        name = "OldMan";
        direction = "down";
        speed = 1;

        // NPC hitbox: centered but slightly lower so interaction checks align with
        // sprite face/torso
        int hitW = 30;
        int hitH = 36;
        solidArea = new java.awt.Rectangle((gamePanel.tileSize - hitW) / 2, gamePanel.tileSize - hitH - 6, hitW, hitH);

        getImage();
        setDialogue();
    }

    public void getImage() {
        try {
            int width = gamePanel.tileSize;
            int height = gamePanel.tileSize;
            // Scale the player's image
            up1 = setup("res/npc/oldman_up_1.png", width, height);
            up2 = setup("res/npc/oldman_up_2.png", width, height);
            down1 = setup("res/npc/oldman_down_1.png", width, height);
            down2 = setup("res/npc/oldman_down_2.png", width, height);
            left1 = setup("res/npc/oldman_left_1.png", width, height);
            left2 = setup("res/npc/oldman_left_2.png", width, height);
            right1 = setup("res/npc/oldman_right_1.png", width, height);
            right2 = setup("res/npc/oldman_right_2.png", width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDialogue() {
        dialogue[0] = "Hello Bones!";
        dialogue[1] = "Welcome to Elkia!";
        dialogue[2] = "I am Ghundyr son of Phylinn \nand Murdoka. I am the";
        dialogue[3] = "Keeper of . . .";
        dialogue[4] = "well. . . you'll soon see.";
    }

    @Override
    public void setAction() {
        if (onPath && pathList != null && !pathList.isEmpty()) {
            return; // Let followPath() handle everything
        }

        // Idle/wander logic
        actionLockCounter++;
        if (actionLockCounter >= 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;

            if (i <= 25)
                direction = "up";
            else if (i <= 50)
                direction = "down";
            else if (i <= 75)
                direction = "left";
            else
                direction = "right";

            actionLockCounter = 0;
        }
    }

    @Override
    public void speak() {
        super.speak();

        int startCol = (worldX + solidArea.x) / gamePanel.tileSize;
        int startRow = (worldY + solidArea.y) / gamePanel.tileSize;

        // Set a specific location for pathfinding for oldMan
        int goalCol = 11;
        int goalRow = 11;

        // Set the oldMan to pathfind to player location
        // int goalCol = (gamePanel.player.worldX + gamePanel.player.solidArea.x) /
        // gamePanel.tileSize;
        // int goalRow = (gamePanel.player.worldY + gamePanel.player.solidArea.y) /
        // gamePanel.tileSize;

        System.out.printf(
                "DEBUG: OldMan start=(%d,%d) walkable? %b   goal=(%d,%d) walkable? %b%n",
                startCol, startRow,
                !gamePanel.pathfinder.node[startCol][startRow].solid,
                goalCol, goalRow,
                !gamePanel.pathfinder.node[goalCol][goalRow].solid);

        gamePanel.pathfinder.setNodes(startCol, startRow, goalCol, goalRow);

        if (gamePanel.pathfinder.search()) {
            pathList = new ArrayList<>(gamePanel.pathfinder.pathList);
            onPath = true;
            System.out.println(name + " found path with " + pathList.size() + " steps.");
        } else {
            onPath = false;
            System.out.println(name + " could not find a path.");
        }
    }
}
