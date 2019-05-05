package gen.map;

import gen.map.export.BlsFileExport;
import gen.map.export.MapLayerBuilder;
import gen.map.surface.SurfaceLayer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class LayerBuilderTest {

    @Test
    public void SurfaceLayerBuilderTest() {
        SurfaceLayer m = new SurfaceLayer(180, 180);
        m.seed = 6078492303428244842L;
        m.generate();

        MapLayerBuilder builder = m.getBuilder();
        builder.generateBuild();

        BlsFileExport exporter = new BlsFileExport();

        exporter.setColorset("colorSet.txt");
        try {
            exporter.exportBlsFile("testSurfaceLayerMap.bls", builder);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Could not create file");
        }
    }
}
