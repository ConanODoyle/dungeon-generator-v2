package gen.map.cave;

import gen.export.MapLayerBuilder;
import gen.map.MapLayer;
import gen.map.MapTile;
import gen.lib.ImprovedNoise;
import gen.lib.PerlinUtils;

import java.util.Random;

public class CaveLayer extends MapLayer {
    public MapTile[][][] caveTiles;
    private static int DEPTH = 5;

    public CaveLayer(int width, int height) {
        super(width, height);
        caveTiles = new MapTile[DEPTH][width][height];
        for (int i = 0; i < DEPTH; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < height; k++) {
                    caveTiles[i][j][k] = MapTile.EMPTY;
                }
            }
        }
    }

    @Override
    public void generate() {

    }


    private void generateCaverns(Random rand) {
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
                    tiles[i][j] = CaveTile.LEVEL1CAVE;
                } else {
                    tiles[i][j] = CaveTile.ROCK;
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
