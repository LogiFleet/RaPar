package tltavlid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescriptionCleaner {

    // Teltonika FM3001
//    private static String TLT_DEVICE_AVL_ID_DESCRIPTION_IN_FILE_NAME = "avlid/TLT-FM3001-AVL-ID-DESCRIPTION-IN.txt";
//    private static String TLT_DEVICE_AVL_ID_DESCRIPTION_OUT_FILE_NAME = "avlid/TLT-FM3001-AVL-ID-DESCRIPTION-OUT.txt";

    // Teltonika FMM130
    private static String TLT_DEVICE_AVL_ID_DESCRIPTION_IN_FILE_NAME = "avlid/TLT-FMM130-AVL-ID-DESCRIPTION-IN.txt";
    private static String TLT_DEVICE_AVL_ID_DESCRIPTION_OUT_FILE_NAME = "avlid/TLT-FMM130-AVL-ID-DESCRIPTION-OUT.txt";

    public static void main(String[] args) throws IOException {
        File fileIn = new File(TLT_DEVICE_AVL_ID_DESCRIPTION_IN_FILE_NAME);
        LineIterator it = null;
        Writer fileTxtWriter = new FileWriter(TLT_DEVICE_AVL_ID_DESCRIPTION_OUT_FILE_NAME, false);

        try {
            it = FileUtils.lineIterator(fileIn, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String pattern1 = "^(.*?):(.*?):(.*?)$";
        String pattern2 = "([0-9]_–_.*?(?=$|_[0-9]))";
        String pattern3 = "([0-9])_–_(.*)"; // "1_–_Ignition_ON", g1="1", g2="Ignition_ON"

        // Create a Pattern object
        Pattern r1 = Pattern.compile(pattern1);
        Pattern r2 = Pattern.compile(pattern2);
        Pattern r3 = Pattern.compile(pattern3);

        try {
            while (it.hasNext()) {
                String str = it.nextLine();
                str = str.replaceAll("-", "–"); // Clean "bad" dash

                if (str.matches("^.*:(\\d_–_.*)")) {
                    Matcher m1 = r1.matcher(str);
                    if (m1.find()){
                        Matcher m2 = r2.matcher(str);
                        while (m2.find( )) {
                            Matcher m3 = r3.matcher(m2.group(1));
                            while (m3.find( )) {
                                fileTxtWriter.write(m1.group(1) + "." + m1.group(2) + "." + m3.group(1)+ ":" + m3.group(2) + "\r\n");
                            }
                        }
                    } else {
                        System.out.print("no find for: " + str);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LineIterator.closeQuietly(it);
        }

        fileTxtWriter.close();
    }

}
