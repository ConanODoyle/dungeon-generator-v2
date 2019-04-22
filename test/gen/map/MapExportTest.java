package gen.map;

import gen.map.export.BlsBuilder;
import gen.map.export.BlsFileExport;
import gen.map.lib.PeekableScanner;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapExportTest {

    @Test
    public void PeekableScannerTest() {
        String input = "The quick brown fox\n" +
                "jumped over the lazy dog.\n";
        PeekableScanner p = new PeekableScanner(input);

        assertEquals("The quick brown fox", p.peek());
        assertEquals("The quick brown fox", p.nextLine());
        assertEquals("jumped over the lazy dog.", p.peek());
        assertEquals("jumped over the lazy dog.", p.nextLine());
    }

    @Test
    public void BlsFileHeaderTest() {
        BlsFileExport bls = new BlsFileExport();
        bls.addToDescription("Description Line 1");
        bls.addToDescription("Author: Conan");

        bls.setColorset("colorSet.txt");

        String header = bls.exportBlsHeader();
        assertEquals(
                "This is a Blockland save file.  You probably shouldn't modify it cause you'll mess it up.\n" +
                        "2\n" +
                        "Description Line 1\n" +
                        "Author: Conan\n" +
                        "1.000000 0.000000 0.000000 1.000000\n" +
                        "1.000000 0.498039 0.000000 1.000000\n" +
                        "1.000000 1.000000 0.000000 1.000000\n" +
                        "0.784314 0.000000 0.000000 1.000000\n" +
                        "0.184314 0.517647 0.078431 1.000000\n" +
                        "0.000000 1.000000 1.000000 1.000000\n" +
                        "0.105882 0.458824 0.768627 1.000000\n" +
                        "0.000000 0.168627 0.509804 1.000000\n" +
                        "1.000000 1.000000 1.000000 0.549020\n" +
                        "1.000000 0.000000 0.000000 0.698039\n" +
                        "1.000000 0.498039 0.000000 0.698039\n" +
                        "1.000000 1.000000 0.000000 0.698039\n" +
                        "0.486275 0.988235 0.000000 0.698039\n" +
                        "0.184314 0.517647 0.078431 0.698039\n" +
                        "0.529412 0.807843 0.980392 0.698039\n" +
                        "0.117647 0.564706 1.000000 0.698039\n" +
                        "0.000000 0.000000 1.000000 0.698039\n" +
                        "1.000000 0.901961 0.980392 0.007843\n" +
                        "1.000000 0.603922 0.423529 1.000000\n" +
                        "1.000000 0.878431 0.611765 1.000000\n" +
                        "0.847059 0.768627 0.580392 1.000000\n" +
                        "0.784314 0.921569 0.490196 1.000000\n" +
                        "0.541176 0.698039 0.552941 1.000000\n" +
                        "0.560784 0.929412 0.960784 1.000000\n" +
                        "0.545098 0.352941 0.000000 1.000000\n" +
                        "1.000000 0.843137 0.388235 1.000000\n" +
                        "0.854902 0.647059 0.125490 1.000000\n" +
                        "0.682353 0.137255 0.137255 1.000000\n" +
                        "0.501961 0.000000 0.000000 1.000000\n" +
                        "0.439216 0.501961 0.564706 1.000000\n" +
                        "0.239216 0.349020 0.670588 1.000000\n" +
                        "0.000000 0.141176 0.333333 1.000000\n" +
                        "0.333333 0.419608 0.184314 1.000000\n" +
                        "0.133333 0.443137 0.152941 1.000000\n" +
                        "0.133333 0.270588 0.152941 1.000000\n" +
                        "0.098039 0.200000 0.152941 1.000000\n" +
                        "0.176471 0.129412 0.078431 1.000000\n" +
                        "0.266667 0.196078 0.117647 1.000000\n" +
                        "1.000000 0.000000 0.501961 1.000000\n" +
                        "0.294118 0.000000 0.509804 1.000000\n" +
                        "0.376471 0.317647 0.309804 1.000000\n" +
                        "0.396078 0.352941 0.305882 1.000000\n" +
                        "0.545098 0.490196 0.419608 1.000000\n" +
                        "0.388235 0.364706 0.341176 1.000000\n" +
                        "0.466667 0.447059 0.419608 1.000000\n" +
                        "0.972549 0.956863 0.976471 1.000000\n" +
                        "0.843137 0.831373 0.847059 1.000000\n" +
                        "0.733333 0.721569 0.737255 1.000000\n" +
                        "0.643137 0.635294 0.647059 1.000000\n" +
                        "0.533333 0.525490 0.537255 1.000000\n" +
                        "0.415686 0.407843 0.419608 1.000000\n" +
                        "0.337255 0.329412 0.341176 1.000000\n" +
                        "0.258824 0.250980 0.262745 1.000000\n" +
                        "0.082353 0.113725 0.105882 1.000000\n" +
                        "0.803922 0.666667 0.490196 1.000000\n" +
                        "0.745098 0.588235 0.360784 1.000000\n" +
                        "0.729412 0.541176 0.262745 1.000000\n" +
                        "0.627451 0.462745 0.231373 1.000000\n" +
                        "0.533333 0.396078 0.192157 1.000000\n" +
                        "0.454902 0.317647 0.113725 1.000000\n" +
                        "0.317647 0.239216 0.117647 1.000000\n" +
                        "0.478431 0.239216 0.000000 1.000000\n" +
                        "0.392157 0.196078 0.000000 1.000000\n" +
                        "1.000000 0.000000 1.000000 0.000000\n",
                header);
    }

    @Test
    public void BlsFileExportTest() {
        BlsFileExport bls = new BlsFileExport();

        bls.setColorset("colorSet.txt");

        try {
            bls.exportBlsFile("testExport.bls", new BlsBuilder.TestBuilder());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception in generating file");
        }

        File test = new File("testExport.bls");
        File expected = new File("testExport_compare.bls");
        Scanner testScanner = null;
        Scanner expectedScanner = null;
        try {
            testScanner = new Scanner(test);
            expectedScanner = new Scanner(expected);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Scanners could not be created");
        }
        assertTrue(new File("testExport.bls").exists());

        while (testScanner.hasNext()) {
            assertEquals(testScanner.nextLine(), expectedScanner.nextLine());
        }
    }
}
