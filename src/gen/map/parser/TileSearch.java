package gen.map.parser;

import gen.map.export.BlsBrick;

import java.util.ArrayList;

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
        NTName = NTName.toLowerCase();
        String top = "_" + NTName + "_top", bot = "_" + NTName + "_bottom";

        if (octree.getBricksByName(top) == null || octree.getBricksByName(bot) == null) {
            throw new RuntimeException("Could not find top/bottom with the name \"" + NTName + "\"!");
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
        double[] boxXYZ = {width, width, topBrick.z - botBrick.z - 0.4};

        return new TileBuild(octree.containerBoxSearch(xyz, boxXYZ), new double[]{xyz[0], xyz[1], topBrick.z - 0.1});
    }

    public TileBuild findSpecialTile(String tileName) {
        tileName = tileName.toLowerCase();
        String top = "_" + tileName + "_top", bot = "_" + tileName + "_bottom";
        if (octree.getBricksByName(top) == null || octree.getBricksByName(bot) == null) {
            throw new RuntimeException("Could not find brick with the name \"" + top + "\"!");
        }

        ArrayList<BlsBrick> topBricks = octree.getBricksByName(top);
        ArrayList<BlsBrick> botBricks = octree.getBricksByName(bot);

        if (topBricks.size() != botBricks.size()) {
            throw new RuntimeException("Topbrick count does not match botbrick count!");
        }

        double[] xyz = new double[3];
        double[] boxXYZ;
        int width = 0;
        double topZ = -100; double botZ = 1000000;
        BlsBrick topBrick, botBrick;

        //average positions to get tile center
        //also determine dimensions (must be square)
        for (int i = 0; i < topBricks.size(); i++) {
            width = Integer.parseInt(topBricks.get(i).uiname.substring(0, 2));
            if (width % 16 != 0) {
                throw new RuntimeException("Specialtile ceiling not a baseplate!");
            }
            width = width/2;

            topBrick = topBricks.get(i); botBrick = botBricks.get(i);
            topZ = topBrick.z > topZ ? topBrick.z : topZ;
            botZ = botBrick.z < botZ ? botBrick.z : botZ;

            xyz[0] += topBrick.x; xyz[1] += topBrick.y; xyz[2] += topBrick.z;
            xyz[0] += botBrick.x; xyz[1] += botBrick.y; xyz[2] += botBrick.z;
        }
        xyz[0] /= topBricks.size() * 2;
        xyz[1] /= topBricks.size() * 2;
        xyz[2] /= topBricks.size() * 2;
        double root = Math.sqrt(topBricks.size());
        boxXYZ = new double[]{root * width, root * width, topZ - botZ};

        return new TileBuild(octree.containerBoxSearch(xyz, boxXYZ), new double[]{xyz[0], xyz[1], topZ});
    }
}
