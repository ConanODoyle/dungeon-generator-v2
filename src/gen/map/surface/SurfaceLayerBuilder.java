package gen.map.surface;

import gen.map.export.BlsBuilder;
import gen.map.MapTile;

import java.awt.*;
import java.util.ArrayList;

//Job: Understands how to convert a SurfaceLayer into a set of bricks
public class SurfaceLayerBuilder extends BlsBuilder {

    //Job: Understands a 4-sided shape on a grid
    private class Rectangle {
        private Point leftCorner; //top left corner
        private int width;
        private int height;

        private Rectangle(int width, int height, Point corner) {
            this.width = width;
            this.height = height;
            this.leftCorner = new Point(corner);
        }
    }

    private Point offset;
    private SurfaceLayer layer;
    private MapTile[][] copy;
    public static final double STARTING_HEIGHT = 100;

    private ArrayList<Rectangle> treeCover = new ArrayList<>();
    private ArrayList<Rectangle> cliffCover = new ArrayList<>();

    public SurfaceLayerBuilder(SurfaceLayer layer) {
        this.layer = layer;
        this.copy = layer.getTiles();
        this.offset = new Point(0, 0);
    }

    public void generateBuild() {
        //optimize tree and cliff coverage
        calculateOptimumTreeCover();
        calculateOptimumCliffCover();

        //
    }

    @Override
    public String nextLine() {
        return null;
    }

    private void calculateOptimumCliffCover() {
        int[] sizes = {4, 3, 2, 1};
        boolean[][] collected = new boolean[copy.length][copy[0].length];

        int currSize;
        ArrayList<Point> curr = new ArrayList<>();
        for (int size : sizes) {
            currSize = size;
            System.out.println("Calculating cliff coverage: size " + currSize);
            for (int i = 0; i < copy.length; i++) {
                for (int j = 0; j < copy[i].length; j++) {
                    if (copy[i][j] == SurfaceTile.CLIFF && !collected[i][j]) {
                        ArrayList<Point> rect = getRectanglePoints(i, j, currSize, currSize);
                        if (rect == null) {
                            continue;
                        }
                        for (Point p : rect) {
                            if (copy[p.x][p.y] != SurfaceTile.CLIFF || collected[p.x][p.y]) {
                                curr.clear();
                                break;
                            } else {
                                curr.add(p);
                            }
                        }

                        if (curr.size() > 0) {
                            cliffCover.add(new Rectangle(currSize, currSize, curr.get(0)));
                            for (Point p : curr) {
                                collected[p.x][p.y] = true;
                            }
                            curr.clear();
                        }
                    }
                }
            }
        }
    }

    private void calculateOptimumTreeCover() {
        int[] sizes = {4, 3, 2, 1};
        boolean[][] collected = new boolean[copy.length][copy[0].length];

        int currSize;
        ArrayList<Point> curr = new ArrayList<>();
        for (int size : sizes) {
            currSize = size;
            System.out.println("Calculating tree coverage: size " + currSize);
            for (int i = 0; i < copy.length; i++) {
                for (int j = 0; j < copy[i].length; j++) {
                    if (copy[i][j] == SurfaceTile.FOREST && !collected[i][j]) {
                        ArrayList<Point> rect = getRectanglePoints(i, j, currSize, currSize);
                        if (rect == null) {
                            continue;
                        }
                        for (Point p : rect) {
                            if (copy[p.x][p.y] != SurfaceTile.FOREST || collected[p.x][p.y]) {
                                curr.clear();
                                break;
                            } else {
                                curr.add(p);
                            }
                        }

                        if (curr.size() > 0) {
                            treeCover.add(new Rectangle(currSize, currSize, curr.get(0)));
                            for (Point p : curr) {
                                collected[p.x][p.y] = true;
                            }
                            curr.clear();
                        }
                    }
                }
            }
        }
    }

    private ArrayList<Point> getRectanglePoints(int x, int y, int width, int height) {
        ArrayList<Point> result = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height;j ++) {
                int currX = x + i;
                int currY = y + j;
                if (currX >= copy.length || currY >= copy[0].length) {
                    return null;
                } else {
                    result.add(new Point(currX, currY));
                }
            }
        }

        return result;
    }
}