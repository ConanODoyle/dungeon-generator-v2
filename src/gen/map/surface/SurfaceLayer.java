package gen.map.surface;

import gen.lib.GridUtils;
import gen.lib.ImprovedNoise;
import gen.lib.Pair;
import gen.lib.PerlinUtils;
import gen.map.MapLayer;
import gen.map.MapTile;

import java.awt.*;
import java.util.Queue;
import java.util.*;

import static gen.map.MapTile.isEmpty;
import static gen.map.surface.SurfaceTile.*;

//Job: Understands how to generate the surface layer
public class SurfaceLayer extends MapLayer {
    private final int TOWNSIZE = 14;
    private static final double MIN_AREA = 0.18;

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
            tiles[i][0] = SurfaceTile.TallCliff();
            tiles[i][height - 1] = SurfaceTile.TallCliff();
        }

        for (int i = 0; i < height; i++) {
            tiles[0][i] = SurfaceTile.TallCliff();
            tiles[width - 1][i] = SurfaceTile.TallCliff();
        }

        //generate map elements
        generateForest(rand);
        generateCliffs(rand);
        generateTown();
        ArrayList<Point> ends = generatePaths(rand);
        replaceInaccessibleAreas(width / 2, height / 2, SurfaceTile.Forest());

        //check if map is big enough, if not, retry generation
        if (getTotalAccessibleArea() < MIN_AREA * height * width) {
            System.out.println("Generation did not generate a large enough map! Changing seed...");
            seed = rand.nextLong();
            generate();
            return;
        }

        //generate ruins, goblin tiles
        growSurfaceTiles(rand, SurfaceTile.GoblinCamp(), rand.nextInt(7) + 5, 0.32);
        growSurfaceTiles(rand, SurfaceTile.Ruins(), rand.nextInt(10) + 3, 0.26);
        growSurfaceTiles(rand, SurfaceTile.Settlement(), rand.nextInt(3) + 2, 0.38, getTiles(SurfaceTile.ForestFloor()));

        //generate features
        //no caves rn cause ???
