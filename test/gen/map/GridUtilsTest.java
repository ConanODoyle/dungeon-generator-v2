package gen.map;

import gen.lib.GridUtils;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class GridUtilsTest {

    @Test
    public void TestCorrectCompassDirections() {
        Point start = new Point(0, 0);
        assertEquals(GridUtils.NORTH, GridUtils.getCompassDirectionTo(start, new Point(0, 1)));
        assertEquals(GridUtils.EAST, GridUtils.getCompassDirectionTo(start, new Point(1, 0)));
        assertEquals(GridUtils.SOUTH, GridUtils.getCompassDirectionTo(start, new Point(0, -1)));
        assertEquals(GridUtils.WEST, GridUtils.getCompassDirectionTo(start, new Point(-1, 0)));

        start = new Point(10, -5);
        assertEquals(GridUtils.NORTH, GridUtils.getCompassDirectionTo(start, new Point(start.x, start.y+1)));
        assertEquals(GridUtils.EAST, GridUtils.getCompassDirectionTo(start, new Point(start.x+1, start.y)));
        assertEquals(GridUtils.SOUTH, GridUtils.getCompassDirectionTo(start, new Point(start.x, start.y-1)));
        assertEquals(GridUtils.WEST, GridUtils.getCompassDirectionTo(start, new Point(start.x-1, start.y)));
    }
}
