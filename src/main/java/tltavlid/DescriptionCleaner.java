package tltavlid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescriptionCleaner {

    private static String TLT_DEVICE_AVL_ID_DESCRIPTION_IN_FILE_NAME = "avlid/TLT-FM3001-AVL-ID-DESCRIPTION-IN.txt";
    private static String TLT_DEVICE_AVL_ID_DESCRIPTION_OUT_FILE_NAME = "avlid/TLT-FM3001-AVL-ID-DESCRIPTION-OUT.txt";

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

        // Create a Pattern object
        Pattern r1 = Pattern.compile(pattern1);
        Pattern r2 = Pattern.compile(pattern2);

        try {
            while (it.hasNext()) {
                String str = it.nextLine();
                str = str.replaceAll("-", "–"); // Clean "bad" dash
//                String[] parts = str.split(":");

                if (str.matches("^.*:(\\d_–_.*)")) {

                    Matcher m1 = r1.matcher(str);

                    if (m1.find()){
                        System.out.print("m1g1: " + m1.group(1) + ", ");
                        System.out.print("m1g2: " + m1.group(2) + ", ");
                    } else {
                        System.out.print("no find for: " + str);
                    }

                    Matcher m2 = r2.matcher(str);

                    while (m2.find( )) {
                        System.out.print("m2g1: " + m2.group(1) + ", ");
                    }

                    System.out.println();

                    fileTxtWriter.write(str + "\r\n");
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
