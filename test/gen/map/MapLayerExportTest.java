package gen.map;

import gen.export.MapLayerBuilder;
import gen.export.BlsFileExport;
import gen.lib.PeekableScanner;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapLayerExportTest {

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
                        "1.000000 0.141176 0.000000 1.000000\n" +
                        "1.000000 0.498039 0.000000 1.000000\n" +
                        "1.000000 1.000000 0.000000 1.000000\n" +
                        "0.000000 0.784314 0.000000 1.000000\n" +
                        "0.000000 1.000000 1.000000 1.000000\n" +
                        "0.000000 0.498039 1.000000 1.000000\n" +
                        "0.000000 0.000000 1.000000 1.000000\n" +
                        "1.000000 1.000000 1.000000 0.250980\n" +
                        "1.000000 0.000000 0.000000 0.698039\n" +
                        "1.000000 0.141176 0.000000 0.698039\n" +
                        "1.000000 0.498039 0.000000 0.698039\n" +
                        "1.000000 1.000000 0.000000 0.698039\n" +
                        "0.486275 0.988235 0.000000 0.698039\n" +
                        "0.529412 0.807843 0.980392 0.698039\n" +
                        "0.117647 0.564706 1.000000 0.698039\n" +
                        "0.000000 0.000000 1.000000 0.698039\n" +
                        "1.000000 0.901961 0.980392 0.019608\n" +
                        "0.956863 0.878431 0.784314 1.000000\n" +
                        "0.784314 0.921569 0.490196 1.000000\n" +
                        "0.541176 0.698039 0.552941 1.000000\n" +
                        "0.439216 0.501961 0.564706 1.000000\n" +
                        "0.698039 0.662745 0.905882 1.000000\n" +
                        "0.878431 0.560784 0.956863 1.000000\n" +
                        "0.925490 0.513725 0.678431 1.000000\n" +
                        "1.000000 0.603922 0.423529 1.000000\n" +
                        "1.000000 0.843137 0.388235 1.000000\n" +
                        "0.705882 0.133333 0.133333 1.000000\n" +
                        "0.501961 0.000000 0.000000 1.000000\n" +
                        "0.745098 0.745098 0.882353 1.000000\n" +
                        "0.239216 0.349020 0.670588 1.000000\n" +
                        "0.000000 0.141176 0.333333 1.000000\n" +
                        "0.000000 0.505882 0.231373 1.000000\n" +
                        "0.431373 0.545098 0.239216 1.000000\n" +
                        "0.133333 0.270588 0.270588 1.000000\n" +
                        "0.294118 0.000000 0.509804 0.698039\n" +
                        "1.000000 0.905882 0.729412 1.000000\n" +
                        "0.823529 0.705882 0.549020 1.000000\n" +
                        "0.854902 0.647059 0.125490 1.000000\n" +
                        "0.545098 0.352941 0.000000 1.000000\n" +
                        "0.396078 0.352941 0.305882 1.000000\n" +
                        "0.545098 0.490196 0.419608 1.000000\n" +
                        "1.000000 0.000000 0.501961 1.000000\n" +
                        "0.501961 0.000000 0.501961 1.000000\n" +
                        "0.294118 0.000000 0.509804 1.000000\n" +
                        "0.988235 0.988235 0.988235 1.000000\n" +
                        "0.800000 0.800000 0.800000 1.000000\n" +
                        "0.721569 0.721569 0.721569 1.000000\n" +
                        "0.643137 0.643137 0.643137 1.000000\n" +
                        "0.513725 0.513725 0.513725 1.000000\n" +
                        "0.396078 0.396078 0.396078 1.000000\n" +
                        "0.250980 0.250980 0.250980 1.000000\n" +
                        "0.078431 0.078431 0.078431 1.000000\n" +
                        "0.250980 0.250980 0.250980 0.698039\n" +
                        "0.803922 0.666667 0.490196 1.000000\n" +
                        "0.745098 0.588235 0.360784 1.000000\n" +
                        "0.729412 0.541176 0.262745 1.000000\n" +
                        "0.533333 0.396078 0.192157 1.000000\n" +
                        "0.454902 0.317647 0.113725 1.000000\n" +
                        "0.317647 0.239216 0.117647 1.000000\n" +
                        "0.419608 0.258824 0.149020 1.000000\n" +
                        "0.392157 0.196078 0.000000 1.000000\n" +
                        "0.584314 0.309804 0.039216 1.000000\n" +
                        "1.000000 0.000000 1.000000 0.000000\n",
                header);
    }

    @Test
    public void BlsFileExportTest() throws IOException {
        BlsFileExport bls = new BlsFileExport();

        bls.setColorset("colorSet.txt");

        try {
            bls.exportBlsFile("testExport.bls", new MapLayerBuilder.TestBuilder());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception in generating file");
        }

        File test = new File("testExport.bls");
        File expected = new File("testExport_compare.bls");
        BufferedReader testScanner = null;
        Scanner expectedScanner = null;
        try {
            testScanner = new BufferedReader( new InputStreamReader(new FileInputStream("testExport.bls"), "windows-1256"));
            expectedScanner = new Scanner(expected);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Scanners could not be created");
        }
        assertTrue(new File("testExport.bls").exists());

        String next = null;
        while ((next = testScanner.readLine()) != null) {
            assertEquals(next, expectedScanner.nextLine());
        }
    }
}
