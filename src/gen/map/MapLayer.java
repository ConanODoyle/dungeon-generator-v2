package gen.map;

import java.util.Random;

//Job: Describes a layer of a dungeon, all contained on the same level
public abstract class MapLayer {
    private final int id;
    protected final int height;
    protected final int width;
    protected MapTile[][] tiles;

    public Random rand = new Random();

    public MapLayer(int width, int height){
        this.id = new Random().nextInt();
        this.width = width;
        this.height = height;
        tiles = new MapTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = MapTile.EMPTY;
            }
        }
    }

    public abstract void generate();

    public String[][] render() {
        String[][] render = new String[width][height];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j] == null) {
                    throw new RuntimeException("Encountered null tile!");
                }
                render[i][j] = tiles[i][j].render;
            }
        }
        return render;
    }

    public void printRender() {
        System.out.println("----------------");

        String[][] render = this.render();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.print(render[width - i - 1][height - j - 1] + " ");
            }
            System.out.println();
        }

        System.out.println("----------------");
    }

    protected void fillTiles() {

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapLayer)) {
            return false;
        } else {
            MapLayer l = (MapLayer) o;

            return l.width == width && l.height == height;
        }
    }

    @Override
    public int hashCode() {
        return id % 16;
    }

    public void setSeed(long seed) {
        this.rand.setSeed(seed);
    }

    public abstract String getTileString();
}
