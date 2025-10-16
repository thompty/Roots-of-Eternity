package src.main;

import java.util.List;
import java.util.Map;

public class SaveData {
    public int version = 1;
    public int currentMap;
    public int worldX;
    public int worldY;
    public String direction;
    public int health;
    public int maxHealth;
    public int level;
    public int coin;
    public List<String> inventory; // item IDs
    public Map<String, String> equipped; // e.g. { "weapon": "OBJ_Sword_Normal", "shield": "OBJ_Shield_Wood" }
}
