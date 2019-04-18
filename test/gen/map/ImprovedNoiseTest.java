package gen.map;

import gen.map.perlin.ImprovedNoise;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import gen.map.perlin.PerlinUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
public class ImprovedNoiseTest {

    private static final int PIXELSIZE = 15;

    @Test
    public void generatePerlinNoise() {
        double step = 0.05;
        int numsteps = 150;
        ImprovedNoise noise = new ImprovedNoise();
        ImprovedNoise alt = new ImprovedNoise(noise.seed);

        double min = 0, max = 0, curr;
        for (double i = 0; i < numsteps; i++) {
            for (double j = 0; j < numsteps; j++) {
                curr = noise.noise(step * i, step * j, 0);
                min = curr < min ? curr : min;
                max = curr > max ? curr : max;
                assertTrue(Math.abs(curr -
                        alt.noise(step * i, step * j, 0)) < Math.pow(10, -5));
            }
        }
        System.out.println("Min:" + min + " Max:" + max);


        double[][] perlin = noise.generate2DNoise(numsteps, numsteps, step);
        PerlinUtils.exportPerlin(perlin, 10);
    }

    @Test
    public void PerlinUtilsReturnCorrectValues() {
        double[][] TestArray = {
                {   0.1,   0.1,   0.1,   0.5,  -0.1,   0.1},
                {  -0.1,  -0.1,   0.3,   0.4,   0.2,   0.1},
                {  -0.5,  -0.4,   0.5,   0.5,   0.3,   0.2},
                {   0.5,   0.5,   0.2,   0.3,   0.2,   0.0}
        };

        assertEquals(18, PerlinUtils.getLargestContiguousArea(TestArray, 0.09));
        assertEquals(13, PerlinUtils.getLargestContiguousArea(TestArray, 0.19));
        assertEquals(3, PerlinUtils.getLargestSquareArea(TestArray, 0.19));
        assertEquals(2, PerlinUtils.getLargestSquareArea(TestArray, 0.29));

        double[][] sum = new double[4][6];
        double[][] psum = PerlinUtils.getNoiseWeightedSum(TestArray, 1, TestArray, 0.3);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                sum[i][j] = (TestArray[i][j] * 1 + TestArray[i][j] * 0.3) / (1 + 0.3);
                assertTrue(Math.abs(sum[i][j] - psum[i][j]) < 0.001);
            }
        }

        assertEquals(19, PerlinUtils.getTotalCutoffArea(TestArray, -0.001));
    }

    @Test
    public void generateTwoLayerPerlinNoise() {
        double bigstep = 0.024;
        double smallstep = 0.24;//03 / 2;
        int numsteps = 30 * 2;
        double bigmul = 1;
        double smallmul = 5;
        int squareFieldRequirement = 4;

        ImprovedNoise big = new ImprovedNoise();//1297618227837464957L);
        ImprovedNoise small = new ImprovedNoise();//1297618227837464957L);

        double[][] perlinbig = big.generate2DNoise(numsteps, numsteps, bigstep);
        double[][] perlinsmall = small.generate2DNoise(numsteps, numsteps, smallstep, 0.12, 0.12);
        double[][] perlinsum = PerlinUtils.getNoiseWeightedSum(perlinbig, bigmul, perlinsmall, smallmul);

        double min = 0, max = 0, curr;
        for (int i = 0; i < numsteps; i += 1) {
            for (int j = 0; j < numsteps; j+= 1) {
                curr = perlinsum[i][j];
                min = curr < min ? curr : min;
                max = curr > max ? curr : max;
            }
        }
        System.out.println("Min:" + min + " Max:" + max);
        System.out.println("bigseed:" + big.seed);
        System.out.println("smallseed:" + small.seed);

        PerlinUtils.exportPerlin(perlinsum, 127);
        double ratio = 0.9;
        double cutoff;
        do {
            cutoff = ratio * (max - min) + min;
            ratio -= 0.025;
        } while (PerlinUtils.getLargestSquareArea(perlinsum, cutoff) < squareFieldRequirement);
        System.out.println("--------");
        System.out.println("Cutoff:" + cutoff);
        System.out.println("Largest field:" + PerlinUtils.getLargestContiguousArea(perlinsum, cutoff));
        System.out.println("Largest square field:" + PerlinUtils.getLargestSquareArea(perlinsum, cutoff));
        PerlinUtils.exportBicolorPerlin(perlinsum, cutoff);
    }
}
