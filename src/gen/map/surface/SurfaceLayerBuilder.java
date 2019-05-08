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
    private MapTile[][] extraCopy;
    private static final double STARTING_HEIGHT = 100;
    private static final String[] TILESETS = {
            "ForestRoof16x", "ForestRoof32x", "ForestRoof48x", "ForestRoof64x",
            "CliffRoof16x", "CliffRoof32x", "CliffRoof48x", "CliffRoof64x",
            "ForestPath16x", "ForestPath32x", "ForestPath48x", "ForestPath64x",
            "ForestFloor16x", "ForestFloor32x", "ForestFloor48x", "ForestFloor64x",

            "ForestWall1", "ForestWall2", "ForestWall3", "CliffWall", "TallCliffWall", "TallCliffRoof16x",
    };
    private static final String[] EXTRAS = {
            "TallPineTree", "ShortPineTree", "PineTree", "Petal", "Grass", "Flower",

//            "GoblinWall1", "GoblinSkull1", "GoblinSkull2", "GoblinSkull3", "GoblinCampfire1", "GoblinCampfire2",
//            "GoblinTower",
//
//            "RuinsWall1", "RuinsWall2", "RuinsStatue1", "RuinsStatue2", "RuinsStatue3", "RuinsStatue4", "RuinsStatue5",
//            "RuinsStatue6", "RuinsStatue7",
    };
    public static final String[] SPECIAL_TILES = {
            "Town","Glen","Cave",
    };

    private ArrayList<BlsBrick> bricks = new ArrayList<>();

    SurfaceLayerBuilder(SurfaceLayer layer) {
        this.layer = layer;
        this.copy = layer.getTilesArray();
        this.extraCopy = layer.getExtraTilesArray();
        this.offset = new Point(0, 0);
    }

    private HashMap<String, TileBuild> loadTilesets() {
        HashMap<String, TileBuild> tileLibrary = new HashMap<>();
        TileSearch search = new TileSearch("resources/tilesets.bls");
        for (String s : TILESETS) {
            tileLibrary.put(s, search.findTile(s));
        }
        for (String s : EXTRAS) {
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

        Random rand = new Random(layer.seed);

        //plant walls
        plantAllWalls(tileLibrary, rand);

        //plant goblin camps
//        plantGoblinCamps();

        //plant ruins
//        plantRuins();

        //generate detailing
        plantTrees(tileLibrary, rand);
        plantGrass(tileLibrary, rand);
        plantFlowers(tileLibrary, rand);
    }

    private void plantGoblinCamps() {
        ArrayList<ArrayList<Point>> goblinCamps = layer.getExtraTilesGroups(SurfaceTile.GOBLINCAMP);
    }

    private void plantRuins() {

    }

    private void plantFlowers(HashMap<String, TileBuild> tileLibrary, Random rand) {
        int flowerCount;
        String[] flowerTypes = {"Flower"};
        double xOffset, yOffset;

        ArrayList<Point> flowerTiles = new ArrayList<>();
        flowerTiles.addAll(layer.getTiles(SurfaceTile.FORESTFLOOR));

        for (Point p : flowerTiles) {
            if (rand.nextDouble() > 0.9) {
                flowerCount = rand.nextInt(8);
            } else {
                flowerCount = 0;
            }
            for (int k = 0; k < flowerCount; k++) {
                xOffset = p.x * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                yOffset = p.y * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

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

    private void plantGrass(HashMap<String, TileBuild> tileLibrary, Random rand) {
        int grassCount;
        String[] grassTypes = {"Petal", "Grass"};
        double xOffset, yOffset;

        ArrayList<Point> grassTiles = new ArrayList<>();
        grassTiles.addAll(layer.getTiles(SurfaceTile.FORESTFLOOR));
        grassTiles.addAll(layer.getTiles(SurfaceTile.FORESTPATH));

        for (Point p : grassTiles) {
            if (copy[p.x][p.y] == SurfaceTile.FORESTPATH) {
                grassCount = rand.nextInt(2) + 1;
            } else if (copy[p.x][p.y] == SurfaceTile.FORESTFLOOR && rand.nextDouble() > 0.7) {
                grassCount = rand.nextInt(3) + 1;
            } else {
                grassCount = 1;
            }
            for (int k = 0; k < grassCount; k++) {
                xOffset = p.x * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                yOffset = p.y * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

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

    private void plantTrees(HashMap<String, TileBuild> tileLibrary, Random rand) {
        int treeCount;
        String[] treeTypes = {"TallPineTree", "PineTree", "ShortPineTree"};
        double xOffset, yOffset;

        ArrayList<Point> grassTiles = new ArrayList<>();
        grassTiles.addAll(layer.getTiles(SurfaceTile.FORESTFLOOR));
        grassTiles.addAll(layer.getTiles(SurfaceTile.FORESTPATH));

        for (Point p : grassTiles) {
            if (copy[p.x][p.y] == SurfaceTile.FORESTPATH) {
                treeCount = rand.nextInt(2);
            } else if (copy[p.x][p.y] == SurfaceTile.FORESTFLOOR && rand.nextDouble() > 0.75) {
                treeCount = (int) (Math.sqrt(rand.nextInt(4)) - rand.nextDouble());
            } else {
                treeCount = 0;
            }
            for (int k = 0; k < treeCount; k++) {
                xOffset = p.x * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                yOffset = p.y * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

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

    private void plantAllWalls(HashMap<String, TileBuild> tileLibrary, Random rand) {
        for (Point curr : layer.getPassableTiles()) {
            ArrayList<Point> adj = layer.getOrthoAdjacent(curr.x, curr.y);
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
                    b.x += curr.x * 8 + 4;
                    b.y += curr.y * 8 + 4;
                }
                bricks.addAll(currTileBricks);
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
            for (Point p : layer.getTiles(type)) {
                if (!collected[p.x][p.y]) {
                    ArrayList<Point> rect = getRectanglePoints(p.x, p.y, currSize, currSize);
                    if (rect == null) {
                        continue;
                    }
                    for (Point q : rect) {
                        if (copy[q.x][q.y] != type || collected[q.x][q.y]) {
                            curr.clear();
                            break;
                        } else {
                            curr.add(q);
                        }
                    }

                    if (curr.size() > 0) {
                        optimized.add(new Rectangle(currSize, currSize, curr.get(0)));
                        for (Point q : curr) {
                            collected[q.x][q.y] = true;
                        }
                        curr.clear();
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