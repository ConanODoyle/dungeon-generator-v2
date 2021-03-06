package gen.map.cave;

import gen.Main;
import gen.export.BlsBrick;
import gen.export.MapLayerBuilder;
import gen.lib.Rectangle;
import gen.map.MapTile;
import gen.parser.TileBuild;
import gen.parser.TileSearch;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import static gen.lib.GridUtils.*;

public class CaveLayerBuilder extends MapLayerBuilder {
    private Point offset;
    private CaveLayer layer;
    private MapTile[][] copy;
    private MapTile[][] extraCopy;
    private static final double STARTING_HEIGHT = 100;
    private static final String[] TILESETS = {
            "CaveFloor16_low", "CaveFloor32_low", "CaveFloor48_low", "CaveFloor64_low",
            "CaveFloor16_mid", "CaveFloor32_mid", "CaveFloor48_mid", "CaveFloor64_mid",
            "CaveFloor16_high", "CaveFloor32_high", "CaveFloor48_high", "CaveFloor64_high",
            "RockWall",

            "Mineshaft", "Mineshaft_R", "Mineshaft_T", "Mineshaft_X", "MineshaftBridge",
            "CaveRampS", "CaveRampR", "CaveRampC",
    };
    private static final String[] EXTRAS = {
            "TallPineTree", "ShortPineTree", "PineTree",
    };
    private static final String[] SPECIAL_TILES = {
//            "Town","Glen","Cave",

            "ShrineRuins1", "ShrineRuins2",
    };

    public CaveLayerBuilder(CaveLayer caveLayer) {
        super();
        this.layer = caveLayer;
        this.copy = layer.getTilesArray();
        this.extraCopy = layer.getSpecialTilesArray();
        this.offset = new Point(1500, 1500);
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

    @Override
    public void generateBuild() {
        //load the tilesets
        HashMap<String, TileBuild> tileLibrary = loadTilesets();

        //place transition tiles first, remove from copy[][] so they don't get optimize-planted
        plantCaveTransitions(tileLibrary, CaveTile.Cave(), CaveTile.LowCave());

        //optimize coverages
        ArrayList<Rectangle> lowCave = calculateOptimumCover(CaveTile.LowCave());
        ArrayList<Rectangle> midCave = calculateOptimumCover(CaveTile.Cave());
        ArrayList<Rectangle> highCave = calculateOptimumCover(CaveTile.HighCave());

        //place optimized tiles
        plantOptimizedTiles(lowCave, new String[]{
                TILESETS[0], TILESETS[1], TILESETS[2], TILESETS[3]}, tileLibrary);
        plantOptimizedTiles(midCave, new String[]{
                TILESETS[4], TILESETS[5], TILESETS[6], TILESETS[7]}, tileLibrary);
        plantOptimizedTiles(highCave, new String[]{
                TILESETS[8], TILESETS[9], TILESETS[10], TILESETS[11]}, tileLibrary);

        //plant all walls
        Random rand = new Random(layer.seed);
        TileBuild tile;
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy.length; j++) {
                if (copy[i][j].equals(CaveTile.Rock())) {
                    tile = tileLibrary.get("RockWall");
                    buildTileAt(rand, tile, i*8 + 4, j*8 + 4);
                }
            }
        }

