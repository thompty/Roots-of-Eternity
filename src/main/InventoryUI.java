package src.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import src.main.GamePanel;
import src.entity.Entity;

public class InventoryUI {
    GamePanel gamePanel;
    BufferedImage uiImage, hoverTab1, hoverTab2, hoverTab3, hoverTab4, hoverTab5, hoverTab6;
    BufferedImage charImage;

    boolean isDragging = false;
    BufferedImage draggedIcon = null;
    int draggedIndex = -1;
    int mouseX, mouseY; // current mouse position for drag icon
    int hoveredSlot = -1;

    int scale = 3;
    int slotX;
    int slotY;

    // Drag state
    boolean draggedFromEquip = false;
    int draggedEquipSlot = -1; // 0 weapon, 1 shield
    final int SLOT_COUNT = 36;
    final int SLOT_SIZE = 16;
    final int START_X = 117 * scale;
    final int START_Y = 12 * scale;

    boolean isOpen = false;

    final int COLUMNS = 6;
    final int ROWS = 6;
    BufferedImage[][] itemIcons = new BufferedImage[ROWS][COLUMNS];
    int[][] invMap = new int[ROWS][COLUMNS]; // maps grid cell -> player.inventory index, -1 if empty
    // Equipment slots: 0=weapon (sword/axe), 1=shield
    private static final int EQUIP_WEAPON = 0;
    private static final int EQUIP_SHIELD = 1;
    private final BufferedImage[] equipIcons = new BufferedImage[2];

    public InventoryUI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        // Get InventoryUI Image

        // Setup UI Images
        uiImage = setup("/res/ui/Inventory/Inventory_UI.png");
        hoverTab1 = setup("/res/ui/Inventory/Inventory_UI-1.png");
        hoverTab2 = setup("/res/ui/Inventory/Inventory_UI-2.png");
        hoverTab3 = setup("/res/ui/Inventory/Inventory_UI-3.png");
        hoverTab4 = setup("/res/ui/Inventory/Inventory_UI-4.png");
        hoverTab5 = setup("/res/ui/Inventory/Inventory_UI-5.png");
        hoverTab6 = setup("/res/ui/Inventory/Inventory_UI-6.png");

