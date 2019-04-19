package gen.map;

import java.awt.*;

public abstract class MapTile {
    public static MapTile EMPTY = new MapTile("#", Color.BLACK, false) {
        @Override
        public String getSaveString() {
            return null;
        }
    };
//    FOREST("F", new Color(0, 127, 0), false),
//    FORESTPATH("^", new Color(60, 180, 30), true),
//    FORESTFLOOR(".", new Color(80, 200, 80), true),
//    DESERTFLOOR("_", new Color(120, 100, 0), true),
//    CLIFF("x", new Color(127, 127, 127), false),
//    TALLCLIFF("X", new Color(80, 80, 80), false),
//    TOWN("T", Color.CYAN, true),
//    GLEN("G", Color.green, true);
    public final String renderString;
    public final Color color;
    public final boolean passable;

    public MapTile(String render, Color color, boolean passable) {
        this.renderString = render;
        this.color = color;
        this.passable = passable;
    }

    public abstract String getSaveString();
}
