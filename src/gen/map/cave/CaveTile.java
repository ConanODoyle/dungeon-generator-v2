package gen.map.cave;

import gen.map.MapTile;

import java.awt.*;

public class CaveTile extends MapTile {
    private static CaveTile ROCK = new CaveTile("X", "Rock", new Color(30,30,30), false);
    private static CaveTile HIGHCAVE = new CaveTile(".", "HighCave", new Color(130,110,80), true);
    private static CaveTile CAVE = new CaveTile(".", "Cave", new Color(110,90,60), true);
    private static CaveTile LOWCAVE = new CaveTile(".", "LowCave", new Color(90,70,40), true);
    private static CaveTile MINESHAFT = new CaveTile(".", "Mineshaft", new Color(60,80,170), true);

    private static CaveTile BOSSENTRANCE = new CaveTile("B", "BossEntrance", new Color(255, 0, 0), true);

    private CaveTile(String render, String name, Color color, boolean passable) {
        super(render, name, color, passable);
    }

    private CaveTile(CaveTile copy) {
        super(copy);
        hasSpawner = copy.hasSpawner;
        switch (copy.name) {
            case "HighCave":
            case "Cave":
            case "LowCave": spawnerChance = 1; break;
            default: spawnerChance = 0;
        }
    }

    public static boolean isRock(MapTile t) { return t.parent == ROCK; }
    public static boolean isHighCave(MapTile t) { return t.parent == HIGHCAVE; }
    public static boolean isCave(MapTile t) { return t.parent == CAVE; }
    public static boolean isLowCave(MapTile t) { return t.parent == LOWCAVE; }

    public static boolean isBossEntrance(MapTile t) { return t.parent == BOSSENTRANCE; }

    public static CaveTile Rock() { return new CaveTile(ROCK); }
    public static CaveTile HighCave() { return new CaveTile(HIGHCAVE); }
    public static CaveTile Cave() { return new CaveTile(CAVE); }
    public static CaveTile LowCave() { return new CaveTile(LOWCAVE); }
    public static CaveTile Mineshaft() { return new CaveTile(MINESHAFT); }

    public static CaveTile BossEntrance() { return new CaveTile(BOSSENTRANCE); }
}
