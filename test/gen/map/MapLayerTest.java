package gen.map;

import org.junit.Test;

import static org.junit.Assert.*;

public class MapLayerTest {

    @Test
    public void MapTileDataStructureTest() {
        MapTile t = new MapTile();
        assertFalse(t.isOccupied());
        t.setOccupied(true);
        assertTrue(t.isOccupied());
    }

    @Test
    public void MapLayerInitializationTest() {
        MapLayer l = new MapLayer();
        l.generateMap(null);
        assertEquals(l, new MapLayer());
    }
}