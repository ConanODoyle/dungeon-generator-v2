package gen.export;

import gen.lib.PeekableScanner;
import gen.parser.TileBuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

//Job: Understands how to return formatted .bls strings
public abstract class MapLayerBuilder {
    public static class TestBuilder extends MapLayerBuilder {
        private PeekableScanner s;

        public TestBuilder() {

        }

        public TestBuilder(String input) {
            this.s = new PeekableScanner(input);
        }

        @Override
        public void generateBuild() {
            s = new PeekableScanner(new File("TestBuilderBls.txt"));
        }

        @Override
        public String nextBrick() {
            if (s == null) {
                generateBuild();
            }

            if (s.hasNextLine()) {
                StringBuilder builder = new StringBuilder();
                builder.append(s.nextLine());
                while (s.peek() != null && s.peek().substring(0, 2).equals("+-")) {
                    builder.append("\n").append(s.nextLine());
                }
                return builder.toString();
            } else {
                return null;
            }
        }
    }

    public ArrayList<BlsBrick> bricks = new ArrayList<>();

    public abstract void generateBuild();
    public abstract String nextBrick();


    public void buildTileAt(Random rand, TileBuild tileBuild, double offsetX, double offsetY) {
        TileBuild adjTile = tileBuild;
        ArrayList<BlsBrick> currTileBricks = adjTile.getRotatedBricks(rand.nextInt(4));
        for (BlsBrick b : currTileBricks) {
            b.x += offsetX;
            b.y += offsetY;
        }
        bricks.addAll(currTileBricks);
    }

    public void buildTileAt(TileBuild tileBuild, double offsetX, double offsetY, int rotation) {
        TileBuild adjTile = tileBuild;
        ArrayList<BlsBrick> currTileBricks = adjTile.getRotatedBricks(rotation);
        for (BlsBrick b : currTileBricks) {
            b.x += offsetX;
            b.y += offsetY;
        }
        bricks.addAll(currTileBricks);
    }
}
