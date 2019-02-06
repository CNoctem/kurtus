package bla.konishy.kurtus.wss;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BG4Nex {

    public static void main(String[] args) throws IOException {
        BufferedImage bi = new BufferedImage(1440, 2560, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,1440, 2560);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 10; i < 140; i+=10) {
            g.fillRect(0,0, 1440, i);
            ImageIO.write(bi, "JPG", new File("/home/ekovger/tmp/nexbg" + i + ".jpg"));
        }
    }


}
