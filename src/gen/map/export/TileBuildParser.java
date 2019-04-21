package gen.map.export;

import java.io.File;
import java.util.Scanner;

public class TileBuildParser {
    private File bls;
    private Scanner contents;
    private BlsBrick lastBrick;
    private BlsBrick nextBrick;

    public TileBuildParser(String s) {
        bls = new File(s);
        try {
            contents = new Scanner(bls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nextBrick = null;
        lastBrick = null;
        getNextBrick();
        getNextBrick();
    }

    private void getNextBrick() {
        lastBrick = nextBrick;
        nextBrick = null;
        if (!contents.hasNextLine()) {
            return;
        }

        String line, name;
        Scanner ls;
        while (contents.hasNextLine()) {
            line = contents.nextLine();
            ls = new Scanner(line);
            name = ls.next();
            if (!name.substring(name.length() - 1).equals("\"")) {
                nextBrick = new BlsBrick(name, ls.nextDouble(), ls.nextDouble(),
                        ls.nextInt(), ls.nextLine());
                break;
            } else if (lastBrick != null) {
                lastBrick.addModifier(line);
            }
        }
    }

    public boolean hasNextBrick() {
        return lastBrick != null;
    }

    public BlsBrick nextBrick() {
        BlsBrick result = lastBrick;
        getNextBrick();
        return result;
    }
}
