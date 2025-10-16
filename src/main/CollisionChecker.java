package src.main;

import java.awt.Rectangle;

import src.entity.Entity;

public class CollisionChecker {

    GamePanel gamePanel;

    public CollisionChecker(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Check tile collisions using entity's solidArea and tile hitBoxes.
     * This method does not mutate entity.solidArea; it builds local rectangles
     * and checks intersections against tiles' hitBox rectangles (in world coords).
     */
    public void checkTile(Entity entity) {
        // Entity current solid area in world coordinates
        Rectangle entityRect = new Rectangle(
                entity.worldX + entity.solidArea.x,
                entity.worldY + entity.solidArea.y,
                entity.solidArea.width,
                entity.solidArea.height);

        // Debug: show player collision area calculation
        if (entity == gamePanel.player) {
            // Debug output removed
        }

        // Predict next position based on direction
        Rectangle predicted = new Rectangle(entityRect);
        switch (entity.direction) {
            case "up":
                predicted.y -= entity.speed;
                break;
            case "down":
                predicted.y += entity.speed;
                break;
            case "left":
                predicted.x -= entity.speed;
                break;
            case "right":
                predicted.x += entity.speed;
                break;
            default:
                break;
        }

        // Determine tiles covered by the predicted rectangle
        int leftCol = Math.max(0, predicted.x / gamePanel.tileSize);
        int rightCol = Math.min(gamePanel.maxWorldCols - 1, (predicted.x + predicted.width - 1) / gamePanel.tileSize);
        int topRow = Math.max(0, predicted.y / gamePanel.tileSize);
        int bottomRow = Math.min(gamePanel.maxWorldRows - 1, (predicted.y + predicted.height - 1) / gamePanel.tileSize);

        // Iterate over the tiles in that area and test precise intersections using
        // tile.hitBox
        for (int col = leftCol; col <= rightCol; col++) {
            for (int row = topRow; row <= bottomRow; row++) {
                int tileNum = gamePanel.tileManager.mapTileNum[gamePanel.currentMap][col][row];

                if (tileNum < 0 || tileNum >= gamePanel.tileManager.tile.length) {
                    continue;
                }

                src.tiles.Tile tile = gamePanel.tileManager.tile[tileNum];
                if (tile == null || !tile.collision) {
                    continue; // non-colliding tile
                }

                // If tile has no specific hitBox, treat whole tile as collision area
                Rectangle tileHit = (tile.hitBox.width > 0 && tile.hitBox.height > 0)
                        ? new Rectangle(col * gamePanel.tileSize + tile.hitBox.x,
                                row * gamePanel.tileSize + tile.hitBox.y,
                                tile.hitBox.width,
                                tile.hitBox.height)
                        : new Rectangle(col * gamePanel.tileSize, row * gamePanel.tileSize, gamePanel.tileSize,
                                gamePanel.tileSize);

                if (predicted.intersects(tileHit)) {
                    // Debug output to show which tile caused collision and its hitbox
                    if (tileNum == 35 || tileNum == 45 || tileNum == 46 || tileNum == 47) {
                        // Store collision location for visual debugging
                        gamePanel.lastCollisionWorldX = tileHit.x;
                        gamePanel.lastCollisionWorldY = tileHit.y;
                        gamePanel.lastCollisionTime = System.currentTimeMillis();
                    }
                    entity.collisionOn = true;
                    return;
                }
            }
        }
    }

    /**
     * Check collision with objects. Returns index of object collided (player only)
     * or 999.
     * Uses local rectangles so solidArea offsets remain unchanged.
     */
    public int checkObject(Entity entity, boolean player) {
        int index = 999;

        for (int i = 0; i < gamePanel.obj[gamePanel.currentMap].length; i++) {
            if (gamePanel.obj[gamePanel.currentMap][i] != null) {
                Entity obj = gamePanel.obj[gamePanel.currentMap][i];

                Rectangle entityRect = new Rectangle(entity.worldX + entity.solidArea.x,
                        entity.worldY + entity.solidArea.y, entity.solidArea.width, entity.solidArea.height);

                Rectangle objRect = new Rectangle(obj.worldX + obj.solidArea.x, obj.worldY + obj.solidArea.y,
                        obj.solidArea.width, obj.solidArea.height);

                switch (entity.direction) {
                    case "up":
                        entityRect.y -= entity.speed;
                        break;
                    case "down":
                        entityRect.y += entity.speed;
                        break;
                    case "left":
                        entityRect.x -= entity.speed;
                        break;
                    case "right":
                        entityRect.x += entity.speed;
                        break;
                    default:
                        break;
                }

                if (entityRect.intersects(objRect)) {
                    if (obj.collision) {
                        entity.collisionOn = true;
                    }
                    if (player) {
                        index = i;
                    }
                }
            }
        }

        return index;
    }

    public int checkEntity(Entity entity, Entity[][] target) {
        int index = 999;

        for (int i = 0; i < target[gamePanel.currentMap].length; i++) {
            if (target[gamePanel.currentMap][i] != null) {
                Entity other = target[gamePanel.currentMap][i];

                Rectangle entityRect = new Rectangle(entity.worldX + entity.solidArea.x,
                        entity.worldY + entity.solidArea.y, entity.solidArea.width, entity.solidArea.height);

                Rectangle otherRect = new Rectangle(other.worldX + other.solidArea.x, other.worldY + other.solidArea.y,
                        other.solidArea.width, other.solidArea.height);

                switch (entity.direction) {
                    case "up":
                        entityRect.y -= entity.speed;
                        break;
                    case "down":
                        entityRect.y += entity.speed;
                        break;
                    case "left":
                        entityRect.x -= entity.speed;
                        break;
                    case "right":
                        entityRect.x += entity.speed;
                        break;
                    default:
                        break;
                }

                if (entityRect.intersects(otherRect)) {
                    if (other != entity) {
                        entity.collisionOn = true;
                        index = i;
                    }
                }
            }
        }

        return index;
    }

    public boolean checkPlayer(Entity entity) {
        boolean contactPlayer = false;

        Rectangle entityRect = new Rectangle(entity.worldX + entity.solidArea.x,
                entity.worldY + entity.solidArea.y, entity.solidArea.width, entity.solidArea.height);

        Rectangle playerRect = new Rectangle(gamePanel.player.worldX + gamePanel.player.solidArea.x,
                gamePanel.player.worldY + gamePanel.player.solidArea.y, gamePanel.player.solidArea.width,
                gamePanel.player.solidArea.height);

        switch (entity.direction) {
            case "up":
                entityRect.y -= entity.speed;
                break;
            case "down":
                entityRect.y += entity.speed;
                break;
            case "left":
                entityRect.x -= entity.speed;
                break;
            case "right":
                entityRect.x += entity.speed;
                break;
            default:
                break;
        }

        if (entityRect.intersects(playerRect)) {
            entity.collisionOn = true;
            contactPlayer = true;
        }

        return contactPlayer;
    }

}
