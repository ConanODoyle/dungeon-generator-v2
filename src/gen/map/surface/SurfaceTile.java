package gen.map.surface;

import gen.map.MapTile;

import java.awt.*;

public class SurfaceTile extends MapTile {
    public static SurfaceTile FOREST = new SurfaceTile("F", new Color(0,127,0), false);
    public static SurfaceTile FORESTPATH = new SurfaceTile("^", new Color(60,180,30), true);
    public static SurfaceTile FORESTFLOOR = new SurfaceTile(".", new Color(80,200,80), true);
    public static SurfaceTile DESERTFLOOR = new SurfaceTile("_", new Color(120,100,0), true);
    public static SurfaceTile CLIFF = new SurfaceTile("x", new Color(127,127,127), false);
    public static SurfaceTile TALLCLIFF = new SurfaceTile("X", new Color(80,80,80), false);
    public static SurfaceTile TOWN = new SurfaceTile("T", Color.CYAN, true);
    public static SurfaceTile GLEN = new SurfaceTile("G", Color.GREEN, true);

    public SurfaceTile(String render, Color color, boolean passable) {
        super(render, color, passable);
    }

    @Override
    public String getSaveString() {
        return null;
    }
}
