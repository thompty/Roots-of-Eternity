package src.entity;

import java.util.ArrayList;
import java.util.Random;

import src.main.GamePanel;

public class NPC_BoneMender extends Entity {
    // Simple state machine for idle vs short walks
    private enum Behavior {
        IDLE, WALK
    }

    private Behavior behavior = Behavior.IDLE;
    private int behaviorTicks = 0; // counts frames in current behavior
    private int walkDuration = 0; // how many ticks to keep walking

    public NPC_BoneMender(GamePanel gamePanel) {
        super(gamePanel);
        name = "BoneMender";
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
            int height = gamePanel.tileSize * 2;
            // Scale the player's image
            // Flip up/down: 1..4 are facing DOWN; 5..8 are facing UP
            down0 = setup("res/player/BoneMender/Bone_Weaver-1.png", width, height);
            down1 = setup("res/player/BoneMender/Bone_Weaver-2.png", width, height);
            down2 = setup("res/player/BoneMender/Bone_Weaver-3.png", width, height);
            down3 = setup("res/player/BoneMender/Bone_Weaver-4.png", width, height);
            up0 = setup("res/player/BoneMender/Bone_Weaver-5.png", width, height);
            up1 = setup("res/player/BoneMender/Bone_Weaver-6.png", width, height);
            up2 = setup("res/player/BoneMender/Bone_Weaver-7.png", width, height);
            up3 = setup("res/player/BoneMender/Bone_Weaver-8.png", width, height);
            right0 = setup("res/player/BoneMender/Bone_Weaver-9.png", width, height);
            right1 = setup("res/player/BoneMender/Bone_Weaver-10.png", width, height);
            right2 = setup("res/player/BoneMender/Bone_Weaver-11.png", width, height);
            right3 = setup("res/player/BoneMender/Bone_Weaver-12.png", width, height);
            left0 = setup("res/player/BoneMender/Bone_Weaver-13.png", width, height);
            left1 = setup("res/player/BoneMender/Bone_Weaver-14.png", width, height);
            left2 = setup("res/player/BoneMender/Bone_Weaver-15.png", width, height);
            left3 = setup("res/player/BoneMender/Bone_Weaver-16.png", width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDialogue() {
        dialogue[0] = "Hello";
        dialogue[1] = "...";
        dialogue[2] = "...";
        dialogue[3] = "...";
        dialogue[4] = "...";
    }

    @Override
    public void setAction() {
        if (onPath && pathList != null && !pathList.isEmpty()) {
            // When pathing, normal movement happens in followPath(); moving=true will be
            // set
            behavior = Behavior.WALK;
            return;
        }

        behaviorTicks++;

        switch (behavior) {
            case IDLE:
                // Stay idle for 2-4 seconds, play idle animation via Entity.draw using 0/1
                // frames
                if (behaviorTicks > 120 + new Random().nextInt(120)) {
                    // Transition to a short walk in current or random direction
                    Random r = new Random();
                    int pick = r.nextInt(4);
                    switch (pick) {
                        case 0:
                            direction = "up";
                            break;
                        case 1:
                            direction = "down";
                            break;
                        case 2:
                            direction = "left";
                            break;
                        case 3:
                            direction = "right";
                            break;
                    }
                    walkDuration = 45 + r.nextInt(60); // walk ~0.75s-1.5s at 60fps
                    behavior = Behavior.WALK;
                    behaviorTicks = 0;
                }
                break;
            case WALK:
                // While walking, Entity.update will move us and mark moving=true, which
                // drives 2/3 frames. Keep walking until duration elapses or we hit collision.
                if (behaviorTicks >= walkDuration || collisionOn) {
                    behavior = Behavior.IDLE;
                    behaviorTicks = 0;
                }
                break;
        }
    }

    @Override
    public void speak() {
        super.speak();

        int startCol = (worldX + solidArea.x) / gamePanel.tileSize;
        int startRow = (worldY + solidArea.y) / gamePanel.tileSize;

        // Set a specific location for pathfinding for oldMan
        int goalCol = 657;
        int goalRow = 525;

        // Set the oldMan to pathfind to player location
        // int goalCol = (gamePanel.player.worldX + gamePanel.player.solidArea.x) /
        // gamePanel.tileSize;
        // int goalRow = (gamePanel.player.worldY + gamePanel.player.solidArea.y) /
        // gamePanel.tileSize;

        // Debug message
        // System.out.printf(
        // "DEBUG: OldMan start=(%d,%d) walkable? %b goal=(%d,%d) walkable? %b%n",
        // startCol, startRow,
        // !gamePanel.pathfinder.node[startCol][startRow].solid,
        // goalCol, goalRow,
        // !gamePanel.pathfinder.node[goalCol][goalRow].solid);

        // Validate target tile to avoid NPEs in Pathfinder when tiles are missing
        if (goalCol < 0 || goalRow < 0 || goalCol >= gamePanel.maxWorldCols || goalRow >= gamePanel.maxWorldRows) {
            System.out.println(name + ": invalid goal tile (out of bounds): [" + goalCol + "," + goalRow + "]");
            onPath = false;
            return;
        }
        int tileNum = gamePanel.tileManager.mapTileNum[gamePanel.currentMap][goalCol][goalRow];
        if (tileNum < 0 || tileNum >= gamePanel.tileManager.tile.length
                || gamePanel.tileManager.tile[tileNum] == null) {
            System.out.println(name + ": invalid goal tileNum=" + tileNum + " at [" + goalCol + "," + goalRow + "]");
            onPath = false;
            return;
        }

        gamePanel.pathfinder.setNodes(startCol, startRow, goalCol, goalRow);

        if (gamePanel.pathfinder.search()) {
            pathList = new ArrayList<>(gamePanel.pathfinder.pathList);
            onPath = !pathList.isEmpty();
            System.out.println(name + " found path with " + pathList.size() + " steps.");
        } else {
            onPath = false;
            System.out.println(name + " could not find a path.");
        }
    }
}
