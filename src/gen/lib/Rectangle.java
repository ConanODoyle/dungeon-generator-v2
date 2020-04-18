package gen.lib;

import java.awt.*;

public class Rectangle {
    public Point leftCorner; //upper-left corner
    public int width;
    public int height;

    public Rectangle(int width, int height, Point corner) {
        this.width = width;
        this.height = height;
        this.leftCorner = new Point(corner);
    }
}