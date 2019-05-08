package gen.map;

import gen.map.export.MapLayerExport;
import gen.map.surface.SurfaceLayerBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

//Job: Understands basic MapLayer operations
@SuppressWarnings("WeakerAccess")
public abstract class MapLayer {
    private final int id;
    public final int height;
    public final int width;
    public final MapTile[][] tiles;
    public final MapTile[][] extraTiles;
    protected boolean hasGenerated = false;

    public long seed;

    public MapLayer(int width, int height){
        this.id = new Random().nextInt();
        this.width = width;
        this.height = height;
        this.tiles = new MapTile[width][height];
        this.extraTiles = new MapTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = MapTile.EMPTY;
                extraTiles[i][j] = MapTile.EMPTY;
            }
        }
        this.seed = new Random().nextLong();
    }

    public abstract void generate();

    @SuppressWarnings("unused")
    public void printRender() {
        System.out.println("----------------");

        String[][] render = MapLayerExport.exportAsStringArray(this);
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
            if (x + xOffset[i] >= 0 && x + xOffset[i] < width
                    && y + yOffset[i] >= 0 && y + yOffset[i] < height) {
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

    private MapTile[][] getTilesArray(MapTile[][] array) {
        MapTile[][] copy = new MapTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                copy[i][j] = array[i][j];
            }
        }
        return copy;
    }

    public MapTile[][] getTilesArray() {
        return getTilesArray(tiles);
    }

    public MapTile[][] getExtraTilesArray() {
        return getTilesArray(extraTiles);
    }

    private ArrayList<Point> getTiles(MapTile[][] array, MapTile type) {
        ArrayList<Point> foundTiles = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                if (array[i][j] == type) {
                    foundTiles.add(new Point(i, j));
                }
            }
        }
        return foundTiles;
    }

    public ArrayList<Point> getTiles(MapTile type) {
        return getTiles(tiles, type);
    }

    public ArrayList<Point> getExtraTiles(MapTile type) {
        return getTiles(extraTiles, type);
    }

    private ArrayList<ArrayList<Point>> getTilesGroups(MapTile[][] array, MapTile type) {
        ArrayList<ArrayList<Point>> siteSpots = new ArrayList<>();
        boolean[][] visited = new boolean[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                if (array[i][j] == type && !visited[i][j]) {
                    ArrayList<Point> stack = new ArrayList<>(), currSite = new ArrayList<>();
                    stack.add(new Point(i, j));
                    while (stack.size() > 0) {
                        Point curr = stack.remove(0);
                        if (visited[curr.x][curr.y]) {
                            continue;
                        }
                        currSite.add(curr);
                        visited[curr.x][curr.y] = true;

                        for (Point p : getOrthoAdjacent(curr.x, curr.y)) {
                            if (array[p.x][p.y] == type && !visited[p.x][p.y]) {
                                stack.add(p);
                            }
                        }
                    }

                    siteSpots.add(currSite);
                }
            }
        }
        return siteSpots;
    }

    public ArrayList<ArrayList<Point>> getTilesGroups(MapTile type) {
        return getTilesGroups(tiles, type);
    }

    public ArrayList<ArrayList<Point>> getExtraTilesGroups(MapTile type) {
        return getTilesGroups(extraTiles, type);
    }

    public ArrayList<Point> getPassableTiles() {
        ArrayList<Point> open = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (tiles[i][j].passable) {
                    open.add(new Point(i, j));
                }
            }
        }
        return open;
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

    public abstract SurfaceLayerBuilder getBuilder();
}
