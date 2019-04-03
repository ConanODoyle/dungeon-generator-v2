package gen.map.surface;

import gen.map.MapLayer;
import gen.map.MapTile;

//Job: Describes the Surface Layer
public class SurfaceLayer extends MapLayer {
    public static final MapTile[] validTiles = {
            MapTile.TALLCLIFF,
            MapTile.CLIFF,
            MapTile.FORESTFLOOR,
            MapTile.TREE
    };

    public SurfaceLayer(int width, int height) {
        super(width, height);
    }

    @Override
    public void generate() {
        //first create a border
        for (int i = 0; i < this.width; i++) {
            this.tiles[i][0] = MapTile.TALLCLIFF;
            this.tiles[i][this.height - 1] = MapTile.TALLCLIFF;
        }

        for (int i = 0; i < this.height; i++) {
            this.tiles[0][i] = MapTile.TALLCLIFF;
            this.tiles[this.width - 1][i] = MapTile.TALLCLIFF;
        }
    }

    @Override
    public String getTileString() {
        StringBuilder valid = new StringBuilder();
        for (MapTile validTile : validTiles) {
            valid.append(validTile);
        }
        return valid.toString();
    }
}
