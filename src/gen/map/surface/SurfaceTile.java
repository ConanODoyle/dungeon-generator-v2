package gen.map.surface;

import gen.map.MapTile;

import java.awt.*;

//Job: Understands the structure of the SurfaceLayer tiles
@SuppressWarnings("WeakerAccess")
public class SurfaceTile extends MapTile {
    public static SurfaceTile FOREST = new SurfaceTile("F", "Forest", new Color(0,127,0), false);
    public static SurfaceTile FORESTPATH = new SurfaceTile("^", "ForestPath", new Color(60,180,30), true);
    public static SurfaceTile FORESTFLOOR = new SurfaceTile(".", "ForestFloor", new Color(80,200,80), true);
    public static SurfaceTile DESERTFLOOR = new SurfaceTile("_", "DesertFloor", new Color(200,180,80), true);
    public static SurfaceTile CLIFF = new SurfaceTile("x", "Cliff", new Color(110,110,110), false);
    public static SurfaceTile TALLCLIFF = new SurfaceTile("X", "TallCliff", new Color(80,80,80), false);
    public static SurfaceTile TOWN = new SurfaceTile("T", "Town", new Color(160, 100, 220), true);
    public static SurfaceTile GLEN = new SurfaceTile("G", "Glen", new Color(0, 200, 220), true);
    public static SurfaceTile CAVE = new SurfaceTile("C", "Cave", new Color(150,150,150), true);

    public static SurfaceTile GOBLINCAMP = new SurfaceTile("C", "GoblinCamp", new Color(100,60,220), true);
    public static SurfaceTile RUINS = new SurfaceTile("C", "Ruins", new Color(190,100,100), true);

    public SurfaceTile(String render, String name, Color color, boolean passable) {
        super(render, name, color, passable);
    }
}
