package gen.map;

import gen.map.export.MapLayerExport;
import gen.map.surface.SurfaceLayer;
import gen.map.surface.SurfaceTile;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapLayerTest {

    @Test
    public void MapLayerInitializationTest() {
        MapLayer l = new SurfaceLayer(40, 40);
        assertEquals(l, new SurfaceLayer(40, 40));
    }

    @Test
    public void MapLayerRenderTest() {
        var width = 20;
        var height = 20;
        MapLayer l = new SurfaceLayer(width, height);

        var render = MapLayerExport.exportAsStringArray(l);
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
        long seed = r.nextLong();
//        long seed = -8459489275569364453L;
        System.out.println("Using seed " + seed + " for generation...\n");
        l.seed = seed;
        l.generate();

        assertTrue(l.validateGeneration());

        MapLayerExport.exportAsImage(l, "SurfaceLayer", 10, 0);
    }

    @Test
    public void SurfaceLayerRemoveInaccessibleAreaTest() {
        var width = 20;
        var height = 20;
        SurfaceLayer l = new SurfaceLayer(width, height);
        var X = SurfaceTile.FOREST;
        var o = SurfaceTile.FORESTFLOOR;

        MapTile[][] src = new MapTile[][]{
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, o, o, o, o, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, o, o, o, o, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, o, o, o, o, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, o, o, o, o, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
                {X, X, o, X, X, X, X, X, X, o, o, o, X, X, X, X, X, X, X, X},
        };
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                l.tiles[i][j] = src[i][j];
            }
        }

        var removed = l.removeInaccessibleAreas(10, 10, X);
//        for (String[] a : MapLayerExport.exportAsStringArray(l)) {
//            System.out.println(Arrays.toString(a));
//        }
        assertEquals(20 + 16, removed);
    }
}