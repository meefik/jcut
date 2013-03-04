package diff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardCopyOption.*;

/**
 *
 * @author Anton Skshidlevsky
 */
public class Diff {
    
    private static void scan(final String root, File out) throws IOException {
        Charset cs = Charset.defaultCharset();
        BufferedWriter buf = null;
        try {
            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(out)), cs));
            buf = bw;
            Path start = Paths.get(root);
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                private void writePath(String path, long time) throws IOException {
                    bw.write(path.replaceFirst(root, "") + "\t" +
                            String.valueOf(time) + "\n");                    
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
                {
                    writePath(file.toString(), attrs.lastModifiedTime().to(TimeUnit.SECONDS));
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                    IOException exc) throws IOException {
                    writePath(dir.toString(), 0);
                    return FileVisitResult.CONTINUE;
                }
            });
        } finally {
            closeQuietly(buf);
        }
    }
    
    private static void sort(File unsorted, File sorted) throws IOException {
        int maxtmpfiles = ExternalSort.DEFAULTMAXTEMPFILES;
        Charset cs = Charset.defaultCharset();
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String r1, String r2) {
                return r1.compareTo(r2);
            }
        };
        List<File> l = ExternalSort.sortInBatch(unsorted, 
                comparator, maxtmpfiles, cs, null);
        ExternalSort.mergeSortedFiles(l, sorted, comparator, cs);
    }
    
    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    public static void diff(File first, File second, File toFirst, 
            File toSecond) throws IOException {
        Charset cs = Charset.defaultCharset();
        BufferedWriter bw1 = null, bw2 = null;
        BufferedReader br1 = null, br2 = null;
        try {
            bw1 = new BufferedWriter(new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(toFirst)), cs));
            bw2 = new BufferedWriter(new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(toSecond)), cs));
            if (first.exists())
                br1 = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(first)), cs));
            if (second.exists())
                br2 = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(second)), cs));
            String line1 = br1 == null ? null : br1.readLine(),
                   line2 = br2 == null ? null : br2.readLine();
            while (line1 != null && line2 != null) {
                String[] arr1 = line1.split("\t"), arr2 = line2.split("\t");
                long hash1 = arr1[0].hashCode(), hash2 = arr2[0].hashCode();
                if (hash1 == hash2) {
                    if (arr1[1].hashCode() != arr2[1].hashCode()) {
                        long time1 = Long.valueOf(arr1[1]),
                             time2 = Long.valueOf(arr2[1]);
                        if (time1 > time2) {
                            bw2.write(line1+"\n");
                        }
                        if (time1 < time2) {
                            bw1.write(line2+"\n");
                        }
                    }
                    line1 = br1.readLine();
                    line2 = br2.readLine();
                    continue;
                }
                if (hash1 < hash2) {
                    bw2.write(line1+"\n");
                    line1 = br1.readLine();
                    continue;
                }
                if (hash1 > hash2) {
                    bw1.write(line2+"\n");
                    line2 = br2.readLine();
                    continue;
                }
            }
            while (line1 != null) {
                bw2.write(line1+"\n");
                line1 = br1.readLine();
            }
            while (line2 != null) {
                bw1.write(line2+"\n");
                line2 = br2.readLine();
            }
        } finally {
            closeQuietly(bw1);
            closeQuietly(bw2);
        }
    }

    public static void go(String dir, String out) {
        try {
            File unsortedList = File.createTempFile("unsorted", "gz");
            scan(dir, unsortedList);
            File sortedList = File.createTempFile("sorted", "gz");
            sort(unsortedList, sortedList);
            Files.delete(unsortedList.toPath());
            File lastsyncList = new File(out+"lastsync.gz");
            File modifiedList = new File(out+"modified.gz");
            File deletedList = new File(out+"deleted.gz");
            diff(lastsyncList, sortedList, modifiedList, deletedList);
            Files.move(sortedList.toPath(), lastsyncList.toPath(), REPLACE_EXISTING);
        
        } catch (IOException ex) {
            Logger.getLogger(Diff.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
