package gen.map.surface;

import gen.map.MapTile;
import gen.map.export.BlsBrick;
import gen.map.export.MapLayerBuilder;
import gen.map.lib.GridUtils;
import gen.map.parser.TileBuild;
import gen.map.parser.TileSearch;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//Job: Understands how to convert a SurfaceLayer into formatted .bls strings
public class SurfaceLayerBuilder extends MapLayerBuilder {

    //Job: Understands a 4-sided shape on a grid
    private class Rectangle {
        private Point leftCorner; //upper-left corner
        private int width;
        private int height;

        private Rectangle(int width, int height, Point corner) {
            this.width = width;
            this.height = height;
            this.leftCorner = new Point(corner);
        }
    }

    private Point offset;
    private SurfaceLayer layer;
    private MapTile[][] copy;
    private static final double STARTING_HEIGHT = 100;
    private static final String[] TILESETS = {
            "ForestRoof16x", "ForestRoof32x", "ForestRoof48x", "ForestRoof64x",
            "CliffRoof16x", "CliffRoof32x", "CliffRoof48x", "CliffRoof64x",
            "ForestPath16x", "ForestPath32x", "ForestPath48x", "ForestPath64x",
            "ForestFloor16x", "ForestFloor32x", "ForestFloor48x", "ForestFloor64x",

            "ForestWall1", "ForestWall2", "ForestWall3", "CliffWall", "TallCliffWall", "TallCliffRoof16x",
    };
    private static final String[] DETAILING = {
            "TallPineTree", "ShortPineTree", "PineTree", "Petal", "Grass", "Flower",
    };
    public static final String[] SPECIAL_TILES = {
            "Town","Glen","Cave",
    };

    private ArrayList<BlsBrick> bricks = new ArrayList<>();

    SurfaceLayerBuilder(SurfaceLayer layer) {
        this.layer = layer;
        this.copy = layer.getTiles();
        this.offset = new Point(0, 0);
    }

    private HashMap<String, TileBuild> loadTilesets() {
        HashMap<String, TileBuild> tileLibrary = new HashMap<>();
        TileSearch search = new TileSearch("resources/tilesets.bls");
        for (String s : TILESETS) {
            tileLibrary.put(s, search.findTile(s));
        }
        for (String s : DETAILING) {
            tileLibrary.put(s, search.findTile(s));
        }
        return tileLibrary;
    }

