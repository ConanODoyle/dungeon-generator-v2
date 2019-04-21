package gen.map.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public abstract class BlsBuilder {
    public static class TestBuilder extends BlsBuilder{
        private Scanner s;

        public TestBuilder() {

        }

        public TestBuilder(String input) {
            this.s = new Scanner(input);
        }

        @Override
        public void generateBuild() {
            try {
                s = new Scanner(new File("TestBuilderBls.txt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String nextLine() {
            if (s == null) {
                generateBuild();
            }

            if (s.hasNext()) {
                return s.nextLine();
            } else {
                return null;
            }
        }
    }

    public abstract void generateBuild();
    public abstract String nextLine();

}