package gen.export;

import java.util.ArrayList;

//Job: Understands what a bls save file brick is
@SuppressWarnings("WeakerAccess")
public class BlsBrick {
    public String uiname;
    public String NTName;
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
        this.NTName = "";
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
        double temp;
        int direction = times > 0 ? 1 : -1;

        for (int i = 0; i < Math.abs(times); i++) {
            temp = xDiff;
            xDiff = yDiff * -1 * direction;
            yDiff = temp * direction;
            angleID = (angleID - direction + 4) % 4;
        }
        x = xDiff;
        y = yDiff;
    }

    public BlsBrick getCopy() {
        BlsBrick copy = new BlsBrick(uiname, x, y, z, angleID, rest);
        copy.modifiers.addAll(modifiers);
        copy.NTName = NTName;
        return copy;
    }

    public String toStringOffset(double xOff, double yOff, double zOff) {
        x += xOff;
        y += yOff;
        z += zOff;
        String str = this.toString();
        x -= xOff;
        y -= yOff;
        z -= zOff;
        return str;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlsBrick)) {
            return false;
        }
        BlsBrick b = (BlsBrick) o;
        return Math.abs(this.x - b.x) < 0.001
                && Math.abs(this.y - b.y) < 0.001
                && Math.abs(this.z - b.z) < 0.001
                && this.angleID == b.angleID
                && this.uiname.equals(b.uiname)
                && this.NTName.equals(b.NTName);
    }

    @Override
    public int hashCode() {
        return (int) (this.uiname.hashCode() + (this.x + this.y + this.z));
    }
}
