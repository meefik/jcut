
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

    /**
     * Рекурсивное сканирование файлов и каталогов
     * @param root корневая директория
     * @param path текущая директория
     * @param bw буфер для сохранения списка файлов и каталогов
     * @throws IOException 
     */
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

    /**
     * Создать снимок каталога, 
     * сохранить список всех файлов и подкаталогов в файл
     * @param path директория сканирования
     * @param out файл для сохранения снимка
     * @throws IOException 
     */
    public static void snapshot(File path, File out) throws IOException {
        Charset cs = Charset.defaultCharset();
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new GZIPOutputStream(
                                new FileOutputStream(out)), cs));) {
                            scan(path.getPath(), path, bw);
                        }
    }

    /**
     * Сравнение двух снимков
     * @param first файл предыдущего снимка
     * @param second файл текущего снимка
     * @return число обработанных строк снимка (файлов)
     * @throws IOException 
     */
    public static long compare(File first, File second) throws IOException {
        long n = 0;
        Charset cs = Charset.defaultCharset();
        try (BufferedReader br1 = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(first)), cs));
                BufferedReader br2 = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(second)), cs));) {
            String line1 = br1 == null ? null : br1.readLine(),
                   line2 = br2 == null ? null : br2.readLine();
            while (line1 != null || line2 != null) {
                n++;
                String path1 = "", path2 = "";
                long time1 = 0, time2 = 0, cmp = 0;
                if (line1 != null) {
                    String[] arr = line1.split("\t");
                    path1 = arr[0];
                    time1 = Long.valueOf(arr[1]);
                    cmp = -1;
                }
                if (line2 != null) {
                    String[] arr = line2.split("\t");
                    path2 = arr[0];
                    time2 = Long.valueOf(arr[1]);
                    cmp = 1;
                }
                if (line1 != null && line2 != null) {
                    cmp = path2.compareTo(path1);
                }
                if (cmp == 0) {
                    if (time1 > time2) {
                        System.out.println("mod" + "\t" + line1);
                    }
                    if (time1 < time2) {
                        System.out.println("mod" + "\t" + line2);
                    }
                    line1 = br1.readLine();
                    line2 = br2.readLine();
                    continue;
                }
                // Deleted
                if (cmp < 0) {
                    System.out.println("del" + "\t" + line1);
                    line1 = br1.readLine();
                    continue;
                }
                // Added
                if (cmp > 0) {
                    System.out.println("add" + "\t" + line2);
                    line2 = br2.readLine();
                    continue;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Diff.class.getName()).log(Level.SEVERE, null, ex);
        }
        return n;
    }

}
