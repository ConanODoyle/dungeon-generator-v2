package gen.map;

import gen.Main;
import gen.export.BlsFileExport;
import gen.export.MapLayerBuilder;
import gen.export.MapLayerExport;
import gen.map.surface.SurfaceLayer;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class LayerBuilderTest {

    @Test
    public void SurfaceLayerBuilderTest() {
        SurfaceLayer m = new SurfaceLayer(180, 180);
        m.seed = 6511287661274410794L;
        System.out.println("Seed: " + m.seed);
        m.generate();

        MapLayerBuilder builder = m.getBuilder();
        builder.generateBuild();

        BlsFileExport exporter = new BlsFileExport();

        exporter.setColorset(Main.tilesetPath);
        try {
            exporter.exportBlsFile("testSurfaceLayerMap.bls", builder);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Could not create file");
        }
        MapLayerExport.exportAsImage(m, "testSurfaceLayerMap", 10, 0);
    }

    @Test
    public void ArraylistContainsTest() {
        ArrayList<Point> test = new ArrayList<>();
        test.add(new Point(10, 20));
        assertTrue(test.contains(new Point(10, 20)));
    }
}
