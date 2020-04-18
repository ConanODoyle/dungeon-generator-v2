package gen.lib;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

//Job: Understands the structure of a 2d array of perlin noise
public class NoiseGrid2D {
    public int height;
    double[][] values;
    public int width;
    double stepSize;
    double widthOffset, heightOffset;
    double minNoise, maxNoise;
    boolean valuesChanged = false;
    long seed;
    ImprovedNoise noiseObject;

    public NoiseGrid2D(int width, int height, double stepSize, long seed, double widthOffset, double heightOffset) {
        this.width = width;
        this.height = height;
        this.stepSize = stepSize;
        this.seed = seed;
        this.widthOffset = widthOffset;
        this.heightOffset = heightOffset;
        this.noiseObject = new ImprovedNoise(new Random(seed).nextLong());
        generateValues();
    }

    public NoiseGrid2D(int width, int height, double stepSize, long seed) {
        this(width, height, stepSize, seed, 0, 0);
    }

    private void generateValues() {
        values = noiseObject.generate2DNoise(width, height, stepSize, widthOffset, heightOffset);
        valuesChanged = true;
    }

    public void addWeightedNoise(NoiseGrid2D addedNoise, double thisWeight, double thatWeight) {
        values = PerlinUtils.getNoiseWeightedSum(values, thisWeight, addedNoise.values, thatWeight);
        valuesChanged = true;
    }

    private void getMinMaxNoiseValues() {
        double min = 0, max = 0;
        boolean firstValue = true;
        for (double[] value : values) {
            for (double v : value) {
                if (firstValue || v < min) {
                    min = v;
                }
                if (firstValue || v > max) {
                    max = v;
                }
                firstValue = false;
            }
        }
        minNoise = min;
        maxNoise = max;
        valuesChanged = false;
    }

    public double getNoiseValue(int i, int j) {
        return values[i][j];
    }

    public double getMaxNoiseValue() {
        if (valuesChanged) {
            getMinMaxNoiseValues();
        }
        return maxNoise;
    }

    public double getMinNoiseValue() {
        if (valuesChanged) {
            getMinMaxNoiseValues();
        }
        return minNoise;
    }



    //utility functions
    public int getLargestContiguousArea(double cutoff) {
        //use flood fill algorithm to determine largest group
        boolean[][] visited = new boolean[values.length][values[0].length];
        int largest = 0;
        int[] xmod = {0, 1, 0, -1};
        int[] ymod = {1, 0, -1, 0};
        ArrayList<Point> q = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                if (values[i][j] > cutoff && !visited[i][j]) {
                    int currsize = 0;
                    q.add(new Point(i, j));
                    visited[i][j] = true;
                    while (!q.isEmpty()) {
                        Point curr = q.remove(0);
                        currsize++;
                        for (int mod = 0; mod < xmod.length; mod++) {
                            int xadj = curr.x + xmod[mod];
                            int yadj = curr.y + ymod[mod];
                            if (xadj >= 0 && xadj < values.length
                                    && yadj >= 0 && yadj < values[0].length
                                    && values[xadj][yadj] > cutoff
                                    && !visited[xadj][yadj]) {
                                q.add(new Point(xadj, yadj));
                                visited[xadj][yadj] = true;
                            }
                        }
                    }

                    if (currsize > largest) {
                        largest = currsize;
                    }
                }
            }
        }
        return largest;
    }

    public int getLargestSquareArea(double cutoff) {
        //Similar to flood fill, but check for cubic shapes
        int largest = 0;
        int[] xmod = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] ymod = {1, 1, 0, -1, -1, -1, 0, 1};
        ArrayList<Point> q = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values.length; j++) {
                if (values[i][j] > cutoff) {
                    int currindex = 0;
                    boolean found = false;
                    q.add(new Point(i, j));
                    while (!found) {
                        Point curr = q.get(currindex);
                        currindex++;
                        for (int mod = 0; mod < xmod.length; mod++) {
                            int xadj = curr.x + xmod[mod];
                            int yadj = curr.y + ymod[mod];
                            if (xadj > 0 && xadj < values.length && yadj > 0 && yadj < values[0].length
                                    && values[xadj][yadj] > cutoff) {
                                if (!q.contains(new Point(xadj, yadj)))
                                    q.add(new Point(xadj, yadj));
                            } else {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (Math.floor(Math.sqrt(q.size())) > largest) {
                        largest = (int) Math.sqrt(q.size());
                    }
                    q.clear();
                }
            }
        }
        return largest;
    }

    public int getTotalCutoffArea(double cutoff) {
        int count = 0;
        for (double[] doubles : values) {
            for (double val : doubles) {
                if (val > cutoff) {
                    count++;
                }
            }
        }
        return count;
    }
}
