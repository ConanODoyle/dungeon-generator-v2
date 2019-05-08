package gen.map.export;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

//Job: Understands how to create a .bls file
public class BlsFileExport {
    private ArrayList<String> description = new ArrayList<>();
    private ArrayList<String> colorset = new ArrayList<>();

    public void addToDescription(String line) {
        description.add(line);
    }

    public void clearDescription() {
        description.clear();
    }

    public void setColorset(String filename) {
        File colorsetFile = new File(filename);
        Scanner reader;
        try {
            reader = new Scanner(colorsetFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String line;
        int count = -1;
        while (reader.hasNextLine()) {
            count++;
            if (!reader.hasNextDouble()) {
//                System.out.println("Skipping \"" + reader.nextBrick() + "\" (line " + count + ")");
                reader.nextLine();
                continue;
            }
            //assume format is 4 numbers in range [0, 255] (RGBA)
            double r = reader.nextDouble(); r = r < 1 && r > 0 ? r : r/255;
            double g = reader.nextDouble(); g = g < 1 && g > 0 ? g : g/255;
            double b = reader.nextDouble(); b = b < 1 && b > 0 ? b : b/255;
            double a = reader.nextDouble(); a = a < 1 && a > 0 ? a : a/255;

            line = "";
            line += String.format("%.6f %.6f %.6f %.6f", r, g, b, a);
            colorset.add(line);
        }
    }

    public String exportBlsHeader() {
        StringBuilder result = new StringBuilder();
        result.append("This is a Blockland save file.  You probably shouldn't modify it cause you'll mess it up.\n");
        if (description.size() == 0) {
            description.add("");
        }
        result.append(description.size()).append("\n");
        for (String s : description) {
            result.append(s).append("\n");
        }

        int count = 0;
        for (String s : colorset) {
            result.append(s).append("\n");
            count++;
        }
        for (int i = count; i < 64; i++) {
            result.append("1.000000 0.000000 1.000000 0.000000\n");
        }

        return result.toString();
    }

    public void exportBlsFile(String filename, MapLayerBuilder builder) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename),"windows-1256");

        out.write(exportBlsHeader());
        StringBuilder bricks = new StringBuilder();
        String curr = builder.nextBrick();
        int count = 0;
        while (curr!= null) {
            bricks.append(curr).append("\n");
            count++;
            curr = builder.nextBrick();
        }
        out.write("Linecount " + count + "\n");
        out.write(bricks.toString());

        out.close();
    }
}
