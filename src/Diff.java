
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Отсеживание изменений в каталоге
 *
 * @author Anton Skshidlevsky
 */
public class Diff {

    private static void scan(String root, File path, BufferedWriter bw) throws IOException {
        File[] list = path.listFiles();
        if (list == null) {
            return;
        }
        Arrays.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        for (File f : list) {
            //System.out.println(f.getPath());
            long lastModified = 0;
            if (f.isDirectory()) {
                scan(root, f, bw);
            } else {
                lastModified = f.lastModified();
            }
            bw.write(f.getPath().replaceFirst(root, "") + "\t"
                    + String.valueOf(lastModified) + "\n");
        }
    }

    public static void snapshot(File path, File out) throws IOException {
        Charset cs = Charset.defaultCharset();
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new GZIPOutputStream(
                                new FileOutputStream(out)), cs));) {
                            scan(path.getPath(), path, bw);
                        }
    }

    public static void diff(File first, File second) throws IOException {
        Charset cs = Charset.defaultCharset();
        try (BufferedReader br1 = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(first)), cs));
                BufferedReader br2 = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(second)), cs));) {
            String line1 = br1 == null ? null : br1.readLine(),
                   line2 = br2 == null ? null : br2.readLine();
            while (line1 != null && line2 != null) {
                String[] arr1 = line1.split("\t"), arr2 = line2.split("\t");
                String path1 = arr1[0], path2 = arr2[0];
                long cmp = path1.compareTo(path2);
                if (cmp == 0) {
                    long time1 = Long.valueOf(arr1[1]),
                         time2 = Long.valueOf(arr2[1]);
                    if (time1 != time2) {
                        // Mod file
                        if (time1 > time2) {
                            System.out.println("mod" + "\t" + line1);
                        }
                        if (time1 < time2) {
                            System.out.println("mod" + "\t" + line2);
                        }
                    }
                    line1 = br1.readLine();
                    line2 = br2.readLine();
                    continue;
                }
                // Del file
                if (cmp < 0) {
                    System.out.println("del" + "\t" + line1);
                    line1 = br1.readLine();
                    continue;
                }
                // Add file
                if (cmp > 0) {
                    System.out.println("add" + "\t" + line2);
                    line2 = br2.readLine();
                    continue;
                }
            }
            // Del dir
            while (line1 != null) {
                System.out.println("del1" + "\t" + line1);
                line1 = br1.readLine();
            }
            // Add dir
            while (line2 != null) {
                System.out.println("add1" + "\t" + line2);
                line2 = br2.readLine();
            }
        } catch (Exception ex) {
            Logger.getLogger(Diff.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
