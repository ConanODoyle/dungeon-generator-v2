package gen.map.parser;

import gen.map.export.BlsBrick;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class TileBuild {
    private ArrayList<BlsBrick> bricks;
    private double[] center;

    public TileBuild(ArrayList<BlsBrick> bricks, double[] center) {
        this.bricks = bricks;
        this.center = center;
    }

    public ArrayList<BlsBrick> getBricks() {
        return bricks;
    }
}
