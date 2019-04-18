package gen.map;

import gen.map.surface.SurfaceLayer;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class MapLayerTest {

    @Test
    public void MapLayerInitializationTest() {
        MapLayer l = new SurfaceLayer(10, 10);
        assertEquals(l, new SurfaceLayer(10, 10));
    }

    @Test
    public void MapLayerRenderTest() {
        var width = 10;
        var height = 10;
        MapLayer l = new SurfaceLayer(width, height);

        var render = l.render();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //Default no-tile-present should print #
                assertEquals("#", render[width - i - 1][height - j - 1]);
            }
        }
    }

    @Test
    public void SurfaceLayerGenerationTest() {
        var width = 160;
        var height = 160;
        MapLayer l = new SurfaceLayer(width, height);

        Random r = new Random();
        long seed = r.nextLong();//1920513116741714457L;
        System.out.println("Using seed " + seed + " for generation...\n");
        l.setSeed(seed);
        l.generate();

        assertTrue(l.validateGeneration());
    }

    @Test
    public void SurfaceLayerRemoveInaccessibleAreaTest() {
        var width = 12;
        var height = 12;
        SurfaceLayer l = new SurfaceLayer(width, height);
        var X = MapTile.FOREST;
        var o = MapTile.FORESTFLOOR;

        MapTile[][] src = new MapTile[][]{
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, o, o, o, o, X, o, o, o},
                {X, X, o, X, o, o, o, o, X, o, o, o},
                {X, X, o, X, o, o, o, o, X, o, o, o},
                {X, X, o, X, o, o, o, o, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
                {X, X, o, X, X, X, X, X, X, o, o, o},
        };
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                l.tiles[i][j] = src[i][j];
            }
        }

        var removed = l.removeInaccessibleAreas();
        assertEquals(12 * 4, removed);
    }
}