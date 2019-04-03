package gen.map;

import gen.map.surface.SurfaceLayer;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class MapLayerTest {

    @Test
    public void MapLayerInitializationTest() {
        MapLayer l = new SurfaceLayer(0, 0);
        assertEquals(l, new SurfaceLayer(0, 0));
    }

    @Test
    public void MapLayerRenderTest() {
        var width = 5;
        var height = 10;
        MapLayer l = new SurfaceLayer(width, height);

        l.printRender();

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
        var width = 30;
        var height = 30;
        MapLayer l = new SurfaceLayer(width, height);

        Random r = new Random();
        long seed = r.nextLong();
        l.setSeed(seed);
        l.generate();

        String validString = l.getTileString();

        l.printRender();

        var render = l.render();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertTrue(validString.contains(render[width - i - 1][height - j - 1]));
            }
        }

    }
}