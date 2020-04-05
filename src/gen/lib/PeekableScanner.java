package gen.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PeekableScanner
{
    private Scanner scan;
    private String nextLine;

    public PeekableScanner( String source )
    {
        scan = new Scanner( source );
        nextLine = (scan.hasNextLine() ? scan.nextLine() : null);
    }

    public PeekableScanner( File file ) {
        try {
            scan = new Scanner( file );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        nextLine = (scan.hasNextLine() ? scan.nextLine() : null);
    }

    public boolean hasNextLine()
    {
        return (nextLine != null);
    }

    public String nextLine()
    {
        String current = nextLine;
        nextLine = (scan.hasNextLine() ? scan.nextLine() : null);
        return current;
    }

    public String peek()
    {
        return nextLine;
    }
}