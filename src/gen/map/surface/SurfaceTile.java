package gen.map.surface;

import gen.map.MapTile;

import java.awt.*;

//Job: Understands the structure of the SurfaceLayer tiles
@SuppressWarnings("WeakerAccess")
public class SurfaceTile extends MapTile {
    private static SurfaceTile FOREST = new SurfaceTile("F", "Forest", new Color(0,127,0), false);
    private static SurfaceTile FORESTPATH = new SurfaceTile("^", "ForestPath", new Color(60,180,30), true);
    private static SurfaceTile FORESTFLOOR = new SurfaceTile(".", "ForestFloor", new Color(80,200,80), true);
    private static SurfaceTile CLIFF = new SurfaceTile("x", "Cliff", new Color(110,110,110), false);
    private static SurfaceTile TALLCLIFF = new SurfaceTile("X", "TallCliff", new Color(80,80,80), false);
    private static SurfaceTile TOWN = new SurfaceTile("T", "Town", new Color(160, 100, 220), true);
    private static SurfaceTile GLEN = new SurfaceTile("G", "Glen", new Color(0, 200, 220), true);
    private static SurfaceTile CAVE = new SurfaceTile("C", "Cave", new Color(150,150,150), true);

    private static SurfaceTile GOBLINCAMP = new SurfaceTile("G", "GoblinCamp", new Color(100,60,220), true);
    private static SurfaceTile RUINS = new SurfaceTile("R", "Ruins", new Color(190,100,100), true);
    private static SurfaceTile SETTLEMENT = new SurfaceTile("S", "Settlement", new Color(190,190,100), true);

    private static SurfaceTile BOSSENTRANCE = new SurfaceTile("B", "BossEntrance", new Color(255, 0, 0), true);

    public final SurfaceTile parent;

    private SurfaceTile(String render, String name, Color color, boolean passable) {
        super(render, name, color, passable);
        parent = null;
    }

    private SurfaceTile(SurfaceTile copy) {
        super(copy);
        parent = copy;
        hasSpawner = copy.hasSpawner;
        switch (copy.name) {
            case "ForestPath": spawnerChance = 0.5; break;
            case "ForestFloor":
            case "GoblinCamp": 
            case "Ruins": spawnerChance = 1; break;
            default: spawnerChance = 0;
        }
    }

    public static boolean isForest(MapTile t) { return t.parent == FOREST; }
    public static boolean isForestPath(MapTile t) { return t.parent == FORESTPATH; }
    public static boolean isForestFloor(MapTile t) { return t.parent == FORESTFLOOR; }
    public static boolean isCliff(MapTile t) { return t.parent == CLIFF; }
    public static boolean isTallCliff(MapTile t) { return t.parent == TALLCLIFF; }
    public static boolean isTown(MapTile t) { return t.parent == TOWN; }
    public static boolean isGlen(MapTile t) { return t.parent == GLEN; }
    public static boolean isCave(MapTile t) { return t.parent == CAVE; }

    public static boolean isGoblinCamp(MapTile t) { return t.parent == GOBLINCAMP; }
    public static boolean isRuins(MapTile t) { return t.parent == RUINS; }
    public static boolean isSettlement(MapTile t) { return t.parent == SETTLEMENT; }
    public static boolean isBossEntrance(MapTile t) { return t.parent == BOSSENTRANCE; }

    public static SurfaceTile Forest() { return new SurfaceTile(FOREST); }
    public static SurfaceTile ForestPath() { return new SurfaceTile(FORESTPATH); }
    public static SurfaceTile ForestFloor() { return new SurfaceTile(FORESTFLOOR); }
    public static SurfaceTile Cliff() { return new SurfaceTile(CLIFF); }
    public static SurfaceTile TallCliff() { return new SurfaceTile(TALLCLIFF); }
    public static SurfaceTile Town() { return new SurfaceTile(TOWN); }
    public static SurfaceTile Glen() { return new SurfaceTile(GLEN); }
    public static SurfaceTile Cave() { return new SurfaceTile(CAVE); }

    public static SurfaceTile GoblinCamp() { return new SurfaceTile(GOBLINCAMP); }
    public static SurfaceTile Ruins() { return new SurfaceTile(RUINS); }
    public static SurfaceTile Settlement() { return new SurfaceTile(SETTLEMENT); }
    public static SurfaceTile BossEntrance() { return new SurfaceTile(BOSSENTRANCE); }
}
