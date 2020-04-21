package gen.lib;

import java.awt.*;
import java.util.ArrayList;

//Job: Provides various useful utility functions for determining the properties between two points
public class GridUtils {
    public static final int NORTH = 1;
    public static final int EAST = 2;
    public static final int SOUTH = 3;
    public static final int WEST = 4;
    public static final int NONE = 0;

    public static int getCompassDirectionTo(Point start, Point end) {
        int xDiff = end.x - start.x;
        int yDiff = end.y - start.y;

        if (yDiff == 0) {
            if (xDiff > 0) {
                return EAST;
            } else if (xDiff < 0) {
                return WEST;
            }
        } else if (xDiff == 0) {
            if (yDiff > 0) {
                return NORTH;
            } else if (yDiff < 0) {
                return SOUTH;
            }
        }
        return NONE;
    }

    //assumes using array where 0 is N, 1 is NE, 2 is E, etc
    //returns -1 if not adjacent
    public static int getAdjacentDirectionTo(Point start, Point end) {
        int xDiff = end.x - start.x;
        int yDiff = end.y - start.y;

        if (Math.abs(xDiff) > 1 || Math.abs(yDiff) > 1) {
            return -1;
        }

        if (yDiff == 0) {
            if (xDiff > 0) {
                return 2;
            } else if (xDiff < 0) {
                return 6;
            }
        } else if (xDiff == 0) {
            if (yDiff > 0) {
                return 0;
            } else if (yDiff < 0) {
                return 4;
            }
        } else {
            if (yDiff > 0) {
                if (xDiff > 0)
                    return 1;
                else
                    return 7;
            } else {
                if (xDiff > 0)
                    return 3;
                else
                    return 5;
            }
        }
        return -1;
    }

    public static ArrayList<Point> getRectanglePoints(int x, int y, int width, int height) {
        ArrayList<Point> result = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height;j ++) {
                int currX = x + i;
                int currY = y + j;
                result.add(new Point(currX, currY));
            }
        }

        return result;
    }
}