//        generateCaves(rand);
        generateGlen(ends, rand);

        generateSpawners(rand);

        if (!generateBossTeleport(rand)) {
            System.out.println("Boss room entrance generation failed! Changing seed...");
            seed = rand.nextLong();
            generate();
            return;
        }

        hasGenerated = true;
    }

    private boolean generateBossTeleport(Random rand) {
        ArrayList<Point> passable = getPassableTiles();
        ArrayList<Point> validLocations = new ArrayList<>();
        ArrayList<int[]> validLocationArrays = new ArrayList<>();
        int wCenter = width / 2 + (width + 1) % 2 - 1;
        int hCenter = height / 2 + (height + 1) % 2 - 1;

        for (Point p : passable) {
            if (Math.abs(p.x - wCenter) + Math.abs(p.y - hCenter) < width / 2 - 5
                    || !isEmpty(specialTiles[p.x][p.y]))
                continue;

            ArrayList<Point> local = getOrthoAdjacent(p.x, p.y);
            boolean failed = false;
            int[] forestWall = {0, 0, 0, 0};
            for (Point a : local) {
                if (isForest(tiles[a.x][a.y])) {
                    forestWall[GridUtils.getCompassDirectionTo(p, a) - 1] = 1;
                } else if (!tiles[a.x][a.y].passable
                        || (!isEmpty(specialTiles[a.x][a.y]) && !isBossEntrance(specialTiles[a.x][a.y]))) {
                    //adjacent to non-forest wall/non boss entrance
                    failed = true;
                    break;
                }
            }

            if (failed)
                continue;

            failed = true;
            for (int i = 0; i < forestWall.length; i++) {
                int opposite = (i + 2) % forestWall.length;
                if (forestWall[i] == 1 && forestWall[opposite] == 0) { //wall not opposite to wall
                    failed = false;
                } else if (forestWall[i] == 1 && forestWall[opposite] == 1) { //wall opposite to wall
                    failed = true;
                    break;
                }
            }

            if (!failed) {
                validLocations.add(p);
                validLocationArrays.add(forestWall);
                specialTiles[p.x][p.y] = SurfaceTile.BossEntrance();
            }
        }

        //only keep paired locations that are adjacent to a wall on the same side
        for (int i = 0; i < validLocations.size(); i++) {
            Point p = validLocations.get(i);
            ArrayList<Point> local = getOrthoAdjacent(p.x, p.y);
            int[] aWalls, pWalls = validLocationArrays.get(i);
            boolean failed = true;
            outer:
            for (Point a : local) {
                if (isBossEntrance(specialTiles[a.x][a.y])) {
                    aWalls = validLocationArrays.get(validLocations.indexOf(a));
                    for (int j = 0; j < pWalls.length; j++) {
                        if (aWalls[j] == 1 && pWalls[j] == 1) {
                            failed = false;
                            break outer;
                        }
                    }
                }
            }
            if (failed) {
                specialTiles[p.x][p.y] = MapTile.EMPTY;
                validLocations.remove(i);
                validLocationArrays.remove(i);
                i--;
            }
        }

        boolean picked = false;
        while (!picked && validLocations.size() > 0) {
            int idx = rand.nextInt(validLocations.size());
            Point p = validLocations.remove(idx);
            int[] aWalls, pWalls = validLocationArrays.remove(idx);

            Point adj = null;
            ArrayList<Point> local = getOrthoAdjacent(p.x, p.y);
            boolean failed = true;
            outer:
            for (Point a : local) {
                if (isBossEntrance(specialTiles[a.x][a.y])) {
                    aWalls = validLocationArrays.get(validLocations.indexOf(a));
                    for (int j = 0; j < pWalls.length; j++) {
                        if (aWalls[j] == 1 && pWalls[j] == 1) {
                            adj = a;
                            failed = false;
                            break outer;
                        }
                    }
                }
            }
            if (failed) {
                specialTiles[p.x][p.y] = MapTile.EMPTY;
            } else {
                validLocations.remove(adj);
                picked = true;
            }
        }

        if (!picked) {
            System.out.println("Failed to generate boss room entrance!");
        } else {
            System.out.println("Boss room generation succeeded, removing excess special tiles...");
            for (Point p : validLocations) {
                specialTiles[p.x][p.y] = MapTile.EMPTY;
            }
        }
        return picked;
    }

    private void generateSpawners(Random rand) {
        ArrayList<Point> passable = getPassableTiles();
        for (Point p : passable) {
            int x = p.x;
            int y = p.y;
            SurfaceTile t = null, te = null;
            if (tiles[x][y] == null) {
                continue;
            }

            if (tiles[x][y] instanceof SurfaceTile) {
                t = (SurfaceTile) tiles[x][y];
            }
            if (specialTiles[x][y] instanceof SurfaceTile) {
                te = (SurfaceTile) specialTiles[x][y];
            }

            if (te != null && te.spawnerChance > 0 && rand.nextDouble() < te.spawnerChance) {
                te.hasSpawner = true;
            } else if (t.spawnerChance > 0 && rand.nextDouble() < t.spawnerChance) {
                t.hasSpawner = true;
            }
        }
    }

    public ArrayList<Pair<Point, MapTile>> getSpawnerPoints() {
        ArrayList<Point> passable = getPassableTiles();
        ArrayList<Pair<Point, MapTile>> result = new ArrayList<>();
        for (Point p : passable) {
            int x = p.x;
            int y = p.y;
            MapTile t = tiles[x][y];
            MapTile te = specialTiles[x][y];
            if (te != null && te.hasSpawner) {
                result.add(new Pair<>(p, te));
            } else if (t.hasSpawner) {
                result.add(new Pair<>(p, t));
            }
        }
        return result;
    }

    private void growSurfaceTiles(Random rand, MapTile type, int groupCount, double growChance) {
        growSurfaceTiles(rand, type, groupCount, growChance, getPassableTiles());
    }

    private void growSurfaceTiles(Random rand, MapTile type, int groupCount, double growChance, ArrayList<Point> valid) {
        int curr, tries = 0;
        while (groupCount > 0 && tries++ < 10000) {
            curr = rand.nextInt(valid.size());
            Point currPoint = valid.remove(curr);
            int x = currPoint.x;
            int y = currPoint.y;
            if (tiles[x][y].passable && distanceToClosestTile(x, y, SurfaceTile.Town()) > 20
                    && !specialTiles[x][y].equals(type)) {
                specialTiles[x][y] = type;
                ArrayList<Point> nextadj, adj = getOrthoAdjacent(x, y);
                for (int i = 0; i < adj.size();) {
                    Point p = adj.remove(0);
                    if (!tiles[p.x][p.y].passable || specialTiles[p.x][p.y].equals(type)) {
                        continue;
                    }
                    specialTiles[p.x][p.y] = type;
                    nextadj = getOrthoAdjacent(p.x, p.y);
                    for (Point q : nextadj) {
                        if (rand.nextDouble() < growChance) {
                            adj.add(q);
                        }
                    }
                }
                groupCount--;
            }
        }

        if (tries >= 10000) {
            System.out.println("Failed to generate all " + type.name + " groups! Groups left: " + groupCount);
        }
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
                if (!isForestPath(tiles[a.x][a.y])
                        && !isForest(tiles[a.x][a.y])) {
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
            tiles[g.x][g.y] = SurfaceTile.Glen();
        }
        tiles[p.x][p.y] = SurfaceTile.Glen();
        System.out.println("    Generated glen at " + p.x + "," + p.y);
    }

    private void generateCaves(Random rand) {
        ArrayList<Point> cliffs = getTiles(SurfaceTile.Cliff());
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
            tiles[curr.x][curr.y] = SurfaceTile.Cave();
            for (Point p : getAdjacent(curr.x, curr.y)) {
                if (isCave(tiles[p.x][p.y])) {
                    tiles[curr.x][curr.y] = SurfaceTile.Cliff();
                    i--;
                    break;
                }
            }
        }
    }

    private void generateForest(Random rand) {
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
                    tiles[i][j] = SurfaceTile.ForestFloor();
                } else {
                    tiles[i][j] = SurfaceTile.Forest();
                }
            }
        }

    }

    private void generateCliffs(Random rand) {
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
                    tiles[i][j] = SurfaceTile.Cliff();
                }
            }
        }

    }

    private ArrayList<Point> generatePaths(Random rand) {
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
            if (!isTown(curr)
                    && !isForestFloor(curr)
                    && !isCliff(curr)) {
                tiles[p.x][p.y] = SurfaceTile.ForestPath();
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
            if (!isTallCliff(tiles[i][0])
                    || !isTallCliff(tiles[i][height - 1] )) {
                return false;
            }
        }
        for (int i = 0; i < height; i++) {
            if (!isTallCliff(tiles[0][i])
                    || !isTallCliff(tiles[width - 1][i])) {
                return false;
            }
        }

        //check for town at center
        int wCenter = width / 2 + (width + 1) % 2 - 1;
        int hCenter = width / 2 + (width + 1) % 2 - 1;
        if (!isTown(tiles[wCenter][hCenter])) {
            return false;
        }

        int pathCount = 0;
        int accessibleCount = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //check for empty tiles
                if (isEmpty(tiles[i][j])) {
                    return false;
                }

                //check for pathtiles (should have at least some)
                if (isForestPath(tiles[i][j])) {
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
        if (accessibleCount < MIN_AREA * width * height) {
            return false;
        }

        return true;
    }

    @Override
    public SurfaceLayerBuilder getBuilder() {
        return new SurfaceLayerBuilder(this);
    }

    private void generateTown() {
        int wCenter = width / 2 + (width + 1) % 2 - 1;
        int hCenter = height / 2 + (height + 1) % 2 - 1;
        for (int i = 0; i < TOWNSIZE; i++) {
            for (int j = 0; j < TOWNSIZE; j++) {
                tiles[wCenter - TOWNSIZE/2 + i][hCenter - TOWNSIZE/2 + j] = SurfaceTile.Town();
            }
        }
    }
}
