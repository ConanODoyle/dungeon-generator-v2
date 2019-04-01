package gen.map;

//Job: Describes a layer of a dungeon, all contained on the same level
public class MapLayer {
    public MapLayer(){

    }

    public void generateMap(DungeonLayout o) {

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapLayer)) {
            return false;
        } else {
            MapLayer l = (MapLayer) o;
            return true;
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