        // Setup Character Sprite Image
        charImage = setup("/res/player/BoneMender/Bone_Weaver-1.png");
    }

    public void toggle() {
        isOpen = !isOpen;
        if (isOpen) {
            populateFromPlayer();
        }
    }

    // Fill the grid from the player's current inventory using each item's icon
    private void populateFromPlayer() {
        // Clear existing icons and mapping
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                itemIcons[r][c] = null;
                invMap[r][c] = -1;
            }
        }
        if (gamePanel == null || gamePanel.player == null)
            return;

        // Populate grid with UNEQUIPPED items only
        int gridIdx = 0;
        for (int invIdx = 0; invIdx < gamePanel.player.inventory.size(); invIdx++) {
            Entity e = gamePanel.player.inventory.get(invIdx);
            if (e == null || e.down1 == null)
                continue;
            // Skip currently equipped items so they show only in equip slots
            if (e == gamePanel.player.currentWeapon || e == gamePanel.player.currentShield)
                continue;

            int row = gridIdx / COLUMNS;
            int col = gridIdx % COLUMNS;
            if (row >= ROWS)
                break;
            itemIcons[row][col] = e.down1; // draw() will scale to SLOT_SIZE
            invMap[row][col] = invIdx; // remember original inventory index
            gridIdx++;
        }

        // Populate equipment icons from player's equipped items
        try {
            if (gamePanel.player.currentWeapon != null) {
                equipIcons[EQUIP_WEAPON] = gamePanel.player.currentWeapon.down1;
            } else {
                equipIcons[EQUIP_WEAPON] = null;
            }
            if (gamePanel.player.currentShield != null) {
                equipIcons[EQUIP_SHIELD] = gamePanel.player.currentShield.down1;
            } else {
                equipIcons[EQUIP_SHIELD] = null;
            }
        } catch (Exception ignore) {
        }
    }

    public void checkHover(int mouseX, int mouseY) {
        hoveredSlot = -1;
        slotX = -1;
        slotY = -1;

        // Use the same x,y origin that draw() uses (handle missing background too)
        int baseX;
        int baseY;
        if (uiImage != null) {
            baseX = (gamePanel.screenWidth - (uiImage.getWidth() * scale)) / 2;
            baseY = (gamePanel.screenHeight - (uiImage.getHeight() * scale)) / 2;
        } else {
            baseX = gamePanel.screenWidth / 2 - 200;
            baseY = gamePanel.screenHeight / 2 - 150;
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int x = baseX + START_X + (col * (SLOT_SIZE * scale + (3 * scale)));
                int y = baseY + START_Y + (row * (SLOT_SIZE * scale + (3 * scale)));
                int size = SLOT_SIZE * scale;

                if (mouseX >= x && mouseX <= x + size &&
                        mouseY >= y && mouseY <= y + size) {
                    hoveredSlot = row * COLUMNS + col;
                    slotX = x;
                    slotY = y;
                    return;
                }
            }
        }
    }

    public void mousePressed(int x, int y) {
        checkHover(x, y);
        mouseX = x;
        mouseY = y;

        // First, check if pressing on equip slots to start dragging to unequip
        int equipUnderMouse = hitTestEquipSlots(x, y);
        if (equipUnderMouse != -1 && equipIcons[equipUnderMouse] != null) {
            isDragging = true;
            draggedFromEquip = true;
            draggedEquipSlot = equipUnderMouse;
            draggedIcon = equipIcons[equipUnderMouse];
            return;
        }

        // Otherwise, start dragging from grid if any
        if (hoveredSlot != -1) {
            int row = hoveredSlot / COLUMNS;
            int col = hoveredSlot % COLUMNS;
            BufferedImage icon = itemIcons[row][col];

            if (icon != null) {
                // Start dragging
                isDragging = true;
                draggedIcon = icon;
                draggedIndex = hoveredSlot; // grid slot index
                draggedFromEquip = false;
                draggedEquipSlot = -1;
            }
        }
    }

    public void mouseDragged(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    public void mouseReleased(int x, int y) {
        if (isDragging) {
            checkHover(x, y);
            int targetSlot = hoveredSlot;

            if (draggedFromEquip) {
                // Dragging an equipped item to the grid to unequip
                if (targetSlot != -1) {
                    int row = targetSlot / COLUMNS;
                    int col = targetSlot % COLUMNS;
                    if (itemIcons[row][col] == null) {
                        // Append the equipped item back into inventory, map to the dropped cell
                        Entity equipped = (draggedEquipSlot == EQUIP_WEAPON) ? gamePanel.player.currentWeapon
                                : gamePanel.player.currentShield;
                        if (equipped != null) {
                            gamePanel.player.inventory.add(equipped);
                            int newIndex = gamePanel.player.inventory.size() - 1;
                            itemIcons[row][col] = draggedIcon;
                            invMap[row][col] = newIndex;

                            // Unequip from player
                            if (draggedEquipSlot == EQUIP_WEAPON) {
                                gamePanel.player.currentWeapon = null;
                                gamePanel.player.attack = gamePanel.player.getAttack();
                                gamePanel.player.getPlayerAttackImage();
                                equipIcons[EQUIP_WEAPON] = null;
                            } else if (draggedEquipSlot == EQUIP_SHIELD) {
                                gamePanel.player.currentShield = null;
                                gamePanel.player.defense = gamePanel.player.getDefense();
                                equipIcons[EQUIP_SHIELD] = null;
                            }
                        }
                    }
                }
            } else {
                // Dragging from the grid
                if (targetSlot != -1 && targetSlot != draggedIndex) {
                    int srcRow = draggedIndex / COLUMNS;
                    int srcCol = draggedIndex % COLUMNS;
                    int destRow = targetSlot / COLUMNS;
                    int destCol = targetSlot % COLUMNS;

                    // Swap icons
                    BufferedImage tempIcon = itemIcons[destRow][destCol];
                    itemIcons[destRow][destCol] = itemIcons[srcRow][srcCol];
                    itemIcons[srcRow][srcCol] = tempIcon;

                    // Swap mapping
                    int tempMap = invMap[destRow][destCol];
                    invMap[destRow][destCol] = invMap[srcRow][srcCol];
                    invMap[srcRow][srcCol] = tempMap;
                } else {
                    // Try to equip if dropped over an equip slot
                    int equipTarget = hitTestEquipSlots(x, y);
                    if (equipTarget != -1 && draggedIndex != -1) {
                        int row = draggedIndex / COLUMNS;
                        int col = draggedIndex % COLUMNS;
                        int invIdx = invMap[row][col];
                        if (invIdx != -1) {
                            boolean equipped = applyEquipFromInventoryIndex(equipTarget, invIdx, row, col);
                            if (equipped) {
                                // applyEquipFromInventoryIndex handles grid updates (swap or clear)
                            }
                        }
                    }
                }
            }
        }

        // Reset drag state
        isDragging = false;
        draggedIcon = null;
        draggedIndex = -1;
        draggedFromEquip = false;
        draggedEquipSlot = -1;
    }

    // Returns equip slot index or -1 if not over a slot
    private int hitTestEquipSlots(int mouseX, int mouseY) {
        // Compute the same origin used in draw()/checkHover
        int x;
        int y;
        if (uiImage != null) {
            x = (gamePanel.screenWidth - (uiImage.getWidth() * scale)) / 2;
            y = (gamePanel.screenHeight - (uiImage.getHeight() * scale)) / 2;
        } else {
            x = gamePanel.screenWidth / 2 - 200;
            y = gamePanel.screenHeight / 2 - 150;
        }

        int eqSize = SLOT_SIZE * scale;
        int eqY = y + 91 * scale;
        int eqXWeapon = x + 13 * scale;
        int eqXShield = eqXWeapon + SLOT_SIZE * scale;

        if (mouseX >= eqXWeapon && mouseX <= eqXWeapon + eqSize && mouseY >= eqY && mouseY <= eqY + eqSize) {
            return EQUIP_WEAPON;
        }
        if (mouseX >= eqXShield && mouseX <= eqXShield + eqSize && mouseY >= eqY && mouseY <= eqY + eqSize) {
            return EQUIP_SHIELD;
        }
        return -1;
    }

    // Apply equipping logic given an inventory index and target equip slot
    private boolean applyEquipFromInventoryIndex(int equipSlot, int inventoryIndex, int srcRow, int srcCol) {
        if (gamePanel == null || gamePanel.player == null)
            return false;
        if (inventoryIndex < 0 || inventoryIndex >= gamePanel.player.inventory.size())
            return false;
        Entity item = gamePanel.player.inventory.get(inventoryIndex);
        if (item == null)
            return false;

        // Determine currently equipped item for this slot
        Entity oldEquipped = null;
        boolean validType = false;
        if (equipSlot == EQUIP_WEAPON) {
            validType = (item.type == item.type_sword || item.type == item.type_axe);
            oldEquipped = gamePanel.player.currentWeapon;
        } else if (equipSlot == EQUIP_SHIELD) {
            validType = (item.type == item.type_shield);
            oldEquipped = gamePanel.player.currentShield;
        }
        if (!validType)
            return false;

        // Remove the new item from inventory first
        gamePanel.player.inventory.remove(inventoryIndex);

        // Adjust invMap indices after removal
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                if (invMap[r][c] >= 0 && invMap[r][c] > inventoryIndex) {
                    invMap[r][c] -= 1;
                }
            }
        }

        // If there was an old equipped item, add it to inventory and place it into the
        // source grid cell
        if (oldEquipped != null) {
            gamePanel.player.inventory.add(oldEquipped);
            int idxOld = gamePanel.player.inventory.size() - 1;
            itemIcons[srcRow][srcCol] = oldEquipped.down1;
            invMap[srcRow][srcCol] = idxOld;
        } else {
            // No previously equipped item: clear the source grid cell
            itemIcons[srcRow][srcCol] = null;
            invMap[srcRow][srcCol] = -1;
        }

        // Equip the new item and update stats/icons
        if (equipSlot == EQUIP_WEAPON) {
            gamePanel.player.currentWeapon = item;
            gamePanel.player.attack = gamePanel.player.getAttack();
            gamePanel.player.getPlayerAttackImage();
            equipIcons[EQUIP_WEAPON] = item.down1;
        } else if (equipSlot == EQUIP_SHIELD) {
            gamePanel.player.currentShield = item;
            gamePanel.player.defense = gamePanel.player.getDefense();
            equipIcons[EQUIP_SHIELD] = item.down1;
        }

        return true;
    }

    // Find the index of an entity in the player's inventory by reference
    private int findInventoryIndexOfEntity(Entity target) {
        if (target == null || gamePanel == null || gamePanel.player == null)
            return -1;
        for (int i = 0; i < gamePanel.player.inventory.size(); i++) {
            if (gamePanel.player.inventory.get(i) == target)
                return i;
        }
        return -1;
    }

    public BufferedImage setup(String imagePath) {
        BufferedImage image = null;
        draggedFromEquip = false;
        draggedEquipSlot = -1;
        try {
            java.io.InputStream is = getClass().getResourceAsStream(imagePath);
            if (is != null) {
                image = ImageIO.read(is);
                if (image == null) {
                    throw new IOException("Failed to read image: " + imagePath);
                }
            } else {
                // Missing UI image is acceptable; we'll draw a fallback panel in draw()
                System.out.println("[InventoryUI] UI image not found at " + imagePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public void draw(Graphics2D g2) {
        if (!isOpen) {
            return;
        }

        int x;
        int y;
        if (uiImage != null) {
            x = (gamePanel.screenWidth - (uiImage.getWidth()) * scale) / 2;
            y = (gamePanel.screenHeight - (uiImage.getHeight()) * scale) / 2;
            g2.drawImage(uiImage, x, y, uiImage.getWidth() * scale, uiImage.getHeight() * scale, null);
        } else {
            // Fallback: draw a simple panel if the background image is missing
            x = gamePanel.screenWidth / 2 - 200;
            y = gamePanel.screenHeight / 2 - 150;
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(x, y, 400, 300, 16, 16);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x, y, 400, 300, 16, 16);
        }

        // Draw Mouse
        if (hoveredSlot != -1) {
            g2.setColor(new Color(255, 255, 0, 100));
            g2.fillRect(slotX, slotY, SLOT_SIZE * scale, SLOT_SIZE * scale);

            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(slotX, slotY, SLOT_SIZE * scale, SLOT_SIZE * scale);
        }

        // Draw Character Sprite
        int charX;
        int charY;
        if (charImage != null) {
            charX = (int) ((gamePanel.screenWidth - (charImage.getWidth() * scale)) / (4.35F));
            charY = (int) ((gamePanel.screenHeight - (charImage.getHeight() * scale)) / (2.75F));
            g2.drawImage(charImage, charX, charY, charImage.getWidth() * scale, charImage.getHeight() * scale, null);
        }

        // Draw Item Slots & Icons
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                // Draw slots adjust with (SLOT_SIZE + int)
                int slotX = x + START_X + (col * (SLOT_SIZE * scale + (3 * scale)));
                int slotY = y + START_Y + (row * (SLOT_SIZE * scale + (3 * scale)));

                // Highlight Box
                // g2.setColor(Color.DARK_GRAY);
                // g2.drawRect(slotX, slotY, SLOT_SIZE * scale, SLOT_SIZE * scale);

                // Item Icon draw
                if (itemIcons[row][col] != null) {
                    // Hide the original icon while dragging it
                    if (!(isDragging && draggedIndex == row * COLUMNS + col)) {
                        g2.drawImage(itemIcons[row][col], slotX, slotY, SLOT_SIZE * scale, SLOT_SIZE * scale, null);
                    }
                }

            }
        }

        // Draw equipment slots (left column)
        int eqSize = SLOT_SIZE * scale;
        int eqY = y + 91 * scale;
        int eqXWeapon = x + 13 * scale;
        int eqXShield = eqXWeapon + SLOT_SIZE * scale;

        // Slot backgrounds
        // g2.setColor(new Color(255, 255, 255, 40));
        // g2.fillRect(eqX, eqYWeapon, eqSize, eqSize);
        // g2.fillRect(eqX, eqYShield, eqSize, eqSize);
        // g2.setColor(new Color(200, 200, 200));
        // g2.drawRect(eqX, eqYWeapon, eqSize, eqSize);
        // g2.drawRect(eqX, eqYShield, eqSize, eqSize);

        // Icons
        if (equipIcons[EQUIP_WEAPON] != null) {
            if (!(isDragging && draggedFromEquip && draggedEquipSlot == EQUIP_WEAPON)) {
                g2.drawImage(equipIcons[EQUIP_WEAPON], eqXWeapon, eqY, eqSize, eqSize, null);
            }
        }
        if (equipIcons[EQUIP_SHIELD] != null) {
            if (!(isDragging && draggedFromEquip && draggedEquipSlot == EQUIP_SHIELD)) {
                g2.drawImage(equipIcons[EQUIP_SHIELD], eqXShield, eqY, eqSize, eqSize, null);
            }
        }

        if (isDragging && draggedIcon != null) {
            int iconSize = SLOT_SIZE * scale;
            // Center the icon around the mouse
            int drawX = mouseX - iconSize / 2;
            int drawY = mouseY - iconSize / 2;
            g2.drawImage(draggedIcon, drawX, drawY, iconSize, iconSize, null);

            // Optional: draw a slight transparent shadow for visibility
            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawRect(drawX, drawY, iconSize, iconSize);
        }
    }
}
