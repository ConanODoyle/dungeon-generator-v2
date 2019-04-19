package gen.map.surface;

import gen.map.MapLayer;
import gen.map.MapTile;
import gen.map.perlin.ImprovedNoise;
import gen.map.perlin.PerlinUtils;

import java.awt.*;
import java.util.*;

//Job: Understands how to generate the surface layer
public class SurfaceLayer extends MapLayer {
    private final int TOWNSIZE = 10;
    private static final double MINAREA = 0.18;

    public SurfaceLayer(int width, int height) {
        super(width, height);
        if (width < TOWNSIZE || height < TOWNSIZE) {
            throw new RuntimeException("Cannot instantiate SurfaceLayer with size less than " + TOWNSIZE + "!");
        }
    }

    @Override
    public void generate() {
        //create rand with seed, for consistency/determinism
        Random rand = new Random(seed);

        //first create a border
        for (int i = 0; i < width; i++) {
            tiles[i][0] = SurfaceTile.TALLCLIFF;
            tiles[i][height - 1] = SurfaceTile.TALLCLIFF;
        }

        for (int i = 0; i < height; i++) {
            tiles[0][i] = SurfaceTile.TALLCLIFF;
            tiles[width - 1][i] = SurfaceTile.TALLCLIFF;
        }

        //generate map elements
        generateForest(rand);
        generateCliffs(rand);
        generateTown();
        ArrayList<Point> ends = generatePaths(rand);
        removeInaccessibleAreas();

        //generate features
        generateCaves(rand);
        generateGlen(ends, rand);

        //check if map is big enough, if not, retry generation
        if (getTotalAccessibleArea() < MINAREA * height * width) {
            System.out.println("Generation did not generate a large enough map! Changing seed...");
            seed = rand.nextLong();
            generate();
            return;
        }

        hasGenerated = true;
    }

    private void generateGlen(ArrayList<Point> ends, Random rand) {
        System.out.println("Attempting to generate glen...");

        ArrayList<Point> validGlens = new ArrayList<>();
        for (Point p : ends) {
            //dont generate glens too close to town
            if (Math.abs(p.x - width / 2) + Math.abs(p.y - height / 2) < (width + height) / 4) {
                continue;
            } else if (!tiles[p.x][p.y].passable) {
                continue;
            }
            ArrayList<Point> adj = getAdjacent(p.x, p.y);
            Set<Point> local = new HashSet<>();
            for (Point a : adj) {
                local.addAll(getAdjacent(a.x, a.y));
            }

            validGlens.add(p);
            for (Point a : local) {
                if (tiles[a.x][a.y] != SurfaceTile.FORESTPATH
                        && tiles[a.x][a.y] != SurfaceTile.FOREST) {
                    validGlens.remove(p);
                }
            }
        }

        if (validGlens.size() <= 0) {
            System.out.println("    Could not generate glen");
            return;
        }

        Point p = validGlens.get(rand.nextInt(validGlens.size()));
        for (Point g : getAdjacent(p.x, p.y)) {
            tiles[g.x][g.y] = SurfaceTile.GLEN;
        }
        tiles[p.x][p.y] = SurfaceTile.GLEN;
        System.out.println("    Generated glen at " + p.x + "," + p.y);
    }

    private void generateCaves(Random rand) {
        ArrayList<Point> cliffs = getTilesByType(SurfaceTile.CLIFF);
        ArrayList<Point> validCaves = new ArrayList<>();

        for (Point p : cliffs) {
            ArrayList<Point> adj = getOrthoAdjacent(p.x, p.y);
            for (Point a : adj) {
                if (tiles[a.x][a.y].passable) {
                    validCaves.add(p);
                    break;
                }
            }
        }

        if (validCaves.size() <= 0) {
            System.out.println("Could not generate caves: no cliffs were adjacent to accessible space!");
            return;
        }
        int numCaves = rand.nextInt((int) Math.sqrt(validCaves.size())) + (int) Math.sqrt(validCaves.size());
        for (int i = 0; i < numCaves; i++) {
            Point curr = validCaves.remove(rand.nextInt(validCaves.size()));
            tiles[curr.x][curr.y] = SurfaceTile.CAVE;
            for (Point p : getAdjacent(curr.x, curr.y)) {
                if (tiles[p.x][p.y] == SurfaceTile.CAVE) {
                    tiles[curr.x][curr.y] = SurfaceTile.CLIFF;
                    i--;
                    break;
                }
            }
        }
    }

