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
            if (!line.contains("\"") || line.substring(0, 2).equals("+-")) {
                if (lastBrick != null) {
                    lastBrick.addModifier(line);
                }
                continue;
            }
            name = line.substring(0, line.indexOf("\"") + 1);
            ls = new Scanner(line.substring(line.indexOf("\"") + 2));
            if (name.substring(name.length() - 1).equals("\"")) {
                nextBrick = new BlsBrick(name, ls.nextDouble(), ls.nextDouble(), ls.nextDouble(),
                        ls.nextInt(), ls.nextLine());
                break;
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
