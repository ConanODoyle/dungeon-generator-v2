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
import java.util.Random;

import static gen.lib.GridUtils.getRectanglePoints;

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

            "Mineshaft", "Mineshaft_R", "Mineshaft_T", "Mineshaft_X",
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
        this.extraCopy = layer.getExtraTilesArray();
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

        //optimize coverages
        ArrayList<Rectangle> lowCave = calculateOptimumCover(CaveTile.LowCave());
        ArrayList<Rectangle> midCave = calculateOptimumCover(CaveTile.Cave());
        ArrayList<Rectangle> highCave = calculateOptimumCover(CaveTile.HighCave());

        plantOptimizedTiles(lowCave, new String[]{
                TILESETS[0], TILESETS[1], TILESETS[2], TILESETS[3]}, tileLibrary);
        plantOptimizedTiles(midCave, new String[]{
                TILESETS[4], TILESETS[5], TILESETS[6], TILESETS[7]}, tileLibrary);
        plantOptimizedTiles(highCave, new String[]{
                TILESETS[8], TILESETS[9], TILESETS[10], TILESETS[11]}, tileLibrary);

        //plant all walls
        ArrayList<BlsBrick> tile;
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy.length; j++) {
                if (copy[i][j].equals(CaveTile.Rock())) {
                    tile = tileLibrary.get("RockWall").getBricks();
                    for (BlsBrick b : tile) {
                        b.x += i * 8 + 4;
                        b.y += j * 8 + 4;
                    }
                    bricks.addAll(tile);
                }
            }
        }

        Random rand = new Random(layer.seed);
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
