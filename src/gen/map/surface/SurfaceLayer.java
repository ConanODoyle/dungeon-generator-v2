package gen.map.surface;

import gen.map.MapLayer;
import gen.map.MapTile;
import gen.map.perlin.ImprovedNoise;
import gen.map.perlin.PerlinUtils;

import java.awt.*;
import java.util.*;

//Job: Understands how to generate the surface layer
public class SurfaceLayer extends MapLayer {
    private static final MapTile[] validTiles = {
            MapTile.TALLCLIFF,
            MapTile.CLIFF,
            MapTile.FORESTFLOOR,
            MapTile.FOREST,
            MapTile.FORESTPATH,
            MapTile.TOWN
    };
    private final int TOWNSIZE = 10;

    public SurfaceLayer(int width, int height) {
        super(width, height);
        if (width < TOWNSIZE || height < TOWNSIZE) {
            throw new RuntimeException("Cannot instantiate SurfaceLayer with size less than " + TOWNSIZE + "!");
        }
    }

    @Override
    public void generate() {
        //create rand with seed, for consistency/determinism
        Random rand = new Random(this.seed);

        //first create a border
        for (int i = 0; i < this.width; i++) {
            this.tiles[i][0] = MapTile.TALLCLIFF;
            this.tiles[i][this.height - 1] = MapTile.TALLCLIFF;
        }

        for (int i = 0; i < this.height; i++) {
            this.tiles[0][i] = MapTile.TALLCLIFF;
            this.tiles[this.width - 1][i] = MapTile.TALLCLIFF;
        }

        //generate a map for forest fields
        ImprovedNoise base = new ImprovedNoise(rand.nextLong());
        ImprovedNoise adjust = new ImprovedNoise(rand.nextLong());
        ImprovedNoise adjust2 = new ImprovedNoise(rand.nextLong());
        double weight1 = 2, step1 = 0.11;
        double weight2 = 0.7, step2 = 0.015;
        double weight3 = 2, step3 = 0.11, xoffset3 = 0.05, yoffset3 = 0.05;
        double[][] baseNoise = base.generate2DNoise(this.width, this.height, step1);
        double[][] adjustNoise = adjust.generate2DNoise(this.width, this.height, step2);
        double[][] adjust2Noise = adjust2.generate2DNoise(this.width, this.height, step3, xoffset3, yoffset3);
        double[][] noise = PerlinUtils.getNoiseWeightedSum(baseNoise, weight1, adjustNoise, weight2);
        noise = PerlinUtils.getNoiseWeightedSum(noise, weight1 + weight2, adjust2Noise, weight3);

        //adjust the cutoff value until we get a map of an appropriate size
        double cutoff = 0.99; //range of perlin noise is -1 to 1
        double minArea = rand.nextDouble() * 0.14 + 0.18;
        while (PerlinUtils.getTotalCutoffArea(noise, cutoff) < minArea * this.width * this.height) {
            cutoff -= 0.02;
        }

        for (int i = 1; i < noise.length - 1; i++) {
            for (int j = 1; j < noise[0].length - 1; j++) {
                if (noise[i][j] > cutoff) {
                    this.tiles[i][j] = MapTile.FORESTFLOOR;
                } else {
                    this.tiles[i][j] = MapTile.FOREST;
                }
            }
        }

        //generate town in center of map
        generateTown();

        //generate paths
        generatePaths(noise, rand);

        //extra pass to clean up inaccessible areas
        if (this.removeInaccessibleAreas() < 0.2 * this.height * this.width) {
            System.out.println("Generation did not generate a large enough map! Changing seed...");
            this.seed = rand.nextLong();
            this.generate();
            return;
        }

        //debug/utility exports
        PerlinUtils.exportBicolorPerlin(noise, cutoff);
        PerlinUtils.exportPerlin(noise, 50);
        this.exportAsImage();

        this.hasGenerated = true;
    }

