package gen.map;

import gen.map.export.BlsBrick;
import gen.map.parser.BlsOctree;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class BlsOctreeTest {

    @Test
    public void OctreeBoxSearchTest() {
        BlsOctree o = new BlsOctree();
        for (int i = 0; i < 32; i++) {
            o.add(new BlsBrick("Test Brick\"", 0.5, 0.5, 0.2, 0, ""));
        }
        assertEquals(0, o.containerBoxSearch(new double[]{0, 0, 0}, new double[]{0.1, 0.1, 0.1}).size());
        assertEquals(0, o.containerBoxSearch(new double[]{0, 0, 0}, new double[]{0.5, 0.5, 0.2}).size());
        assertEquals(32, o.containerBoxSearch(new double[]{0, 0, 0}, new double[]{1, 1, 0.4}).size());

        for (int i = 0; i < 33; i++) {
            o.add(new BlsBrick("Test Brick\"", i * 0.5, 3, 0.2, 0, ""));
        }
        ArrayList output = o.containerBoxSearch(new double[]{8, 3, 0.2}, new double[]{16, 0.1, 0.1});
        assertEquals(33, output.size());
        assertEquals(17, o.containerBoxSearch(new double[]{0, 3, 0.2}, new double[]{16, 0.1, 0.1}).size());
    }
}
