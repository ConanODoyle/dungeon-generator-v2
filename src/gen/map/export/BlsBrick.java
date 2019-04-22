package gen.map.export;

import java.util.ArrayList;

//Job: Understands what a bls save file brick is
@SuppressWarnings("WeakerAccess")
public class BlsBrick {
    public String uiname;
    public double x;
    public double y;
    public double z;
    public int angleID;
    public String rest;
    public ArrayList<String> modifiers;

    /*
     * Example bls data:
     * 2x4" 3.5 2 1.1 0 0 16  0 0 1 1 1
     * +-EVENT 0 1 onActivate 0 Self disappear 5
     * +-EMITTER Brick Explosion" 2
     *
     * First line is brick data
     * Second and third lines are modifiers (copy directly into output save file)
     */

    public BlsBrick(String uiname, double x, double y, double z, int angleID, String rest) {
        this.uiname = uiname;
        this.x = x;
        this.y = y;
        this.z = z;
        this.angleID = angleID;
        this.rest = rest;
        this.modifiers = new ArrayList<>();
    }

    public void addModifier(String modifier) {
        modifiers.add(modifier);
    }

    /**
     * @param times - number of times to rotate counterclockwise (90 deg each turn)
     * @param axisX - x position to rotate around
     * @param axisY - y position to rotate around
     */
    public void rotate(int times, double axisX, double axisY) {
        double xDiff = x - axisX;
        double yDiff = y - axisY;
        int direction = times > 0 ? 1 : -1;

        for (int i = 0; i < Math.abs(times); i++) {
            xDiff = yDiff * -1 * direction;
            yDiff = xDiff * direction;
            angleID += direction;
        }
        x = xDiff;
        y = yDiff;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(uiname).append(" ");

        //Truncate decimal portion of whole numbers
        double[] pos = {x, y, z};
        for (double d : pos) {
            if (d % 1 == 0) {
                result.append((int) d);
            } else {
                result.append(d);
            }
            result.append(" ");
        }

        result.append(angleID).append(rest);

        for (String m : modifiers) {
            result.append("\n").append(m);
        }
        return result.toString();
    }
}
