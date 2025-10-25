package src.tiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.UtilityTool;

public class TileManager {
    GamePanel gamePanel;
    public Tile[] tile;
    public java.awt.Color[] tileColor; // representative color for minimap
    public int mapTileNum[][][];
    // If true, treat incoming map numbers as Tiled GIDs (1-based) and convert to
    // 0-based tile[] indices
    public boolean convertTiledGIDs = true;
    public boolean drawPath = true;

    // Minimap cache and metadata
    public java.awt.image.BufferedImage[] miniMapImage;
    public int[] mapCols;
    public int[] mapRows;
    public boolean showMiniMap = true;
    public int miniMapDrawWidth = 200;
    public int miniMapMargin = 8;
    // avoid spamming the console if placeholder drawn repeatedly
    public boolean[] miniMapPlaceholderLogged;

    // Custom font for overlays
    private java.awt.Font alkFont; // loaded from res/fonts/Alkhemikal.ttf

    public TileManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        tile = new Tile[256];
        tileColor = new java.awt.Color[tile.length];
        mapTileNum = new int[gamePanel.maxMap][gamePanel.maxWorldCols][gamePanel.maxWorldRows];
        miniMapImage = new java.awt.image.BufferedImage[gamePanel.maxMap];
        mapCols = new int[gamePanel.maxMap];
        mapRows = new int[gamePanel.maxMap];
        miniMapPlaceholderLogged = new boolean[gamePanel.maxMap];