    public void generateBuild() {
        //load the tilesets
        HashMap<String, TileBuild> tileLibrary = loadTilesets();

        //optimize coverages
        ArrayList<Rectangle> treeCover = calculateOptimumCover(SurfaceTile.FOREST);
        ArrayList<Rectangle> cliffCover = calculateOptimumCover(SurfaceTile.CLIFF);
        ArrayList<Rectangle> pathCover = calculateOptimumCover(SurfaceTile.FORESTPATH);
        ArrayList<Rectangle> floorCover = calculateOptimumCover(SurfaceTile.FORESTFLOOR);

        //plant the tiles
        plantOptimizedTiles(treeCover, new String[]{
                TILESETS[0], TILESETS[1], TILESETS[2], TILESETS[3]}, tileLibrary);
        plantOptimizedTiles(cliffCover, new String[]{
                TILESETS[4], TILESETS[5], TILESETS[6], TILESETS[7]}, tileLibrary);
        plantOptimizedTiles(pathCover, new String[]{
                TILESETS[8], TILESETS[9], TILESETS[10], TILESETS[11]}, tileLibrary);
        plantOptimizedTiles(floorCover, new String[]{
                TILESETS[12], TILESETS[13], TILESETS[14], TILESETS[15]}, tileLibrary);

        //plant border cliff wall
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy.length; j++) {
                if (copy[i][j] == SurfaceTile.TALLCLIFF) {
                    ArrayList<BlsBrick> tallCliffTile = tileLibrary.get("TallCliffRoof16x").getBricks();
                    for (BlsBrick b : tallCliffTile) {
                        b.x += i * 8 + 4;
                        b.y += j * 8 + 4;
                    }
                    bricks.addAll(tallCliffTile);
                }
            }
        }

        //plant walls
        plantAllWalls(tileLibrary);

        //generate detailing
        plantTrees(tileLibrary);
        plantGrass(tileLibrary);
        plantFlowers(tileLibrary);
    }

    private void plantFlowers(HashMap<String, TileBuild> tileLibrary) {
        Random rand = new Random(new Random(layer.seed).nextLong()); //don't want the random values being same as plantTrees
        int flowerCount;
        String[] flowerTypes = {"Flower"};
        double xOffset, yOffset;
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                if (copy[i][j] == SurfaceTile.FORESTFLOOR) {
                    if (rand.nextDouble() > 0.9) {
                        flowerCount = rand.nextInt(8);
                    } else {
                        flowerCount = 0;
                    }
                    for (int k = 0; k < flowerCount; k++) {
                        xOffset = i * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                        yOffset = j * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

                        TileBuild flowerBuild = tileLibrary.get(flowerTypes[rand.nextInt(flowerTypes.length)]);
                        ArrayList<BlsBrick> currTileBricks = flowerBuild.getRotatedBricks(rand.nextInt(4));
                        for (BlsBrick b : currTileBricks) {
                            b.x += xOffset;
                            b.y += yOffset;
                        }
                        bricks.addAll(currTileBricks);
                    }
                }
            }
        }
    }

    private void plantGrass(HashMap<String, TileBuild> tileLibrary) {
        Random rand = new Random(new Random(layer.seed).nextLong()); //don't want the random values being same as plantTrees
        int grassCount;
        String[] grassTypes = {"Petal", "Grass"};
        double xOffset, yOffset;
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                if (copy[i][j] == SurfaceTile.FORESTPATH || copy[i][j] == SurfaceTile.FORESTFLOOR) {
                    if (copy[i][j] == SurfaceTile.FORESTPATH) {
                        grassCount = rand.nextInt(2) + 1;
                    } else if (copy[i][j] == SurfaceTile.FORESTFLOOR && rand.nextDouble() > 0.7) {
                        grassCount = rand.nextInt(3) + 1;
                    } else {
                        grassCount = 1;
                    }
                    for (int k = 0; k < grassCount; k++) {
                        xOffset = i * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                        yOffset = j * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

                        TileBuild grassBuild = tileLibrary.get(grassTypes[rand.nextInt(grassTypes.length)]);
                        ArrayList<BlsBrick> currTileBricks = grassBuild.getRotatedBricks(rand.nextInt(4));
                        for (BlsBrick b : currTileBricks) {
                            b.x += xOffset;
                            b.y += yOffset;
                        }
                        bricks.addAll(currTileBricks);
                    }
                }
            }
        }
    }

    private void plantTrees(HashMap<String, TileBuild> tileLibrary) {
        Random rand = new Random(layer.seed);
        int treeCount;
        String[] treeTypes = {"TallPineTree", "PineTree", "ShortPineTree"};
        double xOffset, yOffset;
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                if (copy[i][j] == SurfaceTile.FORESTPATH || copy[i][j] == SurfaceTile.FORESTFLOOR) {
                    if (copy[i][j] == SurfaceTile.FORESTPATH) {
                        treeCount = rand.nextInt(2);
                    } else if (copy[i][j] == SurfaceTile.FORESTFLOOR && rand.nextDouble() > 0.75) {
                        treeCount = (int) (Math.sqrt(rand.nextInt(4)) - rand.nextDouble());
                    } else {
                        treeCount = 0;
                    }
                    for (int k = 0; k < treeCount; k++) {
                        xOffset = i * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                        yOffset = j * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

                        TileBuild treeBuild = tileLibrary.get(treeTypes[rand.nextInt(treeTypes.length)]);
                        ArrayList<BlsBrick> currTileBricks = treeBuild.getRotatedBricks(rand.nextInt(4));
                        for (BlsBrick b : currTileBricks) {
                            b.x += xOffset;
                            b.y += yOffset;
                        }
                        bricks.addAll(currTileBricks);
                    }
                }
            }
        }
    }

    private void plantAllWalls(HashMap<String, TileBuild> tileLibrary) {
        Random rand = new Random(layer.seed);
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                if (copy[i][j].passable) {
                    ArrayList<Point> adj = layer.getOrthoAdjacent(i, j);
                    Point curr = new Point(i, j);
                    for (Point p : adj) {
                        int direction = GridUtils.getCompassDirectionTo(curr, p);
                        String name = copy[p.x][p.y].name + "Wall";
                        if (copy[p.x][p.y] == SurfaceTile.FOREST) {
                            name = "ForestWall" + (rand.nextInt(3) + 1);
                        }

                        if (!tileLibrary.containsKey(name)) {
                            continue;
                        }
                        TileBuild adjTile = tileLibrary.get(name);
                        ArrayList<BlsBrick> currTileBricks;
                        switch (direction) {
                            case GridUtils.NORTH: currTileBricks = adjTile.getRotatedBricks(1); break;
                            case GridUtils.EAST: currTileBricks = adjTile.getRotatedBricks(0); break;
                            case GridUtils.SOUTH: currTileBricks = adjTile.getRotatedBricks(3); break;
                            case GridUtils.WEST: currTileBricks = adjTile.getRotatedBricks(2); break;
                            default: throw new RuntimeException("Invalid direction!");
                        }
                        for (BlsBrick b : currTileBricks) {
                            b.x += i * 8 + 4;
                            b.y += j * 8 + 4;
                        }
                        bricks.addAll(currTileBricks);
                    }
                }
            }
        }
    }

    private void plantOptimizedTiles(ArrayList<Rectangle> coverage, String[] tiles, HashMap<String, TileBuild> tileLibrary) {
        for (Rectangle rect : coverage) {
            Point corner = new Point(rect.leftCorner);
            //offset corner by tile size (16x16) and shift to center of rect
            corner.x *= 8; corner.y *= 8;
            corner.x += rect.width * 4; corner.y += rect.height * 4;

            String tileChoice;
            switch (rect.width) {
                case 1: tileChoice = tiles[0]; break;
                case 2: tileChoice = tiles[1]; break;
                case 3: tileChoice = tiles[2]; break;
                case 4: tileChoice = tiles[3]; break;
                default: throw new RuntimeException("Width of optimized rectangle is not in range [1, 4]!");
            }

            TileBuild t = tileLibrary.get(tileChoice);

            ArrayList<BlsBrick> currTileBricks = t.getBricks();
            for (BlsBrick b : currTileBricks) {
                b.x += corner.x;
                b.y += corner.y;
            }
            bricks.addAll(currTileBricks);
        }
    }

    @Override
    public String nextBrick() {
        if (bricks.size() <= 0) {
            return null;
        }
        BlsBrick curr = bricks.remove(0);
        return curr.toStringOffset(offset.x, offset.y, STARTING_HEIGHT);
    }

    private ArrayList<Rectangle> calculateOptimumCover(MapTile type) {
        int[] sizes = {4, 3, 2, 1};
        boolean[][] collected = new boolean[copy.length][copy[0].length];

        int currSize;
        ArrayList<Point> curr = new ArrayList<>();
        ArrayList<Rectangle> optimized = new ArrayList<>();
        for (int size : sizes) {
            currSize = size;
            for (int i = 0; i < copy.length; i++) {
                for (int j = 0; j < copy[i].length; j++) {
                    if (copy[i][j] == type && !collected[i][j]) {
                        ArrayList<Point> rect = getRectanglePoints(i, j, currSize, currSize);
                        if (rect == null) {
                            continue;
                        }
                        for (Point p : rect) {
                            if (copy[p.x][p.y] != type || collected[p.x][p.y]) {
                                curr.clear();
                                break;
                            } else {
                                curr.add(p);
                            }
                        }

                        if (curr.size() > 0) {
                            optimized.add(new Rectangle(currSize, currSize, curr.get(0)));
                            for (Point p : curr) {
                                collected[p.x][p.y] = true;
                            }
                            curr.clear();
                        }
                    }
                }
            }
        }
        return optimized;
    }

    private ArrayList<Point> getRectanglePoints(int x, int y, int width, int height) {
        ArrayList<Point> result = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height;j ++) {
                int currX = x + i;
                int currY = y + j;
                if (currX >= copy.length || currY >= copy[0].length) {
                    return null;
                } else {
                    result.add(new Point(currX, currY));
                }
            }
        }

        return result;
    }
}