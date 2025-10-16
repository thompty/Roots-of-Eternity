package src.tiles;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Tile {

    public BufferedImage image;
    public boolean collision = false;
    public Rectangle hitBox = new Rectangle(0, 0, 0, 0);
}
