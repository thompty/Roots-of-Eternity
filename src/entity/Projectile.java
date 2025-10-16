package src.entity;

import src.main.GamePanel;

public class Projectile extends Entity {

    Entity user;

    public Projectile(GamePanel gamePanel) {
        super(gamePanel);

    }

    public void set(int worldX, int worldY, String direction, boolean alive, Entity user){
        this.worldX = worldX;
        this.worldY = worldY;
        this.direction = direction;
        this.alive = alive;
        this.user = user;
        this.health = maxHealth;
    }

    public void update(){

        if(user == gamePanel.player){
            int monsterIndex = gamePanel.collisionChecker.checkEntity(this, gamePanel.monster);
            if(monsterIndex != 999){
                gamePanel.player.damageMonster(monsterIndex, attack);
                alive = false;
            }
        }
        if(user != gamePanel.player){
            boolean contactPlayer = gamePanel.collisionChecker.checkPlayer(this);
            if(gamePanel.player.immunity == false && contactPlayer == true){
                damagePlayer(attack);
                alive = false;
            }
        }

        switch(direction){
            case "up": worldY -= speed;
                break;
            case "down": worldY += speed;
                break;
            case "left": worldX -= speed;
                break;
            case "right": worldX += speed;
                break;
        }

        health--;
        if(health <= 0){
            alive = false;
        }

        spriteCounter++;
        if(spriteCounter > 12){
            if(spriteNum == 1){
                spriteNum = 2;
            }
            else if(spriteNum == 2){
                spriteNum = 1;
            }
            spriteCounter = 0;
        }
    }

    public boolean haveResource(Entity user){
        boolean haveResource = false;
        return haveResource;
    }

    public void subtractResource(Entity user){}

}
