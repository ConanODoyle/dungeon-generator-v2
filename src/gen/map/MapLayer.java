package gen.map;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

//Job: Understands basic MapLayer operations
public abstract class MapLayer {
    public static final int PIXELSIZE = 10;
    private final int id;
    protected final int height;
    protected final int width;
    protected final MapTile[][] tiles;
    protected boolean hasGenerated = false;

    public long seed;

    public MapLayer(int width, int height){
        this.id = new Random().nextInt();
        this.width = width;
        this.height = height;
        tiles = new MapTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = MapTile.EMPTY;
            }
        }
        this.seed = new Random().nextLong();
    }

    public abstract void generate();

    public String[][] render() {
        String[][] render = new String[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (tiles[i][j] == null) {
                    throw new RuntimeException("Encountered null tile!");
                }
                render[i][j] = tiles[i][j].renderString;
            }
        }
        return render;
    }

    public void exportAsImage() {
        BufferedImage img = new BufferedImage(this.width * PIXELSIZE, this.height * PIXELSIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, this.width * PIXELSIZE, this.height * PIXELSIZE);
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                Color tileColor = tiles[i][j].color;
                g2d.setColor(tileColor);
                g2d.fillRect(j * PIXELSIZE, i * PIXELSIZE, PIXELSIZE, PIXELSIZE);
                g2d.setColor(Color.ORANGE);
                g2d.drawRect(j * PIXELSIZE, i * PIXELSIZE, PIXELSIZE, PIXELSIZE);
            }
        }

        File imgfile = new File(this.getName() + "_Export.png");
        try {
            ImageIO.write(img, "png", imgfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract String getName();

    public void printRender() {
        System.out.println("----------------");

        String[][] render = this.render();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.print(render[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("----------------");
    }

    protected Point[] getOrthoAdjacent(int x, int y) {
        Point[] search = new Point[4];

        int[] xOffset = {0, 1, 0, -1};
        int[] yOffset = {1, 0, -1, 0};
        int curr = 0;

        for (int i = 0; i < 4; i++) {
            if (x + xOffset[i] > 0 && x + xOffset[i] < this.width
                    && y + yOffset[i] > 0 && y + yOffset[i] < this.height) {
                search[curr++] = new Point(x + xOffset[i], y + yOffset[i]);
            }
        }

        //remove null fields
        Point[] result = new Point[curr];
        System.arraycopy(search, 0, result, 0, result.length);

        return result;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapLayer)) {
            return false;
        } else {
            MapLayer l = (MapLayer) o;

            return Arrays.deepEquals(this.tiles, l.tiles);
        }
    }

    @Override
    public int hashCode() {
        return id % 16;
    }

    public abstract boolean validateGeneration();

    protected int distanceToClosestTile(int x, int y, MapTile tileType) {
        int[] xmod = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] ymod = {1, 1, 0, -1, -1, -1, 0, 1};
        ArrayList<Point> checked = new ArrayList<>();

        int currindex = 0;
        boolean found = false;
        Point closest = null;
        checked.add(new Point(x, y));
        while (!found) {
            Point curr = checked.get(currindex);
            currindex++;
            for (int mod = 0; mod < xmod.length; mod++) {
                int xadj = curr.x + xmod[mod];
                int yadj = curr.y + ymod[mod];
                if (xadj > 0 && xadj < this.width && yadj > 0 && yadj < this.height
                        && this.tiles[xadj][yadj] != tileType) {
                    if (!checked.contains(new Point(xadj, yadj)))
                        checked.add(new Point(xadj, yadj));
                } else {
                    found = true;
                    closest = new Point(xadj, yadj);
                    break;
                }
            }
        }
        int foundx = closest.x, foundy = closest.y;
        return Math.abs(foundx - x) + Math.abs(foundy - y);
    }

    protected int getTotalAccessibleArea() {
        int total = 0;
        for (int i = 0; i < this.width; i++)
            for (int j = 0; j < this.height; j++)
                if (tiles[i][j].passable) total++;
        return total;
    }
}
