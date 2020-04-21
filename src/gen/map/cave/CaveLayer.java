package gen.map.cave;

import gen.export.MapLayerBuilder;
import gen.lib.NoiseGrid2D;
import gen.map.MapLayer;
import gen.map.MapTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class CaveLayer extends MapLayer {
    public MapTile[][][] caveTiles;
    private static int DEPTH = 5;

    public CaveLayer(int width, int height) {
        super(width, height);
        caveTiles = new MapTile[DEPTH][width][height];
        for (int j = 0; j < width; j++) {
            for (int k = 0; k < height; k++) {
                tiles[j][k] = CaveTile.Rock();
            }
        }
    }

    @Override
    public void generate() {
        Random rand = new Random(seed);

        //initialize all tiles to Rock
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                tiles[i][j] = CaveTile.Rock();
            }
        }

        generateCaverns(rand);

        ArrayList<Point> ends = generatePaths(rand);

        replaceInaccessibleAreas(ends.get(0).x, ends.get(0).y, CaveTile.Rock());
        removeHiddenTiles(ends.get(0).x, ends.get(0).y, CaveTile.Rock());
    }

    private void removeHiddenTiles(int x, int y, CaveTile type) {
        ArrayList<Point> exists = getTiles(type);
        for (Point p : exists) {
            boolean hasAccessibleAdjacent = false;
            ArrayList<Point> adj = getOrthoAdjacent(p.x, p.y);
            for (Point q : adj) {
                if (tiles[q.x][q.y].passable) {
                    hasAccessibleAdjacent = true;
                    break;
                }
            }
            if (!hasAccessibleAdjacent) {
                tiles[p.x][p.y] = MapTile.EMPTY;
            }
        }
    }


    private void generateCaverns(Random rand) {
        double weight1 = 2, step1 = 0.08;
        double weight2 = 0.7, step2 = 0.025;
        double weight3 = 2, step3 = 0.21, xOffset3 = 0.05, yOffset3 = 0.05;

        NoiseGrid2D baseNoise = new NoiseGrid2D(width, height, step1, rand.nextLong());
        baseNoise.addWeightedNoise(new NoiseGrid2D(width, height, step2, rand.nextLong()),
                weight1, weight2);
        baseNoise.addWeightedNoise(new NoiseGrid2D(width, height, step3, rand.nextLong(), xOffset3, yOffset3),
                weight1 + weight2, weight3);

        //adjust the cutoff value until we get a map of an appropriate size
        double cutoff = 0.99; //range of perlin noise is supposedly +-sqrt(N/4)
        double minArea = rand.nextDouble() * 0.14 + 0.48;
        while (baseNoise.getTotalCutoffArea(cutoff) < minArea * width * height) {
            cutoff -= 0.02;
        }

        for (int i = 1; i < baseNoise.width - 1; i++) {
            for (int j = 1; j < baseNoise.height - 1; j++) {
                if (baseNoise.getNoiseValue(i, j) > cutoff) {
                    tiles[i][j] = CaveTile.Rock();
                } else {
                    tiles[i][j] = CaveTile.Cave();
                }
            }
        }

        baseNoise = new NoiseGrid2D(width, height, 0.08, rand.nextLong(), rand.nextDouble(), rand.nextDouble());
        baseNoise.addWeightedNoise(new NoiseGrid2D(width, height, 0.25, rand.nextLong()),
                2, 1);
        double min = baseNoise.getMinNoiseValue(), max = baseNoise.getMaxNoiseValue();
        double step = (max - min) / 3;
        for (int i = 1; i < baseNoise.width - 1; i++) {
            for (int j = 1; j < baseNoise.height - 1; j++) {
                if (!tiles[i][j].equals(CaveTile.Cave())) {
                    continue;
                }

                if (baseNoise.getNoiseValue(i, j) < min + step)
                    tiles[i][j] = CaveTile.LowCave();
                else if (baseNoise.getNoiseValue(i, j) < min + step * 2)
                    tiles[i][j] = CaveTile.Cave();
                else
                    tiles[i][j] = CaveTile.HighCave();

            }
        }
    }

    private ArrayList<Point> generatePaths(Random rand) {
        ArrayList<Point> unlinkedEnds = new ArrayList<>();
        ArrayList<Point> ends = new ArrayList<>();
        int mod = (int) Math.ceil(Math.sqrt(width * height) / 5);
        int totalPaths = rand.nextInt(4 + mod) + 8 + mod / 2;
        double bigStep = 0.024;
        double smallStep = 0.16;//03 / 2;
        double bigMul = 1;
        double smallMul = 5;

//        NoiseGrid2D baseNoise = new NoiseGrid2D(width, height, bigStep, rand.nextLong());
//        baseNoise.addWeightedNoise(new NoiseGrid2D(width, height, smallStep, rand.nextLong(), smallStep / 2, smallStep / 2),
//                bigMul, smallMul);

        int sx = width / 2, sy = height / 2, dx, dy;
        unlinkedEnds.add(new Point(sx, sy));
        for (int i = 0; i < totalPaths; i++) {
            ends.add(new Point(sx, sy));
            if (rand.nextDouble() > 1.2 && i > 0) { //randomly reset path linkage (if not first path)
                unlinkedEnds.add(new Point(sx, sy));
                sx = rand.nextInt(width - 2) + 1;
                sy = rand.nextInt(height - 2) + 1;
            }
            do {
                dx = rand.nextInt(width - 2) + 1;
                dy = rand.nextInt(height - 2) + 1;
            } while (Math.abs(dx - sx) + Math.abs(dy - sy) < 25
                    || Math.abs(dx - sx) + Math.abs(dy - sy) > 60
                    || shortestDistFromPoints(new Point(dx, dy), ends) < 8);

            generateCavePath(sx, sy, dx, dy, rand);
            sx = dx;
            sy = dy;
        }
        return unlinkedEnds;
    }

    private int shortestDistFromPoints(Point point, ArrayList<Point> points) {
        int shortest = 100000;
        for (Point p : points) {
            int dist = Math.abs(p.x - point.x) + Math.abs(p.y - point.y);
            if (shortest > dist) {
                shortest = dist;
            }
        }
        return shortest;
    }

    //Generate a path between the two points, preferring higher values in noise
    private void generateCavePath(int sx, int sy, int dx, int dy, Random rand) {
//        System.out.println("Generating path from (" + sx + "," + sy + ") to (" + dx + "," + dy + ")");
        int moveX = dx - sx, moveY = dy - sy;

        //generate straight paths between x, y
        //don't make massive straight lines though, occasionally turn and truncate
        int distance = 0, min = 5, max = 10, total = 0;
        boolean isHorizontal = rand.nextBoolean();
        while (sx != dx || sy != dy) {
            MapTile currTile = tiles[sx][sy];
            if (currTile.equals(CaveTile.Rock())) {
                getOrthoAdjacent(sx, sy);
            }

            if (tiles[sx][sy].equals(CaveTile.Rock()))
                tiles[sx][sy] = CaveTile.Mineshaft();
            else
                specialTiles[sx][sy] = CaveTile.Mineshaft();
            total++;

            //force it to go the correct direction if we are already axis-aligned AND are close
//            if (Math.abs(dx - sx) + Math.abs(dy - sy) < 10) {
                if (sx == dx) {
                    isHorizontal = false;
                } else if (sy == dy) {
                    isHorizontal = true;
                }
//            }

            if (isHorizontal) {
                sx += (sx < dx ? 1 : -1);
                distance += 1;
                if (distance > min) {
                    if (distance >= max || (rand.nextDouble() > 0.8)) {
                        isHorizontal = false;
                        distance = 0;
                    }
                }
            } else {
                sy += (sy < dy ? 1 : -1);
                distance += 1;
                if (distance > min) {
                    if (distance >= max || (rand.nextDouble() > 0.8)) {
                        isHorizontal = true;
                        distance = 0;
                    }
                }
            }
        }
    }


    @Override
    public boolean validateGeneration() {
        return false;
    }

    @Override
    public MapLayerBuilder getBuilder() {
        return new CaveLayerBuilder(this);
    }
}