    private double[][] generateForest(Random rand) {
        ImprovedNoise base = new ImprovedNoise(rand.nextLong());
        ImprovedNoise adjust = new ImprovedNoise(rand.nextLong());
        ImprovedNoise adjust2 = new ImprovedNoise(rand.nextLong());
        double weight1 = 2, step1 = 0.11;
        double weight2 = 0.7, step2 = 0.015;
        double weight3 = 2, step3 = 0.11, xOffset3 = 0.05, yOffset3 = 0.05;
        double[][] baseNoise = base.generate2DNoise(width, height, step1);
        double[][] adjustNoise = adjust.generate2DNoise(width, height, step2);
        double[][] adjust2Noise = adjust2.generate2DNoise(width, height, step3, xOffset3, yOffset3);
        double[][] noise = PerlinUtils.getNoiseWeightedSum(baseNoise, weight1, adjustNoise, weight2);
        noise = PerlinUtils.getNoiseWeightedSum(noise, weight1 + weight2, adjust2Noise, weight3);

        //adjust the cutoff value until we get a map of an appropriate size
        double cutoff = 0.99; //range of perlin noise is -1 to 1
        double minArea = rand.nextDouble() * 0.14 + 0.18;
        while (PerlinUtils.getTotalCutoffArea(noise, cutoff) < minArea * width * height) {
            cutoff -= 0.02;
        }

        for (int i = 1; i < noise.length - 1; i++) {
            for (int j = 1; j < noise[0].length - 1; j++) {
                if (noise[i][j] > cutoff) {
                    tiles[i][j] = SurfaceTile.FORESTFLOOR;
                } else {
                    tiles[i][j] = SurfaceTile.FOREST;
                }
            }
        }

        PerlinUtils.exportBicolorPerlin(noise, cutoff);
        PerlinUtils.exportPerlin(noise, 50);

        return noise;
    }

    private double[][] generateCliffs(Random rand) {
        ImprovedNoise base = new ImprovedNoise(rand.nextLong());
        ImprovedNoise adjust = new ImprovedNoise(rand.nextLong());
        ImprovedNoise adjust2 = new ImprovedNoise(rand.nextLong());
        double weight1 = 2, step1 = 0.05;
        double weight2 = 0.7, step2 = 0.005;
        double weight3 = 2, step3 = 0.05, xOffset3 = 0.045, yOffset3 = 0.045;
        double[][] baseNoise = base.generate2DNoise(width, height, step1);
        double[][] adjustNoise = adjust.generate2DNoise(width, height, step2);
        double[][] adjust2Noise = adjust2.generate2DNoise(width, height, step3, xOffset3, yOffset3);
        double[][] noise = PerlinUtils.getNoiseWeightedSum(baseNoise, weight1, adjustNoise, weight2);
        noise = PerlinUtils.getNoiseWeightedSum(noise, weight1 + weight2, adjust2Noise, weight3);

        //adjust the cutoff value until we get a map of an appropriate size
        double cutoff = 0.99; //range of perlin noise is -1 to 1
        double minArea = rand.nextDouble() * 0.06 + 0.04;
        while (PerlinUtils.getTotalCutoffArea(noise, cutoff) < minArea * width * height) {
            cutoff -= 0.02;
        }

        for (int i = 1; i < noise.length - 1; i++) {
            for (int j = 1; j < noise[0].length - 1; j++) {
                if (noise[i][j] > cutoff) {
                    tiles[i][j] = SurfaceTile.CLIFF;
                }
            }
        }

//        PerlinUtils.exportBicolorPerlin(noise, cutoff);
//        PerlinUtils.exportPerlin(noise, 50);

        return noise;
    }

