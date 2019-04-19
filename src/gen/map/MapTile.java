package gen.map;

import java.awt.*;

//Job: Understands a map tile's basic structure
@SuppressWarnings("WeakerAccess")
public abstract class MapTile {
    public static MapTile EMPTY = new MapTile("#", Color.BLACK, false) {
    };

    public final String renderString;
    public final Color color;
    public final boolean passable;

    public MapTile(String render, Color color, boolean passable) {
        this.renderString = render;
        this.color = color;
        this.passable = passable;
    }
}
