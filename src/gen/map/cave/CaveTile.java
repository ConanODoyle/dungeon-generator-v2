package gen.map.cave;

import gen.map.MapTile;

import java.awt.*;

public class CaveTile extends MapTile {
    public static CaveTile ROCK = new CaveTile("X", "Rock", new Color(30,30,30), false);
    public static CaveTile CAVE = new CaveTile(".", "Cave", new Color(30,30,30), false);
    public static CaveTile LEVEL1CAVE = new CaveTile(".", "L1Cave", new Color(160,120,100), false);
    public static CaveTile LEVEL2CAVE = new CaveTile(".", "L2Cave", new Color(130,110,80), false);
    public static CaveTile LEVEL3CAVE = new CaveTile(".", "L3Cave", new Color(110,90,60), false);
    public static CaveTile LEVEL4CAVE = new CaveTile(".", "L4Cave", new Color(90,70,40), false);
    public static CaveTile LEVEL5CAVE = new CaveTile(".", "L5Cave", new Color(70,50,20), false);

    private CaveTile(String render, String name, Color color, boolean passable) {
        super(render, name, color, passable);
    }
}
