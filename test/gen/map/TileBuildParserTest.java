package gen.map;

import gen.map.export.BlsBrick;
import gen.map.export.BlsBuilder;
import gen.map.export.BlsFileExport;
import gen.map.parser.BlsParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TileBuildParserTest {
    @Test
    public void ParseBuildIntoBricksTest() {
        BlsParser parser = new BlsParser("testExport_compare.bls");

        StringBuilder builder = new StringBuilder();
        while (parser.hasNextBrick()) {
            BlsBrick next = parser.nextBrick();
            builder.append(next.toString()).append("\n");
        }

        BlsFileExport bls = new BlsFileExport();

        bls.setColorset("colorSet.txt");

        try {
            bls.exportBlsFile("testExport.bls", new BlsBuilder.TestBuilder(builder.toString()));
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
            assertEquals(expectedScanner.nextLine(), testScanner.nextLine());
        }
    }
}
