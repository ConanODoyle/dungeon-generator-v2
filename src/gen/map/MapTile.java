package gen.map;

public enum MapTile {
    EMPTY("#"),
    TREE("^"),
    FORESTFLOOR("."),
    CLIFF("x"),
    TALLCLIFF("X");

    public final String render;

    MapTile(String render) {
        this.render = render;
    }
}
