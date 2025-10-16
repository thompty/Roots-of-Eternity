package src.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * SQLite-backed SaveManager. Uses a database file named `configData.db` in the
 * working
 * directory (project root). Creates the minimal tables on first use.
 */
public class SaveManager {
    private final GamePanel gp;
    private final Path dbPath;
    private final String jdbcUrl;

    public SaveManager(GamePanel gp) {
        this.gp = gp;
        this.dbPath = Paths.get("configData.db").toAbsolutePath();
        this.jdbcUrl = "jdbc:sqlite:" + dbPath.toString();
        try {
            Files.createDirectories(dbPath.getParent());
        } catch (Exception ex) {
            // ignore - parent may be null for relative paths
        }
        try {
            initDb();
        } catch (SQLException ex) {
            // If DB init fails, log to console; callers will receive IOException on
            // save/load
            ex.printStackTrace();
        }
    }

    // Always enable foreign key enforcement for SQLite connections so ON DELETE
    // CASCADE works
    private Connection openConnection() throws SQLException {
        // Ensure the SQLite JDBC driver class is available. Modern drivers auto-
        // register via ServiceLoader, but explicitly loading the class gives a
        // clearer error message when the driver JAR is not present on the
        // runtime classpath.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException(
                    "SQLite JDBC driver not found on classpath. Please add the sqlite-jdbc JAR to your runtime classpath.",
                    cnfe);
        }

        Connection c = DriverManager.getConnection(jdbcUrl);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    private void initDb() throws SQLException {
        String sqlSaves = "CREATE TABLE IF NOT EXISTS saves ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL UNIQUE,"
                + "timestamp INTEGER NOT NULL DEFAULT (strftime('%s','now')) ,"
                + "current_map INTEGER, world_x INTEGER, world_y INTEGER, direction TEXT,"
                + "health INTEGER, max_health INTEGER, level INTEGER, coin INTEGER,"
                + "weapon TEXT, shield TEXT"
                + ")";

        String sqlInv = "CREATE TABLE IF NOT EXISTS inventory ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "save_id INTEGER NOT NULL,"
                + "slot_index INTEGER NOT NULL,"
                + "item_name TEXT NOT NULL,"
                + "FOREIGN KEY(save_id) REFERENCES saves(id) ON DELETE CASCADE"
                + ")";

        String sqlWorld = "CREATE TABLE IF NOT EXISTS world_entities ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "save_id INTEGER NOT NULL,"
                + "map_index INTEGER NOT NULL,"
                + "category TEXT NOT NULL,"
                + "slot_index INTEGER NOT NULL,"
                + "item_name TEXT NOT NULL,"
                + "world_x INTEGER NOT NULL,"
                + "world_y INTEGER NOT NULL,"
                + "health INTEGER,"
                + "alive INTEGER,"
                + "direction TEXT,"
                + "FOREIGN KEY(save_id) REFERENCES saves(id) ON DELETE CASCADE"
                + ")";

        try (Connection c = openConnection(); Statement s = c.createStatement()) {
            s.execute(sqlSaves);
            s.execute(sqlInv);
            s.execute(sqlWorld);
            // tuner_configs stores hitbox tuner rectangles. key is a string like
            // 'player', 'tile41', 'class_mon:MON_GreenSlime', 'class_item:OBJ_Coin_Bronze'
            String sqlTuner = "CREATE TABLE IF NOT EXISTS tuner_configs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "save_id INTEGER," +
                    "key TEXT NOT NULL," +
                    "x INTEGER, y INTEGER, w INTEGER, h INTEGER," +
                    "FOREIGN KEY(save_id) REFERENCES saves(id) ON DELETE CASCADE" +
                    ")";
            s.execute(sqlTuner);
        }
    }

    /**
     * Save tuner config map into DB. If saveName is null or not found, entries are
     * stored as global (save_id NULL).
     * The map keys should be the same keys used by the file format (player, tile41,
     * class_mon:Name, class_item:Name, etc.).
     */
    public void saveTunerConfigToDb(java.util.Map<String, int[]> map, String saveName) throws IOException {
        try (Connection c = openConnection()) {
            Long saveId = null;
            if (saveName != null && !saveName.isBlank()) {
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM saves WHERE name = ?")) {
                    ps.setString(1, saveName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            saveId = rs.getLong(1);
                    }
                }
            }

            // delete existing for this saveId (NULL handling)
            if (saveId == null) {
                try (PreparedStatement pd = c.prepareStatement("DELETE FROM tuner_configs WHERE save_id IS NULL")) {
                    pd.executeUpdate();
                }
            } else {
                try (PreparedStatement pd = c.prepareStatement("DELETE FROM tuner_configs WHERE save_id = ?")) {
                    pd.setLong(1, saveId);
                    pd.executeUpdate();
                }
            }

            String insert = "INSERT INTO tuner_configs(save_id, key, x, y, w, h) VALUES(?,?,?,?,?,?)";
            try (PreparedStatement ins = c.prepareStatement(insert)) {
                for (java.util.Map.Entry<String, int[]> e : map.entrySet()) {
                    if (saveId == null)
                        ins.setNull(1, java.sql.Types.INTEGER);
                    else
                        ins.setLong(1, saveId);
                    ins.setString(2, e.getKey());
                    int[] v = e.getValue();
                    ins.setInt(3, v[0]);
                    ins.setInt(4, v[1]);
                    ins.setInt(5, v[2]);
                    ins.setInt(6, v[3]);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to save tuner config to DB", ex);
        }
    }

    /**
     * Load tuner config map from DB. Returns a map of key->int[4] (x,y,w,h).
     * If saveName provided, will return global entries overridden by per-save
     * entries.
     */
    public java.util.Map<String, int[]> loadTunerConfigFromDb(String saveName) throws IOException {
        try (Connection c = openConnection()) {
            java.util.Map<String, int[]> out = new java.util.HashMap<>();
            // load global (save_id IS NULL)
            try (PreparedStatement ps = c
                    .prepareStatement("SELECT key,x,y,w,h FROM tuner_configs WHERE save_id IS NULL")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString(1);
                        int x = rs.getInt(2);
                        int y = rs.getInt(3);
                        int w = rs.getInt(4);
                        int h = rs.getInt(5);
                        out.put(key, new int[] { x, y, w, h });
                    }
                }
            }

            if (saveName != null && !saveName.isBlank()) {
                Long saveId = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM saves WHERE name = ?")) {
                    ps.setString(1, saveName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            saveId = rs.getLong(1);
                    }
                }
                if (saveId != null) {
                    try (PreparedStatement ps = c
                            .prepareStatement("SELECT key,x,y,w,h FROM tuner_configs WHERE save_id = ?")) {
                        ps.setLong(1, saveId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String key = rs.getString(1);
                                int x = rs.getInt(2);
                                int y = rs.getInt(3);
                                int w = rs.getInt(4);
                                int h = rs.getInt(5);
                                // override or add
                                out.put(key, new int[] { x, y, w, h });
                            }
                        }
                    }
                }
            }
            return out;
        } catch (SQLException ex) {
            throw new IOException("Failed to load tuner config from DB", ex);
        }
    }

    /**
     * Save under a given name. The filename parameter is used as the save name.
     */
    public void save(String filename) throws IOException {
        // Delegate to saveAndReturnName for unified behavior and keep compatibility
        saveAndReturnName(filename);
    }

    /**
     * Save and return the name used. If filename is null/empty, a new unique name
     * is generated.
     */
    public String saveAndReturnName(String filename) throws IOException {
        try (Connection c = openConnection()) {
            c.setAutoCommit(false);
            try {
                // If filename not provided, generate a unique name
                String nameToUse = filename;
                if (nameToUse == null || nameToUse.isBlank()) {
                    nameToUse = generateNextSaveName(c);
                } else {
                    // delete the existing save with that name so we overwrite
                    try (PreparedStatement pd = c.prepareStatement("DELETE FROM saves WHERE name = ?")) {
                        pd.setString(1, nameToUse);
                        pd.executeUpdate();
                    }
                }

                String insertSave = "INSERT INTO saves(name, current_map, world_x, world_y, direction, "
                        + "health, max_health, level, coin, weapon, shield) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

                long saveId;
                try (PreparedStatement ps = c.prepareStatement(insertSave, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nameToUse);
                    ps.setInt(2, gp.currentMap);
                    ps.setInt(3, gp.player.worldX);
                    ps.setInt(4, gp.player.worldY);
                    ps.setString(5, gp.player.direction);
                    ps.setInt(6, gp.player.health);
                    ps.setInt(7, gp.player.maxHealth);
                    ps.setInt(8, gp.player.level);
                    ps.setInt(9, gp.player.coin);
                    ps.setString(10,
                            gp.player.currentWeapon != null ? gp.player.currentWeapon.getClass().getSimpleName()
                                    : null);
                    ps.setString(11,
                            gp.player.currentShield != null ? gp.player.currentShield.getClass().getSimpleName()
                                    : null);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next())
                            throw new SQLException("Failed to obtain save id");
                        saveId = keys.getLong(1);
                    }
                }

                String insertInv = "INSERT INTO inventory(save_id, slot_index, item_name) VALUES(?,?,?)";
                try (PreparedStatement pinv = c.prepareStatement(insertInv)) {
                    for (int i = 0; i < gp.player.inventory.size(); i++) {
                        Object o = gp.player.inventory.get(i);
                        if (o instanceof src.entity.Entity) {
                            pinv.setLong(1, saveId);
                            pinv.setInt(2, i);
                            pinv.setString(3, o.getClass().getSimpleName());
                            pinv.addBatch();
                        }
                    }
                    pinv.executeBatch();
                }

                // Save world entities: objects, NPCs, monsters
                String insertWorld = "INSERT INTO world_entities(save_id, map_index, category, slot_index, item_name, world_x, world_y, health, alive, direction) VALUES(?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement pwe = c.prepareStatement(insertWorld)) {
                    // objects
                    for (int map = 0; map < gp.maxMap; map++) {
                        for (int slot = 0; slot < gp.obj[map].length; slot++) {
                            src.entity.Entity e = gp.obj[map][slot];
                            if (e != null) {
                                pwe.setLong(1, saveId);
                                pwe.setInt(2, map);
                                pwe.setString(3, "obj");
                                pwe.setInt(4, slot);
                                pwe.setString(5, e.getClass().getSimpleName());
                                pwe.setInt(6, e.worldX);
                                pwe.setInt(7, e.worldY);
                                // objects have no health/alive/direction in general
                                pwe.setNull(8, java.sql.Types.INTEGER);
                                pwe.setNull(9, java.sql.Types.INTEGER);
                                pwe.setNull(10, java.sql.Types.VARCHAR);
                                pwe.addBatch();
                            }
                        }
                    }
                    // npcs
                    for (int map = 0; map < gp.maxMap; map++) {
                        for (int slot = 0; slot < gp.npc[map].length; slot++) {
                            src.entity.Entity e = gp.npc[map][slot];
                            if (e != null) {
                                pwe.setLong(1, saveId);
                                pwe.setInt(2, map);
                                pwe.setString(3, "npc");
                                pwe.setInt(4, slot);
                                pwe.setString(5, e.getClass().getSimpleName());
                                pwe.setInt(6, e.worldX);
                                pwe.setInt(7, e.worldY);
                                // NPCs may have health/direction; save if present
                                if (e.maxHealth != 0) {
                                    pwe.setInt(8, e.health);
                                    pwe.setInt(9, e.alive ? 1 : 0);
                                } else {
                                    pwe.setNull(8, java.sql.Types.INTEGER);
                                    pwe.setNull(9, java.sql.Types.INTEGER);
                                }
                                pwe.setString(10, e.direction != null ? e.direction : null);
                                pwe.addBatch();
                            }
                        }
                    }
                    // monsters
                    for (int map = 0; map < gp.maxMap; map++) {
                        for (int slot = 0; slot < gp.monster[map].length; slot++) {
                            src.entity.Entity e = gp.monster[map][slot];
                            if (e != null) {
                                // Save only monsters that are alive
                                if (!e.alive)
                                    continue;
                                pwe.setLong(1, saveId);
                                pwe.setInt(2, map);
                                pwe.setString(3, "monster");
                                pwe.setInt(4, slot);
                                pwe.setString(5, e.getClass().getSimpleName());
                                pwe.setInt(6, e.worldX);
                                pwe.setInt(7, e.worldY);
                                pwe.setInt(8, e.health);
                                pwe.setInt(9, e.alive ? 1 : 0);
                                pwe.setString(10, e.direction != null ? e.direction : null);
                                pwe.addBatch();
                            }
                        }
                    }
                    pwe.executeBatch();
                }

                c.commit();
                return nameToUse;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to save to database", ex);
        }
    }

    /**
     * Load save by name (filename parameter is treated as save name).
     */
    public void load(String filename) throws IOException {
        try (Connection c = openConnection()) {
            String sel = "SELECT id, current_map, world_x, world_y, direction, health, max_health, level, coin, weapon, shield FROM saves WHERE name = ?";
            long saveId = -1;
            try (PreparedStatement ps = c.prepareStatement(sel)) {
                ps.setString(1, filename);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new IOException("Save not found: " + filename);
                    saveId = rs.getLong("id");
                    gp.currentMap = rs.getInt("current_map");
                    gp.player.worldX = rs.getInt("world_x");
                    gp.player.worldY = rs.getInt("world_y");
                    String dir = rs.getString("direction");
                    gp.player.direction = dir != null ? dir : gp.player.direction;
                    gp.player.health = rs.getInt("health");
                    gp.player.maxHealth = rs.getInt("max_health");
                    gp.player.level = rs.getInt("level");
                    gp.player.coin = rs.getInt("coin");
                    String weapon = rs.getString("weapon");
                    String shield = rs.getString("shield");
                    gp.player.currentWeapon = weapon != null ? gp.assetSetter.createItemByName(weapon) : null;
                    gp.player.currentShield = shield != null ? gp.assetSetter.createItemByName(shield) : null;
                }
            }

            // inventory
            gp.player.inventory.clear();
            String selInv = "SELECT slot_index, item_name FROM inventory WHERE save_id = ? ORDER BY slot_index";
            try (PreparedStatement ps = c.prepareStatement(selInv)) {
                ps.setLong(1, saveId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String itemName = rs.getString("item_name");
                        src.entity.Entity item = gp.assetSetter.createItemByName(itemName);
                        if (item != null)
                            gp.player.inventory.add(item);
                    }
                }
            }

            // Load world entities
            // Clear current world arrays first
            for (int m = 0; m < gp.maxMap; m++) {
                for (int i = 0; i < gp.obj[m].length; i++)
                    gp.obj[m][i] = null;
                for (int i = 0; i < gp.npc[m].length; i++)
                    gp.npc[m][i] = null;
                for (int i = 0; i < gp.monster[m].length; i++)
                    gp.monster[m][i] = null;
            }

            String selWorld = "SELECT map_index, category, slot_index, item_name, world_x, world_y, health, alive, direction FROM world_entities WHERE save_id = ?";
            try (PreparedStatement ps = c.prepareStatement(selWorld)) {
                ps.setLong(1, saveId);
                try (ResultSet rs = ps.executeQuery()) {
                    int restored = 0;
                    while (rs.next()) {
                        int mapIndex = rs.getInt("map_index");
                        String category = rs.getString("category");
                        int slotIndex = rs.getInt("slot_index");
                        String itemName = rs.getString("item_name");
                        int wx = rs.getInt("world_x");
                        int wy = rs.getInt("world_y");
                        Integer healthVal = rs.getObject("health") != null ? rs.getInt("health") : null;
                        Integer aliveVal = rs.getObject("alive") != null ? rs.getInt("alive") : null;
                        String dirVal = rs.getString("direction");

                        src.entity.Entity e = gp.assetSetter.createItemByName(itemName);
                        if (e == null)
                            continue;
                        e.worldX = wx;
                        e.worldY = wy;
                        if (healthVal != null) {
                            e.health = healthVal;
                        }
                        if (aliveVal != null) {
                            e.alive = aliveVal == 1;
                            e.dead = !e.alive;
                        }
                        if (dirVal != null) {
                            e.direction = dirVal;
                        }
                        switch (category) {
                            case "obj":
                                if (slotIndex >= 0 && slotIndex < gp.obj[mapIndex].length)
                                    gp.obj[mapIndex][slotIndex] = e;
                                break;
                            case "npc":
                                if (slotIndex >= 0 && slotIndex < gp.npc[mapIndex].length)
                                    gp.npc[mapIndex][slotIndex] = (src.entity.Entity) e;
                                break;
                            case "monster":
                                if (slotIndex >= 0 && slotIndex < gp.monster[mapIndex].length)
                                    gp.monster[mapIndex][slotIndex] = (src.entity.Entity) e;
                                break;
                            default:
                                break;
                        }
                        restored++;
                    }
                    System.out.println("[SAVE] Restored world entities: " + restored + " for save '" + filename + "'");
                }
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load from database", ex);
        }
    }

    /**
     * Return an array of save names present in the database ordered by timestamp
     * desc.
     */
    public String[] listSaves() throws IOException {
        try (Connection c = openConnection();
                PreparedStatement ps = c.prepareStatement("SELECT name FROM saves ORDER BY timestamp DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                java.util.List<String> out = new java.util.ArrayList<>();
                while (rs.next())
                    out.add(rs.getString("name"));
                return out.toArray(new String[0]);
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to list saves", ex);
        }
    }

    /**
     * Delete a save by name.
     */
    public void deleteSave(String name) throws IOException {
        try (Connection c = openConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM saves WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IOException("Failed to delete save", ex);
        }
    }

    // Generate a new save name like save1, save2 ... choosing the next unused
    // numeric suffix
    private String generateNextSaveName(Connection c) throws SQLException {
        String query = "SELECT name FROM saves ORDER BY timestamp DESC";
        try (PreparedStatement ps = c.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {
            int max = 0;
            while (rs.next()) {
                String n = rs.getString("name");
                if (n == null)
                    continue;
                // accept names like save<number> or save<number>.json
                String digits = n.replaceAll("^.*?(\\d+).*$", "$1");
                try {
                    int v = Integer.parseInt(digits);
                    if (v > max)
                        max = v;
                } catch (NumberFormatException ex) {
                    // ignore non-matching names
                }
            }
            return "save" + (max + 1) + ".json";
        }
    }
}
