package gen.map;

import java.util.ArrayList;

//Job: Understands how to create a .bls file
public class BlsFileExport {
    ArrayList<String> description = new ArrayList<>();

    public BlsFileExport() {

    }

    public void addToDescription(String line) {
        description.add(line);
    }

    public void clearDescription() {

    }
}