    private int removeInaccessibleAreas() {
        boolean[][] mask = new boolean[this.width][this.height];
        int startx = this.width / 2, starty = this.height / 2; //town always in center
        ArrayList<Point> queue = new ArrayList<>();
        queue.add(new Point(startx, starty));

        int totalAccessible = 0;
        while (queue.size() > 0) {
            Point curr = queue.remove(0);
            mask[curr.x][curr.y] = true;
            Point[] adjacent = this.getOrthoAdjacent(curr.x, curr.y);
            for (Point p : adjacent) {
                if (!mask[p.x][p.y] && tiles[p.x][p.y].passable
                        && p.x > 0 && p.x < this.width - 1
                        && p.y > 0 && p.y < this.height - 1) {
                    mask[p.x][p.y] = true;
                    queue.add(p);
                }
            }
            totalAccessible++;
        }

        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                if (!mask[i][j] && tiles[i][j].passable) {
                    tiles[i][j] = MapTile.FOREST;
                }
            }
        }
        return totalAccessible;
    }

    private void generatePaths(double[][] noise, Random rand) {
        int totalPaths = rand.nextInt((int) (4 + Math.ceil(Math.sqrt(this.width * this.height) / 40))) + 8;
        double bigstep = 0.024;
        double smallstep = 0.16;//03 / 2;
        double bigmul = 1;
        double smallmul = 5;

        ImprovedNoise big = new ImprovedNoise(rand.nextLong());//1297618227837464957L);
        ImprovedNoise small = new ImprovedNoise(rand.nextLong());//1297618227837464957L);

        double[][] perlinbig = big.generate2DNoise(this.width, this.height, bigstep);
        double[][] perlinsmall = small.generate2DNoise(this.width, this.height, smallstep, smallstep / 2, smallstep / 2);
        double[][] perlinsum = PerlinUtils.getNoiseWeightedSum(perlinbig, bigmul, perlinsmall, smallmul);

        int sx = this.width / 2, sy = this.height / 2, dx, dy;
//        sx = rand.nextInt(this.width - 2) + 1;
//        sy = rand.nextInt(this.height - 2) + 1;
        for (int i = 0; i < totalPaths; i++) {
            if (rand.nextDouble() > 0.8) { //randomly reset path linkage
                sx = rand.nextInt(this.width - 2) + 1;
                sy = rand.nextInt(this.height - 2) + 1;
            }
            do {
                dx = rand.nextInt(this.width - 2) + 1;
                dy = rand.nextInt(this.height - 2) + 1;
            } while (Math.abs(dx - sx) + Math.abs(dy - sy) < 8);
            generateForestPath(sx, sy, dx, dy, perlinsum);
            sx = dx;
            sy = dy;
        }
    }

    @Override
    protected String getName() {
        return "SurfaceLayer";
    }

    //Generate a path between the two points, preferring higher values in noise
    public void generateForestPath(int sx, int sy, int dx, int dy, double[][] noise) {
//        System.out.println("Generating path from (" + sx + "," + sy + ") to (" + dx + "," + dy + ")");
        Queue<Point> search = new PriorityQueue<>((o1, o2) -> {
            double o1value = Math.sqrt(Math.pow(dx - o1.x, 2) + Math.pow(dy - o1.y, 2)) + (noise[o1.x][o1.y] + 1) * 6;
            double o2value = Math.sqrt(Math.pow(dx - o2.x, 2) + Math.pow(dy - o2.y, 2)) + (noise[o2.x][o2.y] + 1) * 6;
            return Double.compare(o1value, o2value);
        });
        boolean[][] checked = new boolean[this.width][this.height];
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
            Point[] adjacent = this.getOrthoAdjacent(curr.x, curr.y);
            for (Point p : adjacent) {
                if (!checked[p.x][p.y]
                        && p.x > 1 && p.x < this.width - 1
                        && p.y > 1 && p.y < this.height - 1) {
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
            if (tiles[p.x][p.y] != MapTile.TOWN && tiles[p.x][p.y] != MapTile.FORESTFLOOR) {
                tiles[p.x][p.y] = MapTile.FORESTPATH;
            }
        }
    }

    @Override
    public boolean validateGeneration() {
        if (!this.hasGenerated) {
            return false;
        }

        //check for border cliffs
        for (int i = 0; i < this.width; i++) {
            if (this.tiles[i][0] != MapTile.TALLCLIFF
                    || this.tiles[i][this.height - 1] != MapTile.TALLCLIFF) {
                return false;
            }
        }
        for (int i = 0; i < this.height; i++) {
            if (this.tiles[0][i] != MapTile.TALLCLIFF
                    || this.tiles[this.width - 1][i] != MapTile.TALLCLIFF) {
                return false;
            }
        }

        //check for town at center
        int wCenter = this.width / 2 + (this.width + 1) % 2 - 1;
        int hCenter = this.width / 2 + (this.width + 1) % 2 - 1;
        if (this.tiles[wCenter][hCenter] != MapTile.TOWN) {
            return false;
        }

        int pathCount = 0;
        int accessibleCount = 0;
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                //check for empty tiles
                if (this.tiles[i][j] == MapTile.EMPTY) {
                    return false;
                }

                //check for pathtiles (should have at least some)
                if (this.tiles[i][j] == MapTile.FORESTPATH) {
                    pathCount++;
                }

                if (this.tiles[i][j].passable) {
                    accessibleCount++;
                }
            }
        }

        if (pathCount <= 0) {
            return false;
        }
        if (accessibleCount < 0.2 * this.width * this.height) {
            return false;
        }

        return true;
    }

    private void generateTown() {
        int wCenter = this.width / 2 + (this.width + 1) % 2 - 1;
        int hCenter = this.width / 2 + (this.width + 1) % 2 - 1;
        for (int i = 0; i < TOWNSIZE; i++) {
            for (int j = 0; j < TOWNSIZE; j++) {
                this.tiles[wCenter - TOWNSIZE/2 + i][hCenter - TOWNSIZE/2 + j] = MapTile.TOWN;
            }
        }
    }
}
