package gen.map;

//Job: Describes a dungeon map tile
public class MapTile {
    private Object occupyingObject;
    private boolean occupied;

    public MapTile() {

    }

    public boolean isOccupied() {
        return this.occupied;
    }

    public void setOccupied(boolean isOccupied) {
        this.setOccupied(isOccupied, null);
    }

    public void setOccupied(boolean isOccupied, Object occupyingObject) {
        this.occupied = isOccupied;
        this.occupyingObject = occupyingObject;
    }
}
