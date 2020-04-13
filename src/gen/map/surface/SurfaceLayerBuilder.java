package gen.map.surface;

import gen.Main;
import gen.lib.Pair;
import gen.map.MapTile;
import gen.export.BlsBrick;
import gen.export.MapLayerBuilder;
import gen.lib.GridUtils;
import gen.parser.TileBuild;
import gen.parser.TileSearch;

import java.awt.*;
import java.util.*;

import static gen.lib.GridUtils.getCompassDirectionTo;
import static gen.lib.GridUtils.getRectanglePoints;

//Job: Understands how to convert a SurfaceLayer into a list of formatted .bls strings
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

            "ForestWall1", "ForestWall2", "ForestWall3",
            "CliffWall1", "CliffWall2", "CliffWall3", "CliffWallCorner", "CliffWallEnd", "CliffWallCornerFiller",
            "TallCliffRoof16x",
    };
    private static final String[] EXTRAS = {
            "TallPineTree", "ShortPineTree", "PineTree",
            "Petal", "RedFlower", "WhiteFlower", "PinkFlower", "YellowFlower",
            "Mushrooms", "SmallRock", "BigRock",

            "GoblinWall1", "GoblinSkull1", "GoblinSkull2", "GoblinSkull3", "GoblinCampfire1", "GoblinCampfire2",
            "GoblinTower", "GoblinHut1", "GoblinHut2", "GoblinShrine1",

            "RuinsWall1", "RuinsWall2", "RuinsStatue1", "RuinsStatue2", "RuinsStatue3", "RuinsStatue4", "RuinsStatue5",
            "RuinsStatue6", "RuinsStatue7",

            "Tent",
            "Well1", "Well2", "AbandFire", "Fence1", "Fence2", "Fence3",

            "forestspawner", "goblinspawner",
    };
    private static final String[] SPECIAL_TILES = {
//            "Town","Glen","Cave",

            "ShrineRuins1", "ShrineRuins2",
            "House1", "House2",

            "GoblinTent", "TrollBossEntrance",
    };

    SurfaceLayerBuilder(SurfaceLayer layer) {
        this.layer = layer;
        this.copy = layer.getTilesArray();
        this.extraCopy = layer.getExtraTilesArray();
        this.offset = new Point(0, 0);
    }

    private HashMap<String, TileBuild> loadTilesets() {
        HashMap<String, TileBuild> tileLibrary = new HashMap<>();
        TileSearch search = new TileSearch(Main.tilesetPath);
        for (String s : TILESETS) {
            tileLibrary.put(s, search.findTile(s));
        }
        for (String s : EXTRAS) {
            tileLibrary.put(s, search.findTile(s));
        }
        for (String s : SPECIAL_TILES) {
            tileLibrary.put(s, search.findSpecialTile(s));
        }
        return tileLibrary;
    }

    public void generateBuild() {
        //load the tilesets
        HashMap<String, TileBuild> tileLibrary = loadTilesets();

        //optimize coverages
        ArrayList<Rectangle> treeCover = calculateOptimumCover(SurfaceTile.Forest());
        ArrayList<Rectangle> cliffCover = calculateOptimumCover(SurfaceTile.Cliff());
        ArrayList<Rectangle> pathCover = calculateOptimumCover(SurfaceTile.ForestPath());
        ArrayList<Rectangle> floorCover = calculateOptimumCover(SurfaceTile.ForestFloor());

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
                if (copy[i][j].equals(SurfaceTile.TallCliff())) {
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

        //plant goblin camps, ruins, and settlements
        plantGoblinCamps(tileLibrary, rand);
        plantRuins(tileLibrary, rand);
        plantSettlements(tileLibrary, rand);

        //place spawners
        plantSpawners(tileLibrary, rand);

        //place bossroom entrance
        plantBossEntrance(tileLibrary, rand);

        //generate detailing
        HashSet<Point> occupied = new HashSet<>();
        occupied.addAll(layer.getExtraTiles(SurfaceTile.GoblinCamp()));
        occupied.addAll(layer.getExtraTiles(SurfaceTile.Ruins()));
        occupied.addAll(layer.getExtraTiles(SurfaceTile.Settlement()));
        occupied.addAll(layer.getExtraTiles(SurfaceTile.BossEntrance()));
        plantMushrooms(tileLibrary, rand, occupied);
        plantRocks(tileLibrary, rand, occupied);
        plantTrees(tileLibrary, rand, occupied);
        plantGrass(tileLibrary, rand);
        plantFlowers(tileLibrary, rand);
    }

    private void plantBossEntrance(HashMap<String, TileBuild> tileLibrary, Random rand) {
        TileBuild bossEntrance = tileLibrary.get("TrollBossEntrance");
        ArrayList<Point> location = layer.getExtraTiles(SurfaceTile.BossEntrance());
        Point bottomCorner, a = location.get(0), b = location.get(1);

        if (a.x < b.x || a.y < b.y) {
            bottomCorner = a;
        } else {
            bottomCorner = b;
        }

        int[] forestWallsA = {0, 0, 0, 0}; //nesw
        int[] forestWallsB = {0, 0, 0, 0};
        ArrayList<Point> adjacent = layer.getOrthoAdjacent(a.x, a.y);
        adjacent.remove(b);
        for (Point p : adjacent) {
            int dir = getCompassDirectionTo(a, p);
            if (!layer.tiles[p.x][p.y].passable) {
                forestWallsA[dir - 1] = 1;
            }
        }
        adjacent = layer.getOrthoAdjacent(b.x, b.y);
        adjacent.remove(a);
        for (Point p : adjacent) {
            int dir = getCompassDirectionTo(b, p);
            if (!layer.tiles[p.x][p.y].passable) {
                forestWallsB[dir - 1] = 1;
            }
        }
        int rot = 0;
        for (int i = 0; i < forestWallsA.length; i++) {
            if (forestWallsA[i] == forestWallsB[i] && forestWallsA[i] == 1) {
                //round correct rotation
                rot = (i + 1) % 4;
                break;
            }
        }
        buildTileAt(rand, bossEntrance,(bottomCorner.x + 1) * 8,(bottomCorner.y + 1) * 8, rot);

    }

    private void plantSpawners(HashMap<String, TileBuild> tileLibrary, Random rand) {
        ArrayList<Pair<Point, MapTile>> spawnerTiles = layer.getSpawnerPoints();

        double xOffset, yOffset;
        SurfaceTile goblinCamp = SurfaceTile.GoblinCamp();
        for (Pair<Point, MapTile> pair : spawnerTiles) {
            Point p = pair.getFirst();
            MapTile t = pair.getSecond();
            xOffset = p.x * 8 + 4;
            yOffset = p.y * 8 + 4;

            if (t.equals(goblinCamp)) {
                buildTileAt(rand, tileLibrary.get("goblinspawner"), xOffset, yOffset);
            } else {
                buildTileAt(rand, tileLibrary.get("forestspawner"), xOffset, yOffset);
            }
        }
    }


    //Camps
    private void plantSettlements(HashMap<String, TileBuild> tileLibrary, Random rand) {
        ArrayList<ArrayList<Point>> settlementTiles = layer.getExtraTilesGroups(SurfaceTile.Settlement());
        String[] structures = {"AbandFire", "Well1", "Well2"};
        String[] houses = {"House1", "House2"};
        String[] fences = {"Fence1", "Fence2", "Fence3"};

        for (ArrayList<Point> camp : settlementTiles) {
            //create fences
            HashSet<Point> fenceAdjacent = new HashSet<>();
            for (Point curr : camp) {
                ArrayList<Point> adj = layer.getOrthoAdjacent(curr.x, curr.y);
                for (Point p : adj) {
                    if (fenceAdjacent.contains(p)) {
                        continue;
                    }
                    int direction = GridUtils.getCompassDirectionTo(curr, p);
                    String name;
                    if (!camp.contains(p) && copy[p.x][p.y].passable && rand.nextDouble() > 0.6) {
                        name = fences[rand.nextInt(fences.length)];
                    } else {
                        continue;
                    }

                    TileBuild adjTile = tileLibrary.get(name);
                    int rotation;
                    switch (direction) {
                        case GridUtils.NORTH: rotation = 1; break;
                        case GridUtils.EAST: rotation = 0; break;
                        case GridUtils.SOUTH: rotation = 3; break;
                        case GridUtils.WEST: rotation = 2; break;
                        default: throw new RuntimeException("Invalid direction!");
                    }
                    fenceAdjacent.add(curr);
                    buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
                }
            }

            //create houses
            if (camp.size() > 4) {
                boolean cannotFind = false;
                int numHouses = rand.nextInt(Math.max((camp.size() - 4) / 4, 1)) + 1;
                while (numHouses-- > 0 && !cannotFind) {
                    cannotFind = true;
                    Collections.shuffle(camp, rand);
                    for (Point p : camp) {
                        ArrayList<Point> rect = getRectanglePoints(p.x, p.y, 2, 2);
                        if (rect == null || rect.size() != 4 || !camp.containsAll(rect)) {
                            continue;
                        }
                        int rot = rand.nextInt(4);
                        switch (rot) {
                            case 0: rect.remove(2); rect.remove(0); break;
                            case 1: rect.remove(3); rect.remove(2); break;
                            case 2: rect.remove(3); rect.remove(1); break;
                            case 3: rect.remove(1); rect.remove(0); break;
                        }

                        if (camp.containsAll(rect)) {
                            buildTileAt(rand, tileLibrary.get(houses[rand.nextInt(houses.length)]),(p.x + 1) * 8,(p.y + 1) * 8, rot);
                            camp.removeAll(rect);
                            cannotFind = false;
                            break;
                        }
                    }
                }
            }

            if (camp.size() >= 3) {// && rand.nextDouble() > 0.7) {
                Point curr = camp.remove(rand.nextInt(camp.size()));
                buildTileAt(rand, tileLibrary.get(structures[rand.nextInt(structures.length)]), curr.x * 8 + 4, curr.y * 8 + 4);
            }

        }
    }

    private void plantGoblinCamps(HashMap<String, TileBuild> tileLibrary, Random rand) {
        ArrayList<ArrayList<Point>> goblinCamps = layer.getExtraTilesGroups(SurfaceTile.GoblinCamp());
        String[] walls = {"GoblinWall1",};
        String[] towers = {"GoblinTower",};
        String[] campfires = {"GoblinCampfire1", "GoblinCampfire2"};
        String[] tents = {"GoblinTent"};
        String[] structures = {"GoblinShrine1", "GoblinHut1", "GoblinHut2"};

        for (ArrayList<Point> camp : goblinCamps) {
            //create border walls
            for (Point curr : camp) {
                ArrayList<Point> adj = layer.getOrthoAdjacent(curr.x, curr.y);
                for (Point p : adj) {
                    int direction = GridUtils.getCompassDirectionTo(curr, p);
                    String name;
                    if (!camp.contains(p) && copy[p.x][p.y].passable && rand.nextDouble() > 0.4) {
                        name = walls[rand.nextInt(walls.length)];
                    } else {
                        continue;
                    }

                    TileBuild adjTile = tileLibrary.get(name);
                    int rotation;
                    switch (direction) {
                        case GridUtils.NORTH: rotation = 1; break;
                        case GridUtils.EAST: rotation = 0; break;
                        case GridUtils.SOUTH: rotation = 3; break;
                        case GridUtils.WEST: rotation = 2; break;
                        default: throw new RuntimeException("Invalid direction!");
                    }
                    buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
                }
            }

            //create towers
            if (camp.size() >= 3) {
                for (int i = 0; i < rand.nextInt(2) + (camp.size() > 6 ? 1 : 0); i++) {
                    Point curr = camp.remove(rand.nextInt(camp.size()));
                    buildTileAt(rand, tileLibrary.get(towers[rand.nextInt(towers.length)]), curr.x * 8 + 4, curr.y * 8 + 4);
                }
            }

            //create campfires
            if (camp.size() >= 3) {// && rand.nextDouble() > 0.7) {
                Point curr = camp.remove(rand.nextInt(camp.size()));
                buildTileAt(rand, tileLibrary.get(campfires[rand.nextInt(campfires.length)]), curr.x * 8 + 4, curr.y * 8 + 4);
            }

            //create tents/huts
            if (camp.size() >= 6) {
                for (Point p : camp) {
                    ArrayList<Point> rect = getRectanglePoints(p.x, p.y, 2, 2);
                    if (rect == null || rect.size() != 4 || !camp.containsAll(rect)) {
                        continue;
                    }
                    int rot = rand.nextInt(4);
//                    switch (rot) {
//                        case 0: rect.remove(0); rect.remove(1); break;
//                        case 1: rect.remove(0); rect.remove(0); break;
//                        case 2: rect.remove(1); rect.remove(2); break;
//                        case 3: rect.remove(2); rect.remove(2); break;
//                    }

                    if (camp.containsAll(rect)) {
                        buildTileAt(rand, tileLibrary.get(tents[rand.nextInt(tents.length)]),(p.x + 1) * 8,(p.y + 1) * 8, rot);
                        camp.removeAll(rect);
                        break;
                    }
                }
            }

            int alternate = 0;
            while (camp.size() > 6) {
                camp.remove(rand.nextInt(camp.size())); //don't generate that many structures - remove 2 slots per gen
                Point curr = camp.remove(rand.nextInt(camp.size()));
                buildTileAt(rand, tileLibrary.get(structures[(alternate++) % structures.length]), curr.x * 8 + 4, curr.y * 8 + 4);
            }
        }
    }

    private void plantRuins(HashMap<String, TileBuild> tileLibrary, Random rand) {
        ArrayList<ArrayList<Point>> ruins = layer.getExtraTilesGroups(SurfaceTile.Ruins());
        String[] walls = {"RuinsWall1", "RuinsWall2",};
        String[] statues = {"RuinsStatue1", "RuinsStatue2", "RuinsStatue3", "RuinsStatue4", "RuinsStatue5",
                "RuinsStatue6", "RuinsStatue7",};
        String[] special = {"ShrineRuins1", "ShrineRuins2",};

        for (ArrayList<Point> camp : ruins) {
            //create border walls
            for (Point curr : camp) {
                ArrayList<Point> adj = layer.getOrthoAdjacent(curr.x, curr.y);
                for (Point p : adj) {
                    int direction = GridUtils.getCompassDirectionTo(curr, p);
                    String name;
                    if (!camp.contains(p) && copy[p.x][p.y].passable && rand.nextDouble() > 0.7) {
                        name = walls[rand.nextInt(walls.length)];
                    } else {
                        continue;
                    }

                    TileBuild adjTile = tileLibrary.get(name);
                    int rotation;
                    switch (direction) {
                        case GridUtils.NORTH: rotation = 1; break;
                        case GridUtils.EAST: rotation = 0; break;
                        case GridUtils.SOUTH: rotation = 3; break;
                        case GridUtils.WEST: rotation = 2; break;
                        default: throw new RuntimeException("Invalid direction!");
                    }
                    buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
                }
            }

            if (camp.size() > 4) {
                for (Point p : camp) {
                    ArrayList<Point> rect = getRectanglePoints(p.x, p.y, 2, 2);
                    if (rect != null && camp.containsAll(rect)) {
                        buildTileAt(rand, tileLibrary.get(special[rand.nextInt(special.length)]),(p.x + 1) * 8,(p.y + 1) * 8);
                        camp.removeAll(rect);
                        break;
                    }
                }
            }

            if (camp.size() > 4) {
                for (int i = 0; i < rand.nextInt(2) + 1; i++) {
                    Point curr = camp.remove(rand.nextInt(camp.size()));
                    buildTileAt(rand, tileLibrary.get(statues[rand.nextInt(statues.length)]), curr.x * 8 + 4, curr.y * 8 + 4);
                }
            }
        }
    }


    //Foliage
    private void plantFlowers(HashMap<String, TileBuild> tileLibrary, Random rand) {
        int flowerCount;
        String[] flowerTypes = {"RedFlower", "WhiteFlower", "PinkFlower", "YellowFlower"};
        double xOffset, yOffset;

        ArrayList<Point> flowerTiles = new ArrayList<>(layer.getTiles(SurfaceTile.ForestFloor()));

        for (Point p : flowerTiles) {
            if (rand.nextDouble() > 0.9) {
                flowerCount = rand.nextInt(8);
            } else {
                flowerCount = 0;
            }
            for (int k = 0; k < flowerCount; k++) {
                xOffset = p.x * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                yOffset = p.y * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

                buildTileAt(rand, tileLibrary.get(flowerTypes[rand.nextInt(flowerTypes.length)]), xOffset, yOffset);
            }
        }
    }

    private void plantGrass(HashMap<String, TileBuild> tileLibrary, Random rand) {
        int grassCount;
        String[] grassTypes = {"Petal"};
        double xOffset, yOffset;

        ArrayList<Point> grassTiles = new ArrayList<>();
        grassTiles.addAll(layer.getTiles(SurfaceTile.ForestFloor()));
        grassTiles.addAll(layer.getTiles(SurfaceTile.ForestPath()));

        for (Point p : grassTiles) {
            if (copy[p.x][p.y].equals(SurfaceTile.ForestPath()) && rand.nextDouble() > 0.7) {
                grassCount = rand.nextInt(2);
            } else if (copy[p.x][p.y].equals(SurfaceTile.ForestFloor()) && rand.nextDouble() > 0.8) {
                grassCount = rand.nextInt(3) + 1;
            } else {
                continue;
            }

            for (int k = 0; k < grassCount; k++) {
                xOffset = p.x * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);
                yOffset = p.y * 8 + 4 + ((double) rand.nextInt(17) / 2 - 4);

                buildTileAt(rand, tileLibrary.get(grassTypes[rand.nextInt(grassTypes.length)]), xOffset, yOffset);
            }
        }
    }

    private void plantRocks(HashMap<String, TileBuild> tileLibrary, Random rand, HashSet<Point> occupied) {
        String[] rockTypes = {"SmallRock", "BigRock"};
        double xOffset, yOffset;

        ArrayList<Point> rockTiles = new ArrayList<>();
        rockTiles.addAll(layer.getTiles(SurfaceTile.ForestFloor()));
        rockTiles.addAll(layer.getTiles(SurfaceTile.ForestPath()));

        for (Point p : rockTiles) {
            if (occupied.contains(p)) {
                continue;
            } else if (rand.nextDouble() > 0.01) {
                continue;
            }
            occupied.add(p);

            xOffset = p.x * 8 + 4 + ((double) rand.nextInt(5) / 2 - 1);
            yOffset = p.y * 8 + 4 + ((double) rand.nextInt(5) / 2 - 1);

            buildTileAt(rand, tileLibrary.get(rockTypes[rand.nextInt(rockTypes.length)]), xOffset, yOffset);
        }
    }

    private void plantMushrooms(HashMap<String, TileBuild> tileLibrary, Random rand, HashSet<Point> occupied) {
        String[] mushroomTypes = {"Mushrooms"};
        double xOffset, yOffset;

        ArrayList<Point> mushroomTiles = new ArrayList<>();
        mushroomTiles.addAll(layer.getTiles(SurfaceTile.ForestFloor()));
        mushroomTiles.addAll(layer.getTiles(SurfaceTile.ForestPath()));

        for (Point p : mushroomTiles) {
            if (occupied.contains(p)) {
                continue;
            } else if (rand.nextDouble() > 0.003) {
                continue;
            }
            occupied.add(p);

            xOffset = p.x * 8 + 4 + ((double) rand.nextInt(13) / 2 - 3);
            yOffset = p.y * 8 + 4 + ((double) rand.nextInt(13) / 2 - 3);

            buildTileAt(rand, tileLibrary.get(mushroomTypes[rand.nextInt(mushroomTypes.length)]), xOffset, yOffset);
        }
    }

    private void plantTrees(HashMap<String, TileBuild> tileLibrary, Random rand, HashSet<Point> occupied) {
        int treeCount;
        String[] treeTypes = {"TallPineTree", "PineTree", "ShortPineTree"};
        double xOffset, yOffset;

        ArrayList<Point> treeTiles = new ArrayList<>();
        treeTiles.addAll(layer.getTiles(SurfaceTile.ForestFloor()));
        treeTiles.addAll(layer.getTiles(SurfaceTile.ForestPath()));

        for (Point p : treeTiles) {
            if (occupied.contains(p)) {
                continue;
            }

            if (copy[p.x][p.y].equals(SurfaceTile.ForestPath())) {
                treeCount = rand.nextInt(2);
            } else if (copy[p.x][p.y].equals(SurfaceTile.ForestFloor()) && rand.nextDouble() > 0.75) {
                treeCount = (int) (Math.sqrt(rand.nextInt(4)) - rand.nextDouble());
            } else {
                continue;
            }
            for (int k = 0; k < treeCount; k++) {
                xOffset = p.x * 8 + 4 + ((double) rand.nextInt(15) / 2 - 3.5);
                yOffset = p.y * 8 + 4 + ((double) rand.nextInt(15) / 2 - 3.5);

                buildTileAt(rand, tileLibrary.get(treeTypes[rand.nextInt(treeTypes.length)]), xOffset, yOffset);
            }
        }
    }


    //Walls and roofs
    private void plantAllWalls(HashMap<String, TileBuild> tileLibrary, Random rand) {
        String[] forestWalls = {"ForestWall1", "ForestWall2", "ForestWall3"};
        String[] cliffWalls = {"CliffWall1", "CliffWall2", "CliffWall3"};
        String[] cliffLWalls = {"CliffWallCorner", };
        String[] cliffUWalls = {"CliffWallEnd", };
        String[] cliffCornerWalls = {"CliffWallCornerFiller", };

        for (Point curr : layer.getPassableTiles()) {
            ArrayList<Point> adj = layer.getOrthoAdjacent(curr.x, curr.y);
            for (Point p : adj) {
                int direction = GridUtils.getCompassDirectionTo(curr, p);
                String name;
                if (copy[p.x][p.y].equals(SurfaceTile.Forest())) {
                    name = forestWalls[rand.nextInt(forestWalls.length)];
                } else {
                    continue;
                }

                if (!tileLibrary.containsKey(name)) {
                    continue;
                }
                TileBuild adjTile = tileLibrary.get(name);
                int rotation;
                switch (direction) {
                    case GridUtils.NORTH: rotation = 1; break;
                    case GridUtils.EAST: rotation = 0; break;
                    case GridUtils.SOUTH: rotation = 3; break;
                    case GridUtils.WEST: rotation = 2; break;
                    default: throw new RuntimeException("Invalid direction!");
                }
                buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
            }
        }


        //Cliff walls
        ArrayList<Point> cliffCheck = layer.getPassableTiles();
        cliffCheck.addAll(layer.getTiles(SurfaceTile.Forest()));
        for (Point curr : cliffCheck) {
            ArrayList<Point> adj = layer.getOrthoAdjacent(curr.x, curr.y);
            boolean[] cliffOccupied = new boolean[4];
            int numOccupied = 0;
            for (Point p : adj) {
                int direction = GridUtils.getCompassDirectionTo(curr, p);

                if (copy[p.x][p.y].equals(SurfaceTile.Cliff()) || copy[p.x][p.y].equals(SurfaceTile.TallCliff())) {
                    switch (direction) {
                        case GridUtils.NORTH: cliffOccupied[0] = true; numOccupied++; break;
                        case GridUtils.EAST: cliffOccupied[1] = true; numOccupied++; break;
                        case GridUtils.SOUTH: cliffOccupied[2] = true; numOccupied++; break;
                        case GridUtils.WEST: cliffOccupied[3] = true; numOccupied++; break;
                        default: throw new RuntimeException("Invalid direction!");
                    }
                }
            }

            if (numOccupied == 1) {
                //Get direction of wall space, put wall adjacent to that space
                String name = cliffWalls[rand.nextInt(cliffWalls.length)];
                int rotation = 0;
                for (int i = 0; i < 4; i++) {
                    if (cliffOccupied[i]) {
                        rotation = i + 1;
                        break;
                    }
                }
                TileBuild adjTile = tileLibrary.get(name);
                switch (rotation) {
                    case GridUtils.NORTH: rotation = 1; break;
                    case GridUtils.EAST: rotation = 0; break;
                    case GridUtils.SOUTH: rotation = 3; break;
                    case GridUtils.WEST: rotation = 2; break;
                    default: throw new RuntimeException("Invalid direction!");
                }
                buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
            } else if (numOccupied == 3) {
                //Get direction of empty space, put U tile facing that space
                String name = cliffUWalls[rand.nextInt(cliffUWalls.length)];
                int rotation = 0;
                for (int i = 0; i < 4; i++) {
                    if (!cliffOccupied[i]) {
                        rotation = i + 1;
                        break;
                    }
                }
                TileBuild adjTile = tileLibrary.get(name);
                switch (rotation) {
                    case GridUtils.NORTH: rotation = 3; break;
                    case GridUtils.EAST: rotation = 2; break;
                    case GridUtils.SOUTH: rotation = 1; break;
                    case GridUtils.WEST: rotation = 0; break;
                    default: throw new RuntimeException("Invalid direction!");
                }
                buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
            } else if (numOccupied == 2) {
                int firstRotation = -1;
                int rotation = 0;
                for (int i = 0; i < 4; i++) {
                    if (!cliffOccupied[i]) {
                        rotation = i + 1;
                        if (firstRotation < 0) {
                            firstRotation = rotation;
                        } else {
                            break;
                        }
                    }
                }
                //check if walls are adjacent
                if (((firstRotation) % 4) + 1 == rotation || ((rotation) % 4 + 1) == firstRotation) {
                    //they are adjacent, using corner
                    String name = cliffLWalls[rand.nextInt(cliffLWalls.length)];

                    TileBuild adjTile = tileLibrary.get(name);
                    switch (rotation) {
                        case GridUtils.EAST: rotation = 3; break;
                        case GridUtils.SOUTH: rotation = 2; break;
                        case GridUtils.WEST:
                            if (firstRotation == GridUtils.SOUTH) {
                                rotation = 1;
                                break;
                            } else if (firstRotation == GridUtils.NORTH) {
                                rotation = 0;
                                break;
                            }
                        default: throw new RuntimeException("Invalid double direction for cliff corner!");
                    }
                    buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
                } else {
                    //they are not adjacent, using two single walls
                    String name = cliffWalls[rand.nextInt(cliffWalls.length)];
                    TileBuild adjTile = tileLibrary.get(name);
                    switch (rotation) {
                        case GridUtils.NORTH: rotation = 1; break;
                        case GridUtils.EAST: rotation = 0; break;
                        case GridUtils.SOUTH: rotation = 3; break;
                        case GridUtils.WEST: rotation = 2; break;
                        default: throw new RuntimeException("Invalid direction!");
                    }
                    buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, rotation);
                    buildTileAt(rand, adjTile, curr.x * 8 + 4, curr.y * 8 + 4, (rotation + 2) % 4);
                }
            }

            if (numOccupied < 3) { //generate corner pieces
                //check for corners
                int[] xOffset = {1, 1, -1, -1};
                int[] yOffset = {-1, 1, 1, -1};

                for (int i = 0; i < 4; i++) {
                    String name = cliffCornerWalls[rand.nextInt(cliffCornerWalls.length)];
                    MapTile diag = copy[curr.x + xOffset[i]][curr.y + yOffset[i]];
                    if (diag.equals(SurfaceTile.Cliff()) || diag.equals(SurfaceTile.TallCliff())) {
                        buildTileAt(rand, tileLibrary.get(name), curr.x * 8 + 4, curr.y * 8 + 4, i);
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
            for (Point p : layer.getTiles(type)) {
                if (!collected[p.x][p.y]) {
                    ArrayList<Point> rect = getRectanglePoints(p.x, p.y, currSize, currSize);
                    if (rect == null) {
                        continue;
                    }
                    for (Point q : rect) {
                        if (!copy[q.x][q.y].equals(type) || collected[q.x][q.y]) {
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
}