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
@SuppressWarnings("WeakerAccess")
public abstract class MapLayer {
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
        this.tiles = new MapTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = MapTile.EMPTY;
            }
        }
        this.seed = new Random().nextLong();
    }

    public abstract void generate();

    @SuppressWarnings("unused")
    public void printRender() {
        System.out.println("----------------");

        String[][] render = MapExport.exportAsString(this);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.print(render[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("----------------");
    }

    public ArrayList<Point> getOrthoAdjacent(int x, int y) {
        ArrayList<Point> result = new ArrayList<>();

        int[] xOffset = {0, 1, 0, -1};
        int[] yOffset = {1, 0, -1, 0};

        for (int i = 0; i < xOffset.length; i++) {
            if (x + xOffset[i] > 0 && x + xOffset[i] < width
                    && y + yOffset[i] > 0 && y + yOffset[i] < height) {
                result.add(new Point(x + xOffset[i], y + yOffset[i]));
            }
        }

        return result;
    }

    public ArrayList<Point> getAdjacent(int x, int y) {
        ArrayList<Point> result = new ArrayList<>();

        int[] xOffset = {1, 1,  -1, -1};
        int[] yOffset = {1, -1, -1,  1};

        for (int i = 0; i < xOffset.length; i++) {
            if (x + xOffset[i] > 0 && x + xOffset[i] < width
                    && y + yOffset[i] > 0 && y + yOffset[i] < height) {
                result.add(new Point(x + xOffset[i], y + yOffset[i]));
            }
        }
        result.addAll(getOrthoAdjacent(x, y));
        return result;
    }

    public ArrayList<Point> getTilesByType(MapTile t) {
        ArrayList<Point> result = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (tiles[i][j] == t) {
                    result.add(new Point(i, j));
                }
            }
        }

        return result;
    }

    public void setSeed(long seed) {
        seed = seed;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapLayer)) {
            return false;
        } else {
            MapLayer l = (MapLayer) o;

            return Arrays.deepEquals(tiles, l.tiles);
        }
    }

    @Override
    public int hashCode() {
        return id % 16;
    }

    public abstract boolean validateGeneration();

    @SuppressWarnings("unused")
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
                if (xadj > 0 && xadj < width && yadj > 0 && yadj < height
                        && tiles[xadj][yadj] != tileType) {
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
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (tiles[i][j].passable) total++;
        return total;
    }
}