    public int removeInaccessibleAreas() {
        boolean[][] mask = new boolean[width][height];
        int startX = width / 2, startY = height / 2; //town always in center
        ArrayList<Point> queue = new ArrayList<>();
        queue.add(new Point(startX, startY));

        while (queue.size() > 0) {
            Point curr = queue.remove(0);
            mask[curr.x][curr.y] = true;
            ArrayList<Point> adjacent = getOrthoAdjacent(curr.x, curr.y);
            for (Point p : adjacent) {
                if (!mask[p.x][p.y] && tiles[p.x][p.y].passable
                        && p.x > 0 && p.x < width - 1
                        && p.y > 0 && p.y < height - 1) {
                    mask[p.x][p.y] = true;
                    queue.add(p);
                }
            }
        }

        int totalRemoved = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!mask[i][j] && tiles[i][j].passable) {
                    tiles[i][j] = SurfaceTile.FOREST;
                    totalRemoved++;
                }
            }
        }
        return totalRemoved;
    }

    private ArrayList generatePaths(Random rand) {
        ArrayList<Point> unlinkedEnds = new ArrayList<>();
        int mod = (int) Math.ceil(Math.sqrt(width * height) / 40);
        int totalPaths = rand.nextInt(4 + mod) + 8 + mod / 2;
        double bigStep = 0.024;
        double smallStep = 0.16;//03 / 2;
        double bigMul = 1;
        double smallMul = 5;

        ImprovedNoise big = new ImprovedNoise(rand.nextLong());//1297618227837464957L);
        ImprovedNoise small = new ImprovedNoise(rand.nextLong());//1297618227837464957L);

        double[][] perlinBig = big.generate2DNoise(width, height, bigStep);
        double[][] perlinSmall = small.generate2DNoise(width, height, smallStep, smallStep / 2, smallStep / 2);
        double[][] perlinSum = PerlinUtils.getNoiseWeightedSum(perlinBig, bigMul, perlinSmall, smallMul);

        int sx = width / 2, sy = height / 2, dx, dy;
        for (int i = 0; i < totalPaths; i++) {
            if (rand.nextDouble() > 0.8 && i > 0) { //randomly reset path linkage (if not first path)
                unlinkedEnds.add(new Point(sx, sy));
                sx = rand.nextInt(width - 2) + 1;
                sy = rand.nextInt(height - 2) + 1;
            }
            do {
                dx = rand.nextInt(width - 2) + 1;
                dy = rand.nextInt(height - 2) + 1;
            } while (Math.abs(dx - sx) + Math.abs(dy - sy) < 8);
            generateForestPath(sx, sy, dx, dy, perlinSum);
            sx = dx;
            sy = dy;
        }
        return unlinkedEnds;
    }

    //Generate a path between the two points, preferring higher values in noise
    private void generateForestPath(int sx, int sy, int dx, int dy, double[][] noise) {
//        System.out.println("Generating path from (" + sx + "," + sy + ") to (" + dx + "," + dy + ")");
        Queue<Point> search = new PriorityQueue<>((o1, o2) -> {
            double o1value = Math.sqrt(Math.pow(dx - o1.x, 2) + Math.pow(dy - o1.y, 2)) * (4 + (noise[o1.x][o1.y] + 1) * 2);
            double o2value = Math.sqrt(Math.pow(dx - o2.x, 2) + Math.pow(dy - o2.y, 2)) * (4 + (noise[o2.x][o2.y] + 1) * 2);
            return Double.compare(o1value, o2value);
        });
        boolean[][] checked = new boolean[width][height];
        ArrayList<Point> path = new ArrayList<>();

        search.add(new Point(sx, sy));
        checked[sx][sy] = true;

        boolean searchComplete = false;
        while (search.size() != 0) {
            Point curr = search.poll();
            path.add(curr);
            if (curr.x == dx && curr.y == dy) {
                searchComplete = true;
                break;
            }
            ArrayList<Point> adjacent = getOrthoAdjacent(curr.x, curr.y);
            for (Point p : adjacent) {
                if (!checked[p.x][p.y]
                        && p.x > 1 && p.x < width - 1
                        && p.y > 1 && p.y < height - 1) {
                    search.add(new Point(p.x, p.y));
                    checked[p.x][p.y] = true;
                }
            }
        }

        if (!searchComplete) {
            System.out.println("Could not find path between " + sx + "," + sy + " and " + dx + "," + dy + "!");
            return;
        }

        for (Point p : path) {
            MapTile curr = tiles[p.x][p.y];
            if (curr != SurfaceTile.TOWN
                    && curr != SurfaceTile.FORESTFLOOR
                    && curr != SurfaceTile.CLIFF) {
                tiles[p.x][p.y] = SurfaceTile.FORESTPATH;
            }
        }
    }

    @Override
    public boolean validateGeneration() {
        if (!hasGenerated) {
            return false;
        }

        //check for border cliffs
        for (int i = 0; i < width; i++) {
            if (tiles[i][0] != SurfaceTile.TALLCLIFF
                    || tiles[i][height - 1] != SurfaceTile.TALLCLIFF) {
                return false;
            }
        }
        for (int i = 0; i < height; i++) {
            if (tiles[0][i] != SurfaceTile.TALLCLIFF
                    || tiles[width - 1][i] != SurfaceTile.TALLCLIFF) {
                return false;
            }
        }

        //check for town at center
        int wCenter = width / 2 + (width + 1) % 2 - 1;
        int hCenter = width / 2 + (width + 1) % 2 - 1;
        if (tiles[wCenter][hCenter] != SurfaceTile.TOWN) {
            return false;
        }

        int pathCount = 0;
        int accessibleCount = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //check for empty tiles
                if (tiles[i][j] == MapTile.EMPTY) {
                    return false;
                }

                //check for pathtiles (should have at least some)
                if (tiles[i][j] == SurfaceTile.FORESTPATH) {
                    pathCount++;
                }

                if (tiles[i][j].passable) {
                    accessibleCount++;
                }
            }
        }

        if (pathCount <= 0) {
            return false;
        }
        if (accessibleCount < MINAREA * width * height) {
            return false;
        }

        return true;
    }

    private void generateTown() {
        int wCenter = width / 2 + (width + 1) % 2 - 1;
        int hCenter = width / 2 + (width + 1) % 2 - 1;
        for (int i = 0; i < TOWNSIZE; i++) {
            for (int j = 0; j < TOWNSIZE; j++) {
                tiles[wCenter - TOWNSIZE/2 + i][hCenter - TOWNSIZE/2 + j] = SurfaceTile.TOWN;
            }
        }
    }
}
