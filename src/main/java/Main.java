import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    private static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String INPUT_FILE_NAME = "sampleLog.txt";
    private static String OUTPUT_TXT_FILE_NAME = "output.txt";
    private static String OUTPUT_JSON_FILE_NAME = "output.json";
    private static int IGNITION_CODE = 239;
    private static String NUMBER_OF_FILE_LINES_TO_TREAT = "10";    // "*" for all
    private static int N_WORST = 10;
    private static HashMap<String, Integer> imeiOccurence;

    private static int treatmentSwitch(AvlDataPacket avlDataPacket, Writer writer, int fileLineNumber, int matchLine) {

        // Standard
        matchLine++;
        avlDataPacket.soutStd(writer, fileLineNumber, matchLine);

        // ### Missing odometer issue
//        if (!containFourByteElements(avlDataPacket)
//                && !isGPRSCommandAcknowledgeMessage(avlDataPacket)
//                && atLeastOneIgnitionEventInPacket(avlDataPacket)) {
//
//            matchLine++;
//            avlDataPacket.soutStd(writer, fileLineNumber, matchLine);
//
//            // ### Additional data extraction for further analysis
//
//            if (!imeiOccurence.containsKey(avlDataPacket.getImei())) {
//                imeiOccurence.put(avlDataPacket.getImei(), 1);
//            } else {
//                imeiOccurence.computeIfPresent(avlDataPacket.getImei(), (k, v) -> v + 1);
//            }
//
//            // ###
//        }
        // ###

        return matchLine;
    }

    private static boolean atLeastOneIgnitionEventInPacket(AvlDataPacket avlDataPacket) {
        List<AvlData> avlDataList = avlDataPacket.getAvlDataList();

        for (AvlData avlData : avlDataList) {
            if (avlData.getIoElement().getEventID() == IGNITION_CODE) {
                return true;
            }
        }

        return false;
    }

    private static boolean isGPRSCommandAcknowledgeMessage(AvlDataPacket avlDataPacket) {
        // GPRS acknowledge message are sent with '0c' codec
        return "0c".equalsIgnoreCase(avlDataPacket.getCodecID());
    }

    /**
     * Method to check if message contain 4 Byte elements
     * Used to put in evidence messages without odometer IO property (99), use case.
     * @param avlDataPacket
     * @return
     */
    private static boolean containFourByteElements(AvlDataPacket avlDataPacket) {
        int fourByteElementCount = 0;

        List<AvlData> avlDataList = avlDataPacket.getAvlDataList();

        for (AvlData avlData : avlDataList) {
            if (avlData.getIoElement().getFourByteElementCount() != 0) {
                fourByteElementCount++;
            }
        }

        return fourByteElementCount != 0;
    }

    public static void main(String[] args) throws IOException {
        List<AvlDataPacket> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        File file = new File(Main.class.getClassLoader().getResource(INPUT_FILE_NAME).getFile());
        LineIterator it = null;
        imeiOccurence = new HashMap<>();
        long fileLineCount=0;
        Writer fileTxtWriter = new FileWriter(OUTPUT_TXT_FILE_NAME, false); //overwrites file
        Writer fileJsontWriter = new FileWriter(OUTPUT_JSON_FILE_NAME, false); //overwrites file
        int fileLineNumber = 0;
        int matchLine = 0;
        char[] animationChars = new char[]{'|', '/', '-', '\\'};
        int processedPercentage = 0;
        long lineToTreat = 0;
        ObjectMapper mapper = new ObjectMapper();

        try (Stream<String> lines = Files.lines(file.toPath())) {
            fileLineCount = lines.count();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lineToTreat = "*".equals(NUMBER_OF_FILE_LINES_TO_TREAT) ? fileLineCount : Long.parseLong(NUMBER_OF_FILE_LINES_TO_TREAT);
        lineToTreat = lineToTreat > fileLineCount ? fileLineCount : lineToTreat;

        System.out.println(lineToTreat + " lines of " + fileLineCount);

        try {
            it = FileUtils.lineIterator(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (it.hasNext() && fileLineNumber < lineToTreat) {
                String str = it.nextLine();
                AvlDataPacket avlDataPacket = new AvlDataPacket(str,
                    str.substring(0, 15),
                    str.substring(16, 24),
                    str.substring(24, 32),
                    str.substring(32, 34),
                    str.substring(34, 36),
                    str.substring(36));

                fileLineNumber++;
                matchLine = treatmentSwitch(avlDataPacket, fileTxtWriter, fileLineNumber, matchLine);

                processedPercentage = (int)Math.round((((double)fileLineNumber / (double)lineToTreat) * 100.0));
                System.out.print("Processing: " + processedPercentage + "% " + animationChars[processedPercentage % 4] + "\r");

                try {
                    //TODO JSON output improve, do not jsonify every fields!
                    fileJsontWriter.write(mapper.writeValueAsString(avlDataPacket) + "\r\n");
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        System.out.println("Processing: Done!          ");

        System.out.println();
        System.out.println("Total");
        System.out.println(matchLine + " matched in " + lineToTreat);

        fileTxtWriter.close();
        fileJsontWriter.close();

//        System.out.println();
//        System.out.println("the " + N_WORST + " worst");
//
//        final IntWrapper dWrapper = new IntWrapper(N_WORST);
//
//        imeiOccurence.entrySet()
//                .stream().limit(N_WORST)
//                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//                .forEach(k -> {
//                    System.out.println(String.format("%03d", dWrapper.value) + ". " + k.getKey() + " = " + k.getValue());
//                    dWrapper.value--;
//                });

    }

}
