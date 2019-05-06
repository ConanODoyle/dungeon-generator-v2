package gen.map.parser;

import gen.map.export.BlsBrick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

//Job: Understands how to parse a save file into bricks
@SuppressWarnings("WeakerAccess")
public class BlsParser {
    private BufferedReader contents;
    private BlsBrick lastBrick;
    private BlsBrick nextBrick;

    public BlsParser(String s) {
        File bls = new File(s);
        try {
            contents = new BufferedReader(new FileReader(bls));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        nextBrick = null;
        lastBrick = null;
        try {
            getNextBrick();
            getNextBrick();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getNextBrick() throws IOException {
        lastBrick = nextBrick;
        nextBrick = null;

        String line = null, name = null;
        Scanner ls;
        while ((line = contents.readLine()) != null) {
            if (!line.contains("\"") || line.substring(0, 2).equals("+-")) {
                if (lastBrick != null) {
                    lastBrick.addModifier(line);
                    if (line.substring(0, 4).equals("+-NT")) {
                        lastBrick.NTName = line.substring(15).toLowerCase();
                    }
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
        try {
            getNextBrick();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public BlsOctree parseToOctTree() {
        BlsOctree result = new BlsOctree();
        while (hasNextBrick()) {
            result.add(nextBrick());
        }
        return result;
    }
}
