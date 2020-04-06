package gen.export;

import gen.map.MapLayer;
import gen.map.MapTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//Job: Understands how to export MapLayers in various formats
public class MapLayerExport {

    public static String[][] exportAsStringArray(MapLayer map) {
        String[][] render = new String[map.width][map.height];
        for (int i = 0; i < map.width; i++) {
            for (int j = 0; j < map.height; j++) {
                if (map.tiles[i][j] == null) {
                    throw new RuntimeException("Encountered null tile!");
                }
                render[i][j] = map.tiles[i][j].renderString;
            }
        }
        return render;
    }

    public static void exportAsImage(MapLayer map, String name, int tileSize, int gridSize) {
        BufferedImage img = new BufferedImage(map.width * tileSize, map.height * tileSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, map.width * tileSize, map.height * tileSize);
        for (int i = 0; i < map.width; i++) {
            for (int j = 0; j < map.height; j++) {
                Color tileColor = map.tiles[i][j].color;
                g2d.setColor(tileColor);
                g2d.fillRect(j * tileSize, i * tileSize, tileSize, tileSize);

                if (map.specialTiles[i][j] != MapTile.EMPTY) {
                    tileColor = map.specialTiles[i][j].color;
                    g2d.setColor(tileColor);
                    g2d.fillRect(j * tileSize + (int) Math.round(tileSize/12d), i * tileSize + (int) Math.round(tileSize/12d),
                            tileSize*5/6, tileSize*5/6);
                }
            }
        }

        gridSize = tileSize * gridSize;
        if (gridSize > 0) {
            for (int i = 0; i < map.width * tileSize; i += gridSize) {
                for (int j = 0; j < map.height * tileSize; j += gridSize) {
                    g2d.setColor(Color.ORANGE);
                    g2d.drawRect(j, i, gridSize, gridSize);
                }
            }
        }

        File imgfile = new File(name + "_Export.png");
        try {
            ImageIO.write(img, "png", imgfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
