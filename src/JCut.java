
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Отсеживание изменений в каталоге (главный класс)
 *
 * @author Anton Skshidlevsky
 */
public class JCut {

    private static void printHelp() {
        System.out.println("Usage: java -jar jcut.jar <directory> [snapshot.gz]");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }
        try {
            File dir = new File(args[0]);
            if (!dir.exists()) {
                printHelp();
                return;
            }
            File laststate;
            if (args.length == 2) {
                laststate = new File(args[1]);
            } else {
                String tempDir = System.getProperty("java.io.tmpdir");
                laststate = new File(tempDir + File.separator + "jcut-snapshot.gz");
            }
            laststate.createNewFile();
            File snapshot = File.createTempFile("snapshot", "gz");
            long t = System.currentTimeMillis();
            Diff.snapshot(dir, snapshot);
            if (laststate.length() > 0) {
                long n = Diff.compare(laststate, snapshot);
                System.out.println("Time: " + (System.currentTimeMillis() - t) + " ms");
                System.out.println("Processed: " + String.valueOf(n) + " items");
            }
            Files.move(snapshot.toPath(), laststate.toPath(), REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(Diff.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
