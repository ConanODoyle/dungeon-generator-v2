package gen.lib;

//Job: Understands how to generate perlin noise
//Taken from https://mrl.nyu.edu/~perlin/noise/, with minor reformatting and additions (seeding, array generation)

// JAVA REFERENCE IMPLEMENTATION OF IMPROVED NOISE - COPYRIGHT 2002 KEN PERLIN.

import java.util.*;

public final class ImprovedNoise {
    private final int[] p = new int[512];
    private boolean hasPermutation = false;
    public long seed;

    public ImprovedNoise() {
        this.seed = new Random().nextLong();
    }

    public ImprovedNoise(long seed) {
        this.setSeed(seed);
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public double noise(double x, double y, double z) {
        if (!this.hasPermutation) {
            this.generatePermutation();
            this.hasPermutation = true;
        }
        int X = (int) Math.floor(x) & 255;                               // FIND UNIT CUBE THAT
        int Y = (int) Math.floor(y) & 255;                               // CONTAINS POINT.
        int Z = (int) Math.floor(z) & 255;
        x -= Math.floor(x);                                             // FIND RELATIVE X,Y,Z
        y -= Math.floor(y);                                             // OF POINT IN CUBE.
        z -= Math.floor(z);

        double u = fade(x);                                             // COMPUTE FADE CURVES
        double v = fade(y);                                             // FOR EACH OF X,Y,Z.
        double w = fade(z);
        int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z,                   // HASH COORDINATES OF
                B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;               // THE 8 CUBE CORNERS,

        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),   // AND ADD BLENDED RESULTS
                grad(p[BA], x - 1, y, z)),                      // FROM  8 CORNERS OF CUBE
                lerp(u, grad(p[AB], x, y - 1, z),
                        grad(p[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                        grad(p[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                                grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }

    public double[][] generate2DNoise(int width, int height, double step, double xoffset, double yoffset) {
        double[][] result = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = this.noise(step * i + xoffset, step * j + yoffset, 0);
            }
        }
        return result;
    }

    public double[][] generate2DNoise(int width, int height, double step) {
        return generate2DNoise(width, height, step, 0, 0);
    }

    public double[][][] generate3DNoise(int depth, int width, int height, double step, double zStep, double xoffset, double yoffset) {
        double[][][] result = new double[depth][width][height];
        for (int z = 0; z < depth; z++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    result[z][i][j] = this.noise(step * i + xoffset, step * j + yoffset, zStep * z);
                }
            }
        }
        return result;
    }

    public double[][][] generate3DNoise(int depth, int width, int height, double step, double zStep) {
        return generate3DNoise(depth, width, height, step, zStep, 0, 0);
    }

    private void generatePermutation() {
        List<Integer> permutation = new ArrayList<>();
        for (int i = 0; i < 255; i++) {
            permutation.add(i);
        }

        Collections.shuffle(permutation, new Random(this.seed));
        Iterator<Integer> iter = permutation.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            this.p[i] = iter.next();
            this.p[i+255] = this.p[i];
        }
    }

    //sinusoidal interpolation function
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        if (Math.abs(z - 0) < 0.0000001) {
            return grad(hash, x, y);
        }

        // 4 bits, 0-3 representing place value 0
        // top two bits:
        // 00[xx]: u=+-x, v=+-y
        // 10[xx]: u=+-y, v=+-z
        // 01[xx]: u=+-x, v=+-z
        //
        //i dont understand the following cases wtf??
        //do they just never happen?
        // 11[00]: u=y, v=x
        // 11[01]: u=-y, v=z
        // 11[10]: u=y, v=-x
        // 11[11]: u=-y, v=-z
        // return:
        // bit 1 == 0       ? u=[x,y] : u=[-x,-y]
        // bit 2 & 1 == 0   ? v=[y]   : v=[-x,-z]
        // if 00, guarantees v = y AND u = [x,y]
        // if 01, guarantees v = y AND u = [-x,-y]
        int h = hash & 15;                      // CONVERT LO 4 BITS OF HASH CODE
        double u = h<8 ? x : y,                 // INTO 12 GRADIENT DIRECTIONS.
                v = h<4 ? y : h==12||h==14 ? x : z;
        return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
    }

    private static double grad(int hash, double x, double y) {
        double scale = hash > 127 ? 1 : 1;
        switch (hash & 7) {
            case 0: return scale * (y);
            case 1: return scale * (x + y);
            case 2: return scale * (x);
            case 3: return scale * (x - y);
            case 4: return scale * (-y);
            case 5: return scale * (-x - y);
            case 6: return scale * (-x);
            case 7: return scale * (-x + y);
            default: return 0;
        }
    }
}