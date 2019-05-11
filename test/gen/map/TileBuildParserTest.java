package gen.map;

import gen.map.export.BlsBrick;
import gen.map.export.MapLayerBuilder;
import gen.map.export.BlsFileExport;
import gen.map.parser.BlsOctree;
import gen.map.parser.BlsParser;
import gen.map.parser.TileBuild;
import gen.map.parser.TileSearch;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
            bls.exportBlsFile("testExport.bls", new MapLayerBuilder.TestBuilder(builder.toString()));
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

    @Test
    public void ParseCountTest() {
        BlsParser parser = new BlsParser("testExport_compare.bls");
        BlsOctree o = parser.parseToOctTree();
        assertEquals(695, o.size());

        parser = new BlsParser("resources/tests/testTileset.bls");
        o = parser.parseToOctTree();
        assertEquals(6658, o.size());
    }

    @Test
    public void ParseIntoTilesTest() {
        TileSearch search = new TileSearch("resources/tests/tileTest.bls");
        TileBuild test0 = search.findTile("testTile0");
        TileBuild test1 = search.findTile("testTile1");

        assertEquals(10, test0.getBricks().size());
        assertEquals(10, test1.getBricks().size());
    }

    @Test
    public void ParseSpecialTilesTest() {
        TileSearch search = new TileSearch("resources/tests/specialTileTest.bls");
        TileBuild test0 = search.findSpecialTile("TempleRuins");

        assertEquals(130, test0.getBricks().size());
    }

    @Test
    public void TileRotationTest() {
        TileSearch search = new TileSearch("resources/tests/tileTest.bls");
        TileBuild test0 = search.findTile("testTile0");
        TileBuild test1 = search.findTile("testTile1");

        ArrayList<BlsBrick> tile0Bricks = test0.getRotatedBricks(1);
        ArrayList<BlsBrick> tile1Bricks = test1.getBricks();
        checkBricksMatch(tile0Bricks, tile1Bricks);

        tile0Bricks = test0.getRotatedBricks(3);
        tile1Bricks = test1.getRotatedBricks(-2);
        checkBricksMatch(tile0Bricks, tile1Bricks);

        tile0Bricks = test0.getRotatedBricks(-2);
        tile1Bricks = test1.getRotatedBricks(1);
        checkBricksMatch(tile0Bricks, tile1Bricks);
    }

    private void checkBricksMatch(ArrayList<BlsBrick> tile0Bricks, ArrayList<BlsBrick> tile1Bricks) {
        for (BlsBrick curr : tile0Bricks) {
            curr.NTName = "";
            boolean match = false;

            for (BlsBrick tile1Brick : tile1Bricks) {
                tile1Brick.NTName = "";
                if (curr.equals(tile1Brick)) {
                    match = true;
                    break;
                }
            }
            assertTrue(match);
        }
    }
}
