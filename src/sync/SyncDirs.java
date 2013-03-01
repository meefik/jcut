package sync;

import diff.Diff;

/**
 *
 * @author Anton Skshidlevsky
 */
public class SyncDirs {
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            long t = System.currentTimeMillis();
            Diff.go(args[0],args[1]);
            System.out.println("Time (ms): " + (System.currentTimeMillis() - t));
            
        } else {
            System.out.println("Usage: java -jar syncdirs.jar <dir1> <dir2>");
        }
    }
}