        plantMineshafts(tileLibrary);

    }

    private void plantCaveTransitions(HashMap<String, TileBuild> tileLibrary, MapTile type, MapTile lower) {
        TileBuild rampS = tileLibrary.get("CaveRampS");
        TileBuild rampR = tileLibrary.get("CaveRampR");
        TileBuild rampC = tileLibrary.get("CaveRampC");
        TileBuild curr = null;

        //get list of tiles - all type tiles adjacent to a tile of the lower level
        ArrayList<Point> edgeTiles = new ArrayList<>(), lowerTiles = layer.getTiles(lower);
        HashSet<Point> visibleEdges = new HashSet<>();
        for (Point p : lowerTiles) {
            ArrayList<Point> adj = layer.getAdjacent(p.x, p.y);
            for (Point q : adj) {
                MapTile peek = copy[q.x][q.y];
                if (peek.equals(type)) {
                    visibleEdges.add(q);
                }
            }
        }

        //remove found edges not adjacent to rock or another tile
        HashSet<Point> removePoints = new HashSet<>();
        for (Point p : visibleEdges) {
            ArrayList<Point> adj = layer.getOrthoAdjacent(p.x, p.y);
            boolean remove = true;
            for (Point q : adj) {
                if (copy[q.x][q.y].equals(type) || copy[q.x][q.y].equals(CaveTile.Rock())) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                removePoints.add(p);
            }
        }

        for (Point p : removePoints) {
            visibleEdges.remove(p);
        }

        //plant edge tiles
        removePoints.clear();
        int[] sameAdj = new int[4], lowerEdgeAdj = new int[4];
        int totalAdj, totalLowerAdj, rot = 0;
        for (Point p : visibleEdges) {
            ArrayList<Point> adj = layer.getOrthoAdjacent(p.x, p.y);
            sameAdj[0] = 0; sameAdj[1] = 0; sameAdj[2] = 0; sameAdj[3] = 0;
            lowerEdgeAdj[0] = 0; lowerEdgeAdj[1] = 0; lowerEdgeAdj[2] = 0; lowerEdgeAdj[3] = 0;
            totalAdj = 0; totalLowerAdj = 0;
            rot = 0;
            for (Point q : adj) {
                int dir = getCompassDirectionTo(p, q);
                if (copy[q.x][q.y].equals(type) || !copy[q.x][q.y].passable) {
                    sameAdj[dir - 1] = 1;
                    totalAdj++;
                }
            }

            adj = layer.getAdjacent(p.x, p.y);
            adj.removeAll(layer.getOrthoAdjacent(p.x, p.y));
            for (Point q : adj) {
                int dir = getAdjacentDirectionTo(p, q);
                if (copy[q.x][q.y].equals(lower) || !copy[q.x][q.y].passable) {
                    sameAdj[dir / 2] = 1;
                    totalLowerAdj++;
                }
            }

            if (totalAdj == 1) {
                curr = rampS;
                for (int i = 0; i < 4; i++) {
                    if (sameAdj[i] == 1) {
                        rot = (i + 3) % 4;
                        break;
                    }
                }

            } else if (totalAdj == 4) {
                if (totalLowerAdj != 1)
                {
                    curr = null;
                }
                else {
                    curr = rampC;
                    int open1 = -1;
                    for (int i = 0; i < 4; i++) {
                        if (lowerEdgeAdj[i] == 1) {
                            open1 = i;
                            if (lowerEdgeAdj[(i + 4 - 1) % 4] == 1) {
                                open1 = (i + 4 - 1) % 4;
                            } else {
                                //dont transition sandwiched tiles
                                curr = null;
                                open1 += 1;
                            }
                            break;
                        }
                    }
                    rot = (4 - open1) % 4;
                }
//                throw new IllegalStateException("Edge tile with 4 adjacent sides!");
            } else if (totalAdj == 3) {
                curr = rampS;
                if (sameAdj[0] == 0) rot = 3;
                if (sameAdj[1] == 0) rot = 2;
                if (sameAdj[2] == 0) rot = 1;
                if (sameAdj[3] == 0) rot = 0;
            } else if (totalAdj == 2) {
                int open1 = -1;
                for (int i = 0; i < 4; i++) {
                    if (sameAdj[i] == 1) {
                        open1 = i;
                        if (sameAdj[(i + 1) % 4] == 1) {
                            curr = rampR;
                        } else if (sameAdj[(i + 4 - 1) % 4] == 1) {
                            open1 = (i + 4 -1) % 4;
                            curr = rampR;
                        } else {
                            //dont transition sandwiched tiles
                            curr = null;
                            open1 += 1;
                        }
                        break;
                    }
                }
                rot = (4 - open1) % 4;
            }

            if (curr != null) {
//                throw new IllegalStateException("No mineshaft tile type selected!");
                buildTileAt(curr, p.x * 8 + 4, p.y * 8 + 4, rot);
                removePoints.add(p);
            }
        }
        for (Point p : removePoints) {
            copy[p.x][p.y] = lower;
        }
    }

    private void plantMineshafts(HashMap<String, TileBuild> tileLibrary) {
        TileBuild mineshaft = tileLibrary.get("Mineshaft");
        TileBuild mineshaftR = tileLibrary.get("Mineshaft_R");
        TileBuild mineshaftT = tileLibrary.get("Mineshaft_T");
        TileBuild mineshaftX = tileLibrary.get("Mineshaft_X");
        TileBuild curr = null;

        //plant in-wall mineshafts
        ArrayList<Point> mineshafts = layer.getTiles(CaveTile.Mineshaft());
        int[] passable = new int[4];
        int totalPassable, rot = 0;

        for (Point p : mineshafts) {
            ArrayList<Point> adj = layer.getOrthoAdjacent(p.x, p.y);
            passable[0] = 0; passable[1] = 0; passable[2] = 0; passable[3] = 0;
            totalPassable = 0;
            for (Point q : adj) {
                int dir = getCompassDirectionTo(p, q);
                if (copy[q.x][q.y].passable) {
                    passable[dir - 1] = 1;
                    totalPassable++;
                }
            }

            if (totalPassable == 1) {
                curr = mineshaft;
                if (passable[1] == 1 || passable[3] == 1) {
                    //east/west
                    rot = 0;
                } else {
                    rot = 1;
                }
            } else if (totalPassable == 4) {
                curr = mineshaftX;
                rot = 0;
            } else if (totalPassable == 3) {
                curr = mineshaftT;
                if (passable[0] == 0) rot = 0;
                if (passable[1] == 0) rot = 3;
                if (passable[2] == 0) rot = 2;
                if (passable[3] == 0) rot = 1;
            } else if (totalPassable == 2) {
                int open1 = -1;
                for (int i = 0; i < 4; i++) {
                    if (passable[i] == 1) {
                        open1 = i;
                        if (passable[(i + 1) % 4] == 1) {
                            curr = mineshaftR;
                        } else if (passable[(i + 4 - 1) % 4] == 1) {
                            open1 = (i + 4 -1) % 4;
                            curr = mineshaftR;
                        } else {
                            curr = mineshaft;
                            open1 += 1;
                        }
                        break;
                    }
                }
                if (open1 < 0) {
                    throw new IllegalStateException("Mineshaft has no adjacent passable tiles!");
                }
                rot = (4 - open1 + 2) % 4;
            }

            if (curr == null) {
                throw new IllegalStateException("No mineshaft tile type selected!");
            }

            buildTileAt(curr, p.x * 8 + 4, p.y * 8 + 4, rot);
        }

        //plant mineshaft bridges
        ArrayList<Point> mineshaftBridges = layer.getSpecialTiles(CaveTile.Mineshaft());
        mineshaftBridges.removeAll(mineshafts);
        curr = tileLibrary.get("MineshaftBridge");
        for (Point p : mineshaftBridges) {
            buildTileAt(curr, p.x * 8 + 4, p.y * 8 + 4, 0);
        }
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
}
