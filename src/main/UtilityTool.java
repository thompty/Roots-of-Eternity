package src.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class UtilityTool {
    //Scales an image to specified width and height
    public BufferedImage scaleImage(BufferedImage original, int width, int height) {
        if (original == null) {
            throw new IllegalArgumentException("Original image is null");
        }

        //Creates new scaled image and draw original into it
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();

        return scaledImage;
    }
}
