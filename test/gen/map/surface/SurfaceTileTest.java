package gen.map.surface;

import org.junit.Test;

import static org.junit.Assert.*;

public class SurfaceTileTest {
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
}