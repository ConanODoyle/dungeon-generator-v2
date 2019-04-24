package gen.map.parser;

import gen.map.export.BlsBrick;

//Job: Understands how to retrieve a tile from an octree
public class TileSearch {
    private BlsOctree octree;

    public TileSearch(BlsOctree octree) {
        this.octree = octree;
    }

    public TileSearch(String filename) {
        this(new BlsParser(filename).parseToOctTree());
    }

    public TileBuild findTile(String NTName) {
        String top = "_" + NTName + "_top", bot = "_" + NTName + "_bottom";

        if (octree.getBricksByName(top) == null || octree.getBricksByName(bot) == null) {
            throw new RuntimeException("Could not find brick with the name \"" + top + "\"!");
        }

        if (octree.getBricksByName(top).size() != 1) {
            throw new RuntimeException("Tile search failed: not only one brick with the name \"" + top + "\"!");
        } else if (octree.getBricksByName(bot).size() != 1) {
            throw new RuntimeException("Tile search failed: not only one brick with the name \"" + bot + "\"!");
        }
        BlsBrick topBrick = octree.getBricksByName(top).get(0);
        BlsBrick botBrick = octree.getBricksByName(bot).get(0);

        if (!topBrick.uiname.equals(botBrick.uiname)) {
            throw new RuntimeException("Top and bottom bricks do not match!");
        } else if (Math.abs(topBrick.x - botBrick.x) > 0.0001 || Math.abs(topBrick.y - botBrick.y) > 0.0001) {
            throw new RuntimeException("Top and bottom bricks are not aligned!");
        } else if (topBrick.z < botBrick.z) {
            throw new RuntimeException("Top brick not above bottom brick!");
        }

        int width = Integer.parseInt(topBrick.uiname.substring(0, 2));
        if (width % 16 != 0) {
            throw new RuntimeException("Tile ceiling not a baseplate!");
        }
        width = width/2;

        double[] xyz = {topBrick.x, topBrick.y, (topBrick.z + botBrick.z)/2};
        double[] boxXYZ = {width, width, topBrick.z - botBrick.z};

        return new TileBuild(octree.containerBoxSearch(xyz, boxXYZ), new double[]{topBrick.x, topBrick.y, topBrick.z});
    }
}