        // Load custom overlay font (Alkhemikal.ttf) if available
        try (java.io.InputStream fis = getClass().getResourceAsStream("/res/fonts/Alkhemikal.ttf")) {
            if (fis != null) {
                alkFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fis);
            }
        } catch (Exception ex) {
            // If loading fails, keep alkFont null and fall back to default font
            alkFont = null;
        }

        getTileImage();
        loadMap("/res/maps/Overworld Base CSV.csv", 0);
        loadMap("/res/maps/hut.txt", 1);
        // Attempt to build minimaps immediately so toggle shows real image instead of
        // placeholder
        try {
            ensureMiniMapBuilt(0);
            ensureMiniMapBuilt(1);
        } catch (Exception ex) {
            // ignore
        }
    }

    public void getTileImage() {
        try {
            setup(7, "Grass_1", false);
            setup(44, "Grass_1", false);
            setup(35, "Water_1", true);
            setup(45, "Water_1", true);
            setup(46, "Water_2", true);
            setup(47, "Water_3", true);
            setup(17, "Dirt_1", false);
            setup(25, "House_1", true);
            setup(15, "House_2", true);
            setup(5, "House_3", true);
            setup(24, "House_4", true);
            setup(14, "House_5", true);
            setup(4, "House_6", true);
            setup(23, "House_7", true);
            setup(13, "House_8", true);
            setup(3, "House_9", true);
            setup(22, "House_10", true);
            setup(12, "House_11", true);
            setup(2, "House_12", true);
            setup(21, "House_13", true);
            setup(11, "House_14", true);
            setup(1, "House_15", true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup(int index, String imageName, boolean collision) {
        UtilityTool uTool = new UtilityTool();

        try {
            tile[index] = new Tile();
            tile[index].image = ImageIO.read(getClass().getResourceAsStream("/res/tiles/" + imageName + ".png"));
            tile[index].image = uTool.scaleImage(tile[index].image, gamePanel.tileSize, gamePanel.tileSize);
            tile[index].collision = collision;

            if (collision) {
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, gamePanel.tileSize);
            }

            // Custom hitboxes for specific tiles

            // Water tiles
            if (index == 35 || index == 45 || index == 46 || index == 47) {
                int waterH = gamePanel.tileSize;
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, waterH);
            }
            // House tiles - adjust hitboxes based on house part
            // House bottom parts (likely have doors/openings)
            if (index == 1 || index == 2 || index == 3) { // House_15, House_12, House_9
                // Bottom row - reduce hitbox height to allow entry
                int doorH = gamePanel.tileSize * 2 / 3;
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, doorH);
            }

            // House middle parts
            if (index == 11 || index == 12 || index == 13) { // House_14, House_11, House_8
                // Full collision for middle sections
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, gamePanel.tileSize);
            }

            // House top parts
            if (index == 21 || index == 22 || index == 23) { // House_13, House_10, House_7
                // Full collision for roof sections
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, gamePanel.tileSize);
            }

            // House left column
            if (index == 4 || index == 14 || index == 24) { // House_6, House_5, House_4
                // Full collision
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, gamePanel.tileSize);
            }

            // House center column
            if (index == 5 || index == 15 || index == 25) { // House_3, House_2, House_1
                // Full collision
                tile[index].hitBox = new Rectangle(0, 0, gamePanel.tileSize, gamePanel.tileSize);
            }

            sampleTileColor(index);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sampleTileColor(int index) {
        try {
            if (index < 0 || index >= tile.length)
                return;
            if (tile[index] == null || tile[index].image == null)
                return;
            java.awt.image.BufferedImage bi = tile[index].image;
            long r = 0, g = 0, b = 0;
            int samples = 0;
            int step = Math.max(1, bi.getWidth() / 4);
            for (int y = 0; y < bi.getHeight(); y += step) {
                for (int x = 0; x < bi.getWidth(); x += step) {
                    int rgb = bi.getRGB(x, y);
                    java.awt.Color c = new java.awt.Color(rgb, true);
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                    samples++;
                }
            }
            if (samples > 0) {
                tileColor[index] = new java.awt.Color((int) (r / samples), (int) (g / samples), (int) (b / samples));
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    public void loadMap(String filePath, int map) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("Map resource not found: " + filePath);
                return;
            }

            java.util.List<String> lines = new java.util.ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0)
                        continue;
                    lines.add(line);
                }
            }

            if (lines.isEmpty()) {
                System.out.println("Map file appears empty: " + filePath);
                return;
            }

            int detectedRows = lines.size();
            int detectedCols = 0;
            boolean usesComma = false;
            for (String l : lines) {
                if (l.contains(","))
                    usesComma = true;
                String[] toks = usesComma ? l.split(",") : l.split("\\s+");
                detectedCols = Math.max(detectedCols, toks.length);
            }

            System.out.println(
                    "Loading map '" + filePath + "' -> detected cols=" + detectedCols + ", rows=" + detectedRows
                            + ", usingComma=" + usesComma);

            int rowsToFill = Math.min(detectedRows, gamePanel.maxWorldRows);
            int colsToFill = Math.min(detectedCols, gamePanel.maxWorldCols);

            for (int row = 0; row < rowsToFill; row++) {
                String line = lines.get(row);
                String[] numbers = usesComma ? line.split(",") : line.split("\\s+");

                for (int col = 0; col < colsToFill; col++) {
                    if (col >= numbers.length) {
                        mapTileNum[map][col][row] = 0;
                        continue;
                    }

                    String s = numbers[col].trim();
                    if (s.length() == 0) {
                        mapTileNum[map][col][row] = 0;
                        continue;
                    }

                    try {
                        int num = Integer.parseInt(s);
                        if (convertTiledGIDs && num > 0) {
                            num = num - 1;
                        }

                        if (num < 0 || num >= tile.length || tile[num] == null || tile[num].image == null) {
                            mapTileNum[map][col][row] = 0;
                            if (row < 3 && col < 3) {
                                System.out.println("Invalid/uninit tile at col:" + col + ",row:" + row + ",num:" + num);
                            }
                        } else {
                            mapTileNum[map][col][row] = num;
                        }
                    } catch (NumberFormatException nfe) {
                        mapTileNum[map][col][row] = 0;
                    }
                }

                for (int col = colsToFill; col < gamePanel.maxWorldCols; col++) {
                    mapTileNum[map][col][row] = 0;
                }
            }

            for (int row = rowsToFill; row < gamePanel.maxWorldRows; row++) {
                for (int col = 0; col < gamePanel.maxWorldCols; col++) {
                    mapTileNum[map][col][row] = 0;
                }
            }

            // store the actually used (clamped) map dimensions and build minimap with those
            mapCols[map] = colsToFill;
            mapRows[map] = rowsToFill;
            buildMiniMap(map, colsToFill, rowsToFill);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void buildMiniMap(int map, int cols, int rows) {
        try {
            System.out.println("[Minimap] buildMiniMap called map=" + map + " cols=" + cols + " rows=" + rows
                    + " maxWorldCols=" + gamePanel.maxWorldCols + " maxWorldRows=" + gamePanel.maxWorldRows);
            if (cols <= 0 || rows <= 0)
                return;

            int srcW = Math.max(0, Math.min(cols, gamePanel.maxWorldCols));
            int srcH = Math.max(0, Math.min(rows, gamePanel.maxWorldRows));
            System.out.println("[Minimap] computed srcW=" + srcW + " srcH=" + srcH);
            if (srcW == 0 || srcH == 0)
                return;
            java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(srcW, srcH,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D mg = src.createGraphics();

            int maxC = srcW;
            int maxR = srcH;
            if (map >= 0 && map < mapTileNum.length) {
                maxC = Math.min(maxC, mapTileNum[map].length);
                if (mapTileNum[map].length > 0) {
                    maxR = Math.min(maxR, mapTileNum[map][0].length);
                }
            }

            for (int r = 0; r < maxR; r++) {
                for (int c = 0; c < maxC; c++) {
                    int tnum = 0;
                    if (map >= 0 && map < mapTileNum.length && c >= 0 && c < mapTileNum[map].length && r >= 0
                            && r < mapTileNum[map][0].length) {
                        try {
                            tnum = mapTileNum[map][c][r];
                        } catch (ArrayIndexOutOfBoundsException aioob) {
                            System.out.println("[Minimap][ERROR] mapTileNum access out of bounds map=" + map
                                    + " c=" + c + " r=" + r + " mapTileNum[map].length=" + mapTileNum[map].length
                                    + " mapTileNum[map][0].length=" + mapTileNum[map][0].length);
                            tnum = 0;
                        }
                    }
                    java.awt.Color col = (tnum >= 0 && tnum < tileColor.length && tileColor[tnum] != null)
                            ? tileColor[tnum]
                            : new java.awt.Color(120, 120, 120);
                    mg.setColor(col);
                    mg.fillRect(c, r, 1, 1);
                }
            }
            mg.dispose();

            int dstW = miniMapDrawWidth;
            int dstH = Math.max(8, (int) ((double) dstW * maxR / Math.max(1, maxC)));
            java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(dstW, dstH,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = dst.createGraphics();
            g.drawImage(src, 0, 0, dstW, dstH, null);
            g.dispose();

            miniMapImage[map] = dst;
        } catch (ArrayIndexOutOfBoundsException aioob) {
            System.out.println("[Minimap][FATAL] buildMiniMap aborted due to out-of-bounds: " + aioob.getMessage());
            aioob.printStackTrace();
        }
    }

    /**
     * Ensure the minimap for the given map is built. If mapCols/mapRows are not
     * available, scan the mapTileNum to determine used extents.
     */
    public void ensureMiniMapBuilt(int map) {
        if (map < 0 || map >= miniMapImage.length)
            return;
        if (miniMapImage[map] != null)
            return;

        int cols = mapCols[map];
        int rows = mapRows[map];

        if (cols <= 0 || rows <= 0) {
            // scan for used extents
            int maxC = 0;
            int maxR = 0;
            for (int r = 0; r < mapTileNum[map][0].length; r++) {
                for (int c = 0; c < mapTileNum[map].length; c++) {
                    if (mapTileNum[map][c][r] != 0) {
                        if (c + 1 > maxC)
                            maxC = c + 1;
                        if (r + 1 > maxR)
                            maxR = r + 1;
                    }
                }
            }
            if (maxC <= 0)
                maxC = Math.min(gamePanel.maxWorldCols, 128);
            if (maxR <= 0)
                maxR = Math.min(gamePanel.maxWorldRows, 128);
            cols = maxC;
            rows = maxR;
            mapCols[map] = cols;
            mapRows[map] = rows;
        }

        buildMiniMap(map, cols, rows);
    }

    public void drawMiniMap(Graphics2D g2) {
        if (!showMiniMap)
            return;
        int map = gamePanel.currentMap;
        java.awt.image.BufferedImage img = miniMapImage[map];
        int drawW;
        int drawH;
        int sx;
        int sy;

        if (img == null) {
            // No cached minimap available yet; draw a visible placeholder so the toggle
            // produces an observable effect. This helps confirm overlay drawing works.
            drawW = Math.min(miniMapDrawWidth, gamePanel.screenWidth / 4);
            drawH = Math.max(24, drawW / 2);
            sx = gamePanel.screenWidth - drawW - miniMapMargin;
            sy = miniMapMargin;

            g2.setColor(new java.awt.Color(0, 0, 0, 200));
            g2.fillRect(sx - 4, sy - 4, drawW + 8, drawH + 8);
            g2.setColor(new java.awt.Color(100, 140, 180, 220));
            g2.fillRect(sx, sy, drawW, drawH);
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString("Minimap (building...)", sx + 6, sy + 14);

            // --- Coordinate overlay (placeholder) -------------------------------------
            // Compute the player's tile coordinate by converting world pixels -> tile index
            // We floor the division so the tile index is stable for a whole tile
            int pcol = Math.max(0,
                    (int) Math.floor((double) gamePanel.player.worldX / Math.max(1, gamePanel.tileSize)));
            int prow = Math.max(0,
                    (int) Math.floor((double) gamePanel.player.worldY / Math.max(1, gamePanel.tileSize)));
            String l1 = "Tile:(" + pcol + "," + prow + ")";
            // Render the text with Alkhemikal (half current UI font size) if available
            java.awt.Font oldFont = g2.getFont();
            java.awt.Font small = (alkFont != null)
                    ? alkFont.deriveFont(oldFont.getSize2D() * 0.5f)
                    : oldFont.deriveFont(oldFont.getSize2D() * 0.5f);
            g2.setFont(small);
            // Measure text to size the background rect and compute placement
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(l1); // text width in pixels
            int th = fm.getHeight() + 6; // text height + small vertical padding
            // Right-align under the minimap
            // We draw a background box of width (tw + 8). Align its right edge to the
            // minimap's right edge at (sx + drawW). The +4 re-centers text inside the
            // padded background box (because bg extends 4px left and right).
            int textX = sx + drawW - (tw + 8) + 4; // left coordinate for text baseline
            int gap = Math.max(6, miniMapMargin);
            // Place the baseline such that the TOP of the rectangle is fully below the
            // minimap
            int textY = sy + drawH + gap + fm.getAscent() + 4;
            // Compute background rect bounds from baseline
            int rectTop = textY - fm.getAscent() - 4;
            int rectBottom = rectTop + th;
            int maxBottom = gamePanel.screenHeight - miniMapMargin;
            // Clamp within screen bottom
            if (rectBottom > maxBottom) {
                rectTop = maxBottom - th;
                textY = rectTop + fm.getAscent() + 4;
            }
            // Background with slight transparency for readability on any scene
            g2.setColor(new java.awt.Color(0, 0, 0, 160));
            g2.fillRect(textX - 4, rectTop, tw + 8, th);
            // Foreground text
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString(l1, textX, textY);
            g2.setFont(oldFont);
            // lightweight debug (log only once per map)
            if (!miniMapPlaceholderLogged[map]) {
                System.out.println("[Minimap] no cached image for map=" + map + ", drawing placeholder.");
                miniMapPlaceholderLogged[map] = true;
            }
            return;
        }

        drawW = img.getWidth();
        drawH = img.getHeight();

        sx = gamePanel.screenWidth - drawW - miniMapMargin;
        sy = miniMapMargin;

        g2.setColor(new java.awt.Color(0, 0, 0, 160));
        g2.fillRect(sx - 4, sy - 4, drawW + 8, drawH + 8);

        g2.drawImage(img, sx, sy, null);

        int cols = mapCols[map];
        int rows = mapRows[map];
        if (cols <= 0 || rows <= 0)
            return;

        double tx = (double) gamePanel.player.worldX / (cols * gamePanel.tileSize);
        double ty = (double) gamePanel.player.worldY / (rows * gamePanel.tileSize);
        int px = sx + (int) (tx * drawW);
        int py = sy + (int) (ty * drawH);

        g2.setColor(new java.awt.Color(255, 0, 0, 200));
        g2.fillOval(px - 3, py - 3, 6, 6);

        double viewW = (double) gamePanel.screenWidth / (cols * gamePanel.tileSize);
        double viewH = (double) gamePanel.screenHeight / (rows * gamePanel.tileSize);
        int vrw = Math.max(2, (int) (viewW * drawW));
        int vrh = Math.max(2, (int) (viewH * drawH));
        int vrx = sx + (int) (tx * drawW) - vrw / 2;
        int vry = sy + (int) (ty * drawH) - vrh / 2;

        g2.setColor(new java.awt.Color(255, 255, 255, 120));
        g2.drawRect(vrx, vry, vrw, vrh);

        // --- Coordinate overlay (final minimap image) -------------------------------
        // Compute the player's tile coordinate clamped to known map extents
        int pcol = Math.max(0, Math.min(cols - 1,
                (int) Math.floor((double) gamePanel.player.worldX / Math.max(1, gamePanel.tileSize))));
        int prow = Math.max(0, Math.min(rows - 1,
                (int) Math.floor((double) gamePanel.player.worldY / Math.max(1, gamePanel.tileSize))));
        String l1 = "Tile:(" + pcol + "," + prow + ")";
        // Render the text with Alkhemikal (half current UI font size) if available
        java.awt.Font oldFont = g2.getFont();
        java.awt.Font small = (alkFont != null)
                ? alkFont.deriveFont(oldFont.getSize2D() * 0.5f)
                : oldFont.deriveFont(oldFont.getSize2D() * 0.5f);
        g2.setFont(small);
        // Measure text to size the background rect and compute placement
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(l1); // text width in pixels
        int th = fm.getHeight() + 6; // text height + small vertical padding
        // Right-align under the minimap (see placeholder branch comment for math)
        int textX = sx + drawW - (tw + 8) + 4;
        int gap = Math.max(6, miniMapMargin);
        // Place the baseline such that the TOP of the rectangle is fully below the
        // minimap
        int textY = sy + drawH + gap + fm.getAscent() + 4;
        int rectTop = textY - fm.getAscent() - 4;
        int rectBottom = rectTop + th;
        int maxBottom = gamePanel.screenHeight - miniMapMargin;
        if (rectBottom > maxBottom) {
            rectTop = maxBottom - th;
            textY = rectTop + fm.getAscent() + 4;
        }
        // Background with slight transparency for readability on any scene
        g2.setColor(new java.awt.Color(0, 0, 0, 160));
        g2.fillRect(textX - 4, rectTop, tw + 8, th);
        g2.setColor(java.awt.Color.WHITE);
        g2.drawString(l1, textX, textY);
        g2.setFont(oldFont);
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gamePanel.maxWorldCols && worldRow < gamePanel.maxWorldRows) {

            int tileNum = mapTileNum[gamePanel.currentMap][worldCol][worldRow]; // What tile image will be displayed

            if (tileNum < 0 || tileNum >= tile.length || tile[tileNum] == null || tile[tileNum].image == null) {
                worldCol++;
                if (worldCol == gamePanel.maxWorldCols) {
                    worldCol = 0;
                    worldRow++;
                }
                continue;
            }

            int worldX = worldCol * gamePanel.tileSize;
            int worldY = worldRow * gamePanel.tileSize;
            int screenX = worldX - gamePanel.player.worldX + gamePanel.player.screenX;
            int screenY = worldY - gamePanel.player.worldY + gamePanel.player.screenY;

            if (worldX + gamePanel.tileSize > gamePanel.player.worldX - gamePanel.player.screenX &&
                    worldX - gamePanel.tileSize < gamePanel.player.worldX + gamePanel.player.screenX &&
                    worldY + gamePanel.tileSize > gamePanel.player.worldY - gamePanel.player.screenY &&
                    worldY - gamePanel.tileSize < gamePanel.player.worldY + gamePanel.player.screenY) {

                if (tile[tileNum] != null && tile[tileNum].image != null) {
                    g2.drawImage(tile[tileNum].image, screenX, screenY, null);
                }
            }

            worldCol++;

            if (worldCol == gamePanel.maxWorldCols) {
                worldCol = 0;
                worldRow++;
            }
        }

        if (drawPath == true) {
            g2.setColor(new Color(255, 0, 0, 70));

            for (int i = 0; i < gamePanel.pathfinder.pathList.size(); i++) {
                int worldX = gamePanel.pathfinder.pathList.get(i).col * gamePanel.tileSize;
                int worldY = gamePanel.pathfinder.pathList.get(i).row * gamePanel.tileSize;
                int screenX = worldX - gamePanel.player.worldX + gamePanel.screenWidth / 2;
                int screenY = worldY - gamePanel.player.worldY + gamePanel.screenHeight / 2;

                g2.fillRect(screenX, screenY, gamePanel.tileSize, gamePanel.tileSize);
            }
        }

        // Minimap is drawn by the overlay (GamePanel) when desired.

    }
}
