package gen.map.surface;

import gen.map.MapTile;

import java.awt.*;

//Job: Understands the structure of the SurfaceLayer tiles
@SuppressWarnings("WeakerAccess")
public class SurfaceTile extends MapTile {
    public static SurfaceTile FOREST = new SurfaceTile("F", new Color(0,127,0), false);
    public static SurfaceTile FORESTPATH = new SurfaceTile("^", new Color(60,180,30), true);
    public static SurfaceTile FORESTFLOOR = new SurfaceTile(".", new Color(80,200,80), true);
    public static SurfaceTile DESERTFLOOR = new SurfaceTile("_", new Color(200,180,80), true);
    public static SurfaceTile CLIFF = new SurfaceTile("x", new Color(110,110,110), false);
    public static SurfaceTile TALLCLIFF = new SurfaceTile("X", new Color(80,80,80), false);
    public static SurfaceTile TOWN = new SurfaceTile("T", new Color(160, 100, 220), true);
    public static SurfaceTile GLEN = new SurfaceTile("G", new Color(0, 200, 220), true);
    public static SurfaceTile CAVE = new SurfaceTile("C", new Color(150,150,150), true);


    public SurfaceTile(String render, Color color, boolean passable) {
        super(render, color, passable);
    }

}
