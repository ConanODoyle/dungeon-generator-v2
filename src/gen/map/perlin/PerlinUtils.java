package gen.map.perlin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//Job: Understands how to do operations on perlin noise arrays
public class PerlinUtils {
    private static final int PIXELSIZE = 10;

    public static double[][] getNoiseWeightedSum(double[][] array1, double weight1, double[][] array2, double weight2) {
        if (array1.length != array2.length || array1[0].length != array2[0].length) {
            throw new RuntimeException("Cannot add noise arrays of different dimensions");
        }

        double[][] result = new double[array1.length][array1[0].length];
        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array1[0].length; j++) {
                result[i][j] = (array1[i][j] * weight1 + array2[i][j] * weight2) / (weight1 + weight2);
            }
        }
        return result;
    }

    public static int getLargestContiguousArea(double[][] perlin, double cutoff) {
        //use flood fill algorithm to determine largest group
        boolean[][] visited = new boolean[perlin.length][perlin[0].length];
        int largest = 0;
        int[] xmod = {0, 1, 0, -1};
        int[] ymod = {1, 0, -1, 0};
        ArrayList<Point> q = new ArrayList<>();

        for (int i = 0; i < perlin.length; i++) {
            for (int j = 0; j < perlin[0].length; j++) {
                if (perlin[i][j] > cutoff && !visited[i][j]) {
                    int currsize = 0;
                    q.add(new Point(i, j));
                    visited[i][j] = true;
                    while (!q.isEmpty()) {
                        Point curr = q.remove(0);
                        currsize++;
                        for (int mod = 0; mod < xmod.length; mod++) {
                            int xadj = curr.x + xmod[mod];
                            int yadj = curr.y + ymod[mod];
                            if (xadj >= 0 && xadj < perlin.length
                                    && yadj >= 0 && yadj < perlin[0].length
                                    && perlin[xadj][yadj] > cutoff
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

    public static int getLargestSquareArea(double[][] perlin, double cutoff) {
        //Similar to flood fill, but check for cubic shapes
        int largest = 0;
        int[] xmod = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] ymod = {1, 1, 0, -1, -1, -1, 0, 1};
        ArrayList<Point> q = new ArrayList<>();

        for (int i = 0; i < perlin.length; i++) {
            for (int j = 0; j < perlin.length; j++) {
                if (perlin[i][j] > cutoff) {
                    int currindex = 0;
                    boolean found = false;
                    q.add(new Point(i, j));
                    while (!found) {
                        Point curr = q.get(currindex);
                        currindex++;
                        for (int mod = 0; mod < xmod.length; mod++) {
                            int xadj = curr.x + xmod[mod];
                            int yadj = curr.y + ymod[mod];
                            if (xadj > 0 && xadj < perlin.length && yadj > 0 && yadj < perlin[0].length
                                    && perlin[xadj][yadj] > cutoff) {
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

    public static int getTotalCutoffArea(double[][] perlin, double cutoff) {
        int count = 0;
        for (double[] doubles : perlin) {
            for (double val : doubles) {
                if (val > cutoff) {
                    count++;
                }
            }
        }
        return count;
    }

    public static void exportBicolorPerlin(double[][] perlin, double cutoff) {
        BufferedImage img = new BufferedImage(perlin.length * PIXELSIZE, perlin[0].length * PIXELSIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, perlin.length * PIXELSIZE, perlin[0].length * PIXELSIZE);
        for (int i = 0; i < perlin.length; i++) {
            for (int j = 0; j < perlin[i].length; j++) {
                double val = perlin[i][j];
                if (val > cutoff) {
                    g2d.setColor(Color.LIGHT_GRAY);
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.fillRect(j * PIXELSIZE, i * PIXELSIZE, PIXELSIZE, PIXELSIZE);
                g2d.setColor(Color.ORANGE);
                g2d.drawRect(j * PIXELSIZE, i * PIXELSIZE, PIXELSIZE, PIXELSIZE);
            }
        }

        File imgfile = new File("perlinbi.png");
        try {
            ImageIO.write(img, "png", imgfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportPerlin(double[][] perlin, int numColors) {
        BufferedImage img = new BufferedImage(perlin.length * PIXELSIZE, perlin[0].length * PIXELSIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, perlin.length * PIXELSIZE, perlin[0].length * PIXELSIZE);
        for (int i = 0; i < perlin.length; i++) {
            for (int j = 0; j < perlin[i].length; j++) {
                int val = (int) ((perlin[i][j] + 1.0) / 2.0 * numColors);
                val = (int) Math.floor(255.0 / numColors * val); //distribute colors
                g2d.setColor(new Color(val, val, val));
                g2d.fillRect(j * PIXELSIZE, i * PIXELSIZE, PIXELSIZE, PIXELSIZE);
                g2d.setColor(Color.ORANGE);
                g2d.drawRect(j * PIXELSIZE, i * PIXELSIZE, PIXELSIZE, PIXELSIZE);
            }
        }

        File imgfile = new File("perlin1.png");
        try {
            ImageIO.write(img, "png", imgfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportPerlin(double[][] perlin) {
        exportPerlin(perlin, 255);
    }
}
