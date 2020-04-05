package gen.parser;

import gen.export.BlsBrick;

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
        String top1 = "_" + tileName + "_top1", bot1 = "_" + tileName + "_bottom1";
        String top2 = "_" + tileName + "_top2", bot2 = "_" + tileName + "_bottom2";
        if (octree.getBricksByName(top1) == null || octree.getBricksByName(bot1) == null
                || octree.getBricksByName(top2) == null || octree.getBricksByName(bot2) == null) {
            throw new RuntimeException("Could not find special tile top/bottom with the name \"" + tileName + "\"!");
        }

        if (octree.getBricksByName(top1).size() != 1) {
            throw new RuntimeException("Tile search failed: not only one brick with the name \"" + top1 + "\"!");
        } else if (octree.getBricksByName(bot1).size() != 1) {
            throw new RuntimeException("Tile search failed: not only one brick with the name \"" + bot1 + "\"!");
        } else if (octree.getBricksByName(top2).size() != 1) {
            throw new RuntimeException("Tile search failed: not only one brick with the name \"" + top2 + "\"!");
        } else if (octree.getBricksByName(bot2).size() != 1) {
            throw new RuntimeException("Tile search failed: not only one brick with the name \"" + bot2 + "\"!");
        }

        BlsBrick t1 = octree.getBricksByName(top1).get(0);
        BlsBrick t2 = octree.getBricksByName(top2).get(0);
        BlsBrick b1 = octree.getBricksByName(bot1).get(0);
        BlsBrick b2 = octree.getBricksByName(bot2).get(0);

        if (!t1.uiname.equals(b1.uiname) || !t2.uiname.equals(b2.uiname)) {
            throw new RuntimeException("Top and bottom bricks do not match!");
        } else if (Math.abs(t1.x - b1.x) > 0.0001 || Math.abs(t1.y - b1.y) > 0.0001
                || Math.abs(t2.x - b2.x) > 0.0001 || Math.abs(t2.y - b2.y) > 0.0001) {
            throw new RuntimeException("Top and bottom bricks are not aligned!");
        } else if (t1.z < b1.z || t2.z < b2.z) {
            throw new RuntimeException("Top brick not above bottom brick!");
        }

        double[] xyz = new double[3];
        double[] boxXYZ;
        int c1width = Integer.parseInt(t1.uiname.substring(0, 2));
        int c2width = Integer.parseInt(t2.uiname.substring(0, 2));
        if (c1width % 16 != 0 || c2width % 16 != 0) {
            throw new RuntimeException("Specialtile ceiling not a baseplate!");
        } else if (c1width != c2width) {
            throw new RuntimeException("Specialtile corner baseplates not same size!");
        }
        double topZ = (t1.z + t2.z) / 2;
        double botZ = (b1.z + b2.z) / 2;

        //average positions to get tile center
        //also determine dimensions (must be square)
        xyz[0] = (t1.x + t2.x) / 2;
        xyz[1] = (t1.y + t2.y) / 2;
        xyz[2] = (topZ + botZ) / 2;
        double xDim = Math.abs(t1.x - t2.x) + c1width / 2d;
        double yDim = Math.abs(t1.y - t2.y) + c1width / 2d;
        if (Math.abs(xDim - yDim) > 0.0001) {
            throw new RuntimeException("Specialtile not defined by square corners!");
        }
        boxXYZ = new double[]{xDim, yDim, topZ - botZ - 0.4};

        return new TileBuild(octree.containerBoxSearch(xyz, boxXYZ), new double[]{xyz[0], xyz[1], topZ - 0.1});
    }
}
