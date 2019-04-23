package gen.map.parser;

import gen.map.export.BlsBrick;

import java.util.ArrayList;

//Job: Understands an octree of BlsBricks (for fast lookup and search)
@SuppressWarnings("FieldCanBeLocal")
public class BlsOctree {
    private static int MAX_ELEMENTS = 32;
    private static int MIN_SIZE = 2;
    private int x, y, z;
    private ArrayList<BlsBrick> container;
    //+++, ++-, +-+, +--, -++, -+-, --+, --- (xyz diff)
    private BlsOctree[] subtrees = new BlsOctree[8];
    private int dimensions; //box dimensions = dimensions x dimensions x dimensions
    private int size = 0;

    public BlsOctree() {
        this(0, 0, 0, (int) Math.pow(2, 24));
    }

    private BlsOctree(int x, int y, int z, int dimensions) {
        this.container = new ArrayList<>();
        this.dimensions = dimensions;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(BlsBrick blsBrick) {
        container.add(blsBrick);
        size++;
        if (size > MAX_ELEMENTS) {
            update();
            int j = 1;
        }
    }

    public int size() {
        return size;
    }

    public ArrayList<BlsBrick> containerBoxSearch(double[] xyz, double[] boxXYZ) {
        ArrayList<BlsBrick> result = new ArrayList<>();

        if (!intersects(xyz, boxXYZ)) {
            return result;
        }

        if (size > MAX_ELEMENTS && dimensions >= MIN_SIZE) {

            for (BlsOctree subtree : subtrees) {
                result.addAll(subtree.containerBoxSearch(xyz, boxXYZ));
            }
        } else {
            //add minor error to account for double rounding problems;
            double[] bigCorner = {xyz[0] + (boxXYZ[0] + 0.001)/2,
                    xyz[1] + (boxXYZ[1] + 0.001)/2,
                    xyz[2] + (boxXYZ[2] + 0.001)/2
            };
            double[] smallCorner = {xyz[0] - (boxXYZ[0] + 0.001)/2,
                    xyz[1] - (boxXYZ[1] + 0.001)/2,
                    xyz[2] - (boxXYZ[2] + 0.001)/2
            };

            //check bricks in oct
            for (BlsBrick b : container) {
                if (b.x <= bigCorner[0] && b.x >= smallCorner[0]
                        && b.y <= bigCorner[1] && b.y >= smallCorner[1]
                        && b.z <= bigCorner[2] && b.z >= smallCorner[2]) {
                    result.add(b);
                }
            }
        }

        return result;
    }

    private boolean intersects(double[] xyz, double[] boxXYZ) {
        double[] xR = {x - dimensions/2d, x + dimensions/2d};
        double[] yR = {y - dimensions/2d, y + dimensions/2d};
        double[] zR = {z - dimensions/2d, z + dimensions/2d};

        //account for minor error in floats
        double[] xBR = {xyz[0] - (boxXYZ[0] + 0.001)/2, xyz[0] + (boxXYZ[0] + 0.001)/2};
        double[] yBR = {xyz[1] - (boxXYZ[1] + 0.001)/2, xyz[1] + (boxXYZ[1] + 0.001)/2};
        double[] zBR = {xyz[2] - (boxXYZ[2] + 0.001)/2, xyz[2] + (boxXYZ[2] + 0.001)/2};

        return xR[1] >= xBR[0] && xBR[1] >= xR[0]
                && yR[1] >= yBR[0] && yBR[1] >= yR[0]
                && zR[1] >= zBR[0] && zBR[1] >= zR[0];
    }

    private void update() {
        if (dimensions <= MIN_SIZE) {
            return;
        }
        if (subtrees[0] == null && dimensions >= MIN_SIZE) {
            initializeSubtrees();
        }

        for (BlsBrick b : container) {
            int high = Math.round(b.x * 100) >= x * 100 ? 0 : 4;
            int mid = Math.round(b.y * 100) >= y * 100 ? 0 : 2;
            int low = Math.round(b.z * 100) >= z * 100 ? 0 : 1;
            int idx = low + mid + high;
            subtrees[idx].add(b);
        }

        container.clear();
    }

    private void initializeSubtrees() {
        int newDimensions = dimensions / 2;
        int modPos = newDimensions / 2;
        int[][] modValues = {
                {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1},
                {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
        };

        for (int i = 0; i < subtrees.length; i++) {
            subtrees[i] = new BlsOctree(modValues[i][0] * modPos + x,
                    modValues[i][1] * modPos + y,
                    modValues[i][2] * modPos + z, newDimensions);
        }
    }

    @Override
    public String toString() {
        return "{" + this.x + "," + this.y + "} (" + this.dimensions + ") size:" + size;
    }
}
