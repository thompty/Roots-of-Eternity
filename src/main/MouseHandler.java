package src.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseHandler extends MouseAdapter implements MouseMotionListener {

    GamePanel gp;

    public MouseHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gp.mousePressed = true;

        double sx = (gp.screenWidthFS > 0) ? (gp.screenWidth / (double) gp.screenWidthFS) : 1.0;
        double sy = (gp.screenHeightFS > 0) ? (gp.screenHeight / (double) gp.screenHeightFS) : 1.0;
        int mx = (int) Math.round(e.getX() * sx);
        int my = (int) Math.round(e.getY() * sy);

        if (gp.inventoryUI != null && gp.inventoryUI.isOpen) {
            gp.inventoryUI.mousePressed(mx, my);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gp.mousePressed = false;

        double sx = (gp.screenWidthFS > 0) ? (gp.screenWidth / (double) gp.screenWidthFS) : 1.0;
        double sy = (gp.screenHeightFS > 0) ? (gp.screenHeight / (double) gp.screenHeightFS) : 1.0;
        int mx = (int) Math.round(e.getX() * sx);
        int my = (int) Math.round(e.getY() * sy);

        if (gp.inventoryUI != null && gp.inventoryUI.isOpen) {
            gp.inventoryUI.mouseReleased(mx, my);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Map physical mouse coords to the game's logical backbuffer coords.
        // The game draws a fixed-size backbuffer (screenWidth x screenHeight)
        // and then scales it to the actual component size (screenWidthFS x
        // screenHeightFS).
        // To make hit tests line up, convert the mouse coords back to logical space.
        double sx = (gp.screenWidthFS > 0) ? (gp.screenWidth / (double) gp.screenWidthFS) : 1.0;
        double sy = (gp.screenHeightFS > 0) ? (gp.screenHeight / (double) gp.screenHeightFS) : 1.0;
        gp.mouseX = (int) Math.round(e.getX() * sx);
        gp.mouseY = (int) Math.round(e.getY() * sy);

        if (gp.inventoryUI != null && gp.inventoryUI.isOpen) {
            gp.inventoryUI.checkHover(gp.mouseX, gp.mouseY);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        double sx = (gp.screenWidthFS > 0) ? (gp.screenWidth / (double) gp.screenWidthFS) : 1.0;
        double sy = (gp.screenHeightFS > 0) ? (gp.screenHeight / (double) gp.screenHeightFS) : 1.0;
        gp.mouseX = (int) Math.round(e.getX() * sx);
        gp.mouseY = (int) Math.round(e.getY() * sy);
        if (gp.inventoryUI != null && gp.inventoryUI.isOpen) {
            gp.inventoryUI.mouseDragged(gp.mouseX, gp.mouseY);
        }
    }
}
