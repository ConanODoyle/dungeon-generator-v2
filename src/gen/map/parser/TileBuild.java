package gen.map.parser;

import gen.map.export.BlsBrick;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class TileBuild {
    private ArrayList<BlsBrick> bricks;
    private double[] center;

    public TileBuild(ArrayList<BlsBrick> bricks, double[] center) {
        this.bricks = new ArrayList<>();
        for (BlsBrick b : bricks) {
            this.bricks.add(0, b.getCopy());
        }
        this.center = center;

        fixOffset();
    }

    private void fixOffset() {
        for (BlsBrick b : bricks) {
            b.x -= center[0];
            b.y -= center[1];
            b.z -= center[2];
        }
    }

    /**
     * @param times - number of times to rotate counterclockwise
     */
    public ArrayList<BlsBrick> getRotatedBricks(int times) {
        if (times == 0) {
            return getBricks();
        }

        ArrayList<BlsBrick> result = new ArrayList<>();
        for (BlsBrick b : bricks) {
            BlsBrick copy = b.getCopy();
            copy.rotate(times, 0, 0);
            result.add(0, copy);
        }
        return result;
    }

    public ArrayList<BlsBrick> getBricks() {
        return bricks;
    }
}