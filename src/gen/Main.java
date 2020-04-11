package gen;

import gen.export.BlsFileExport;
import gen.export.MapLayerBuilder;
import gen.export.MapLayerExport;
import gen.map.MapLayer;
import gen.map.surface.SurfaceLayer;

import java.io.IOException;
import java.util.Random;

public class Main {
    public static String tilesetPath = "resources/tilesets.bls";
    public static String colorsetPath = "resources/colorSet.txt";
    public static String outputPath = "mapExport.bls";
    public static String imageName = "";
    public static String layer = "Surface";
    public static long seed = new Random().nextLong();

    public static void main(String[] args) {
        String curr;
        for (int i = 0; i < args.length; i++) {
            curr = args[i];
            switch (curr) {
                case "--colorset":
                case "-c":
                    colorsetPath = args[++i]; break;
                case "--tilesets":
                case "-t":
                    tilesetPath = args[++i]; break;
                case "--filename":
                case "-f":
                    outputPath = args[++i]; break;
                case "--layer":
                case "-l":
                    layer = args[++i]; break;
                case "--seed":
                case "-s":
                    seed = Long.parseLong(args[++i]); break;
                case "--imagename":
                case "-i":
                    imageName = args[++i]; break;
                case "--help":
                case "-h":
                    System.out.println("args:" +
                            "-c --colorset: filepath of colorset.txt to use" +
                            "-t --tilesets: filepath of tilesets.bls to use" +
                            "-f --filename: output file name" +
                            "-i --imagename: export image of map gen with this name" +
                            "-l --layer: layer to generate (string)");
                    return;
            }
        }

        layer = layer.toLowerCase();

        MapLayer maplayer;
        MapLayerBuilder builder;
        BlsFileExport exporter = new BlsFileExport();
        exporter.setColorset(colorsetPath);

        switch (layer) {
            case "surface":
                maplayer = new SurfaceLayer(180, 180);
                maplayer.seed = seed;
                maplayer.generate();
                builder = maplayer.getBuilder();
                builder.generateBuild();
                break;
            case "cave":
            case "desert":
            default:
                System.out.println("Invalid layer type!");
                return;
        }


        try {
            exporter.exportBlsFile(outputPath, builder);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create file");
        }

        if (!imageName.equals("")) {
            MapLayerExport.exportAsImage(maplayer, imageName, 10, 0);
        }
    }
}
