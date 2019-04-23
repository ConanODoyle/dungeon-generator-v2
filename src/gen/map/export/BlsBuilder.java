package gen.map.export;

import gen.map.lib.PeekableScanner;

import java.io.File;

//Job: Understands how to return    bricks as strings for bls export
public abstract class BlsBuilder {
    public static class TestBuilder extends BlsBuilder{
        private PeekableScanner s;

        public TestBuilder() {

        }

        public TestBuilder(String input) {
            this.s = new PeekableScanner(input);
        }

        @Override
        public void generateBuild() {
            s = new PeekableScanner(new File("TestBuilderBls.txt"));
        }

        @Override
        public String nextBrick() {
            if (s == null) {
                generateBuild();
            }

            if (s.hasNextLine()) {
                StringBuilder builder = new StringBuilder();
                builder.append(s.nextLine());
                while (s.peek() != null && s.peek().substring(0, 2).equals("+-")) {
                    builder.append("\n").append(s.nextLine());
                }
                return builder.toString();
            } else {
                return null;
            }
        }
    }

    public abstract void generateBuild();
    public abstract String nextBrick();

}
