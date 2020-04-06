package gen.map;

import gen.map.surface.SurfaceTile;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapTileTest {
    @Test
    public void ObjectEqualityTest() {
        SurfaceTile curr;

        curr = SurfaceTile.ForestFloor();
        assertEquals(curr, SurfaceTile.ForestFloor());
        assertNotEquals(curr, SurfaceTile.GoblinCamp());

        curr = SurfaceTile.Cave();
        assertEquals(curr, SurfaceTile.Cave());
        assertNotEquals(curr, SurfaceTile.Glen());

        curr = SurfaceTile.Town();
        assertEquals(curr, SurfaceTile.Town());
        assertNotEquals(curr, SurfaceTile.Settlement());

        assertEquals(curr, curr);
    }

    @Test
    public void EmptyObjectTest() {
        MapTile curr = null;

        assertTrue(MapTile.isEmpty(curr));
        curr = MapTile.EMPTY;
        assertTrue(MapTile.isEmpty(curr));
    }
}