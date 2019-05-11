package gen.map.lib;

import java.awt.*;

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
}
