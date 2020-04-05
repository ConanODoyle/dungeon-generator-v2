package gen.map;

import java.awt.*;

//Job: Understands a map tile's basic structure
@SuppressWarnings("WeakerAccess")
public abstract class MapTile {
    public static MapTile EMPTY = new MapTile("#", "Empty", Color.BLACK, false) {
    };

    public final String renderString;
    public final String name;
    public final Color color;
    public final boolean passable;
    public final MapTile parent;

    public MapTile(String render, String name, Color color, boolean passable) {
        this.renderString = render;
        this.name = name;
        this.color = color;
        this.passable = passable;
        this.parent = null;
    }

    public MapTile(MapTile copy) {
        this.renderString = copy.renderString;
        this.name = copy.name;
        this.color = copy.color;
        this.passable = copy.passable;
        this.parent = copy;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapTile)) {
            return false;
        }
        MapTile t = (MapTile) o;
        return t.parent == parent;
    }
}
