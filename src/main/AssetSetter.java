package src.main;

import src.entity.NPC_BoneMender;
import src.entity.NPC_Merchant;
import src.entity.NPC_OldMan;
import src.monster.MON_GreenSlime;
import src.object.OBJ_Mana_Crystal;
import src.object.OBJ_Coin_Bronze;
import src.object.OBJ_Potion_Red;
import src.object.OBJ_Heart;

public class AssetSetter {
    GamePanel gamePanel;

    public AssetSetter(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    // Lightweight factory used by SaveManager to recreate items/entities by simple
    // class name
    public src.entity.Entity createItemByName(String name) {
        // Objects
        switch (name) {
            case "OBJ_Coin_Bronze":
                return new src.object.OBJ_Coin_Bronze(gamePanel);
            case "OBJ_Heart":
                return new src.object.OBJ_Heart(gamePanel);
            case "OBJ_Potion_Red":
                return new src.object.OBJ_Potion_Red(gamePanel);
            case "OBJ_Mana_Crystal":
                return new src.object.OBJ_Mana_Crystal(gamePanel);
            case "OBJ_Sword_Normal":
                return new src.object.OBJ_Sword_Normal(gamePanel);
            case "OBJ_Shield_Wood":
                return new src.object.OBJ_Shield_Wood(gamePanel);
            case "OBJ_Boots":
                return new src.object.OBJ_Boots(gamePanel);
            case "OBJ_Axe":
                return new src.object.OBJ_Axe(gamePanel);
            case "OBJ_Key":
                return new src.object.OBJ_Key(gamePanel);
            // NPCs / Monsters if needed
            case "NPC_OldMan":
                return new src.entity.NPC_OldMan(gamePanel);
            case "NPC_BoneMender":
                return new src.entity.NPC_BoneMender(gamePanel);
            case "NPC_Merchant":
                return new src.entity.NPC_Merchant(gamePanel);
            case "MON_GreenSlime":
                return new src.monster.MON_GreenSlime(gamePanel);
            default:
                return null;
        }
    }

    public void setObject() {
        int mapNum = 0;
        // Ensure the object slots for this map are empty before placing
        for (int i = 0; i < gamePanel.obj[mapNum].length; i++) {
            gamePanel.obj[mapNum][i] = null;
        }
        // Helper that finds the first null slot in obj[map] and places the entity there
        java.util.function.BiConsumer<src.entity.Entity, Integer> place = (ent, map) -> {
            for (int idx = 0; idx < gamePanel.obj[map].length; idx++) {
                if (gamePanel.obj[map][idx] == null) {
                    gamePanel.obj[map][idx] = ent;
                    return;
                }
            }
            // if no slot available, ignore
        };

        src.entity.Entity o1 = new OBJ_Coin_Bronze(gamePanel);
        o1.worldX = gamePanel.tileSize * 21;
        o1.worldY = gamePanel.tileSize * 22;
        place.accept(o1, mapNum);

        src.entity.Entity o2 = new OBJ_Coin_Bronze(gamePanel);
        o2.worldX = gamePanel.tileSize * 23;
        o2.worldY = gamePanel.tileSize * 25;
        place.accept(o2, mapNum);

        src.entity.Entity o3 = new OBJ_Heart(gamePanel);
        o3.worldX = gamePanel.tileSize * 25;
        o3.worldY = gamePanel.tileSize * 23;
        place.accept(o3, mapNum);

        src.entity.Entity o4 = new OBJ_Potion_Red(gamePanel);
        o4.worldX = gamePanel.tileSize * 24;
        o4.worldY = gamePanel.tileSize * 21;
        place.accept(o4, mapNum);

        src.entity.Entity o5 = new OBJ_Mana_Crystal(gamePanel);
        o5.worldX = gamePanel.tileSize * 19;
        o5.worldY = gamePanel.tileSize * 21;
        place.accept(o5, mapNum);

    }

    public void setNPC() {
        int mapNum = 0;
        // Clear NPC slots for map 0 before placing
        for (int i = 0; i < gamePanel.npc[mapNum].length; i++) {
            gamePanel.npc[mapNum][i] = null;
        }
        // place into first null NPC slot
        java.util.function.BiConsumer<src.entity.Entity, Integer> placeNPC = (ent, map) -> {
            for (int idx = 0; idx < gamePanel.npc[map].length; idx++) {
                if (gamePanel.npc[map][idx] == null) {
                    gamePanel.npc[map][idx] = ent;
                    return;
                }
            }
        };

        src.entity.Entity n1 = new NPC_OldMan(gamePanel);
        n1.worldX = gamePanel.tileSize * 650;
        n1.worldY = gamePanel.tileSize * 500;
        placeNPC.accept(n1, mapNum);

        src.entity.Entity boneMender1 = new NPC_BoneMender(gamePanel);
        boneMender1.worldX = gamePanel.tileSize * 630;
        boneMender1.worldY = gamePanel.tileSize * 540;
        placeNPC.accept(boneMender1, mapNum);

        mapNum = 1;
        // Clear NPC slots for map 1 before placing
        for (int i = 0; i < gamePanel.npc[mapNum].length; i++) {
            gamePanel.npc[mapNum][i] = null;
        }
        src.entity.Entity n2 = new NPC_Merchant(gamePanel);
        n2.worldX = gamePanel.tileSize * 12;
        n2.worldY = gamePanel.tileSize * 7;
        placeNPC.accept(n2, mapNum);
    }

    public void setMonster() {
        int mapNum = 0;
        // Clear existing monsters on this map before spawning new ones
        for (int i = 0; i < gamePanel.monster[mapNum].length; i++) {
            gamePanel.monster[mapNum][i] = null;
        }
        java.util.function.BiConsumer<src.entity.Entity, Integer> placeMon = (ent, map) -> {
            for (int idx = 0; idx < gamePanel.monster[map].length; idx++) {
                if (gamePanel.monster[map][idx] == null) {
                    gamePanel.monster[map][idx] = ent;
                    return;
                }
            }
        };

        src.entity.Entity m1 = new MON_GreenSlime(gamePanel);
        m1.worldX = gamePanel.tileSize * 600;
        m1.worldY = gamePanel.tileSize * 524;
        placeMon.accept(m1, mapNum);

        src.entity.Entity m2 = new MON_GreenSlime(gamePanel);
        m2.worldX = gamePanel.tileSize * 590;
        m2.worldY = gamePanel.tileSize * 524;
        placeMon.accept(m2, mapNum);

        src.entity.Entity m3 = new MON_GreenSlime(gamePanel);
        m3.worldX = gamePanel.tileSize * 595;
        m3.worldY = gamePanel.tileSize * 524;
        placeMon.accept(m3, mapNum);

    }
}
