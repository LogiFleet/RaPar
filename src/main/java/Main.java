import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String INPUT_FILE_NAME = "sampleLog.txt";
    private static int IGNITION_CODE = 239;
    private static int TESTING_OCCURRENCE = 5000;
    private static int N_WORST = 10;

    private static int fileLineNumber = 0;
    private static int fileLineTreated = 0;

    private static HashMap<String, Integer> imeiOccurancy;

    private static void treatmentSwitch(AvlDataPacket avlDataPacket) {

        // Standard
//        fileLineTreated++;
//        standardOutput(avlDataPacket);

        // ### Missing odometer issue
        if (!containFourByteElements(avlDataPacket)
                && !isGPRSCommandAcknowledgeMessage(avlDataPacket)
                && atLeastOneIgnitionEventInPacket(avlDataPacket)) {
            fileLineTreated++;
//            standardOutput(avlDataPacket);

            // ### Additional data extraction for further analysis

            if (!imeiOccurancy.containsKey(avlDataPacket.getImei())) {
                imeiOccurancy.put(avlDataPacket.getImei(), 1);
            } else {
                imeiOccurancy.computeIfPresent(avlDataPacket.getImei(), (k, v) -> v + 1);
            }

            // ###
        }
        // ###

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

    /**
     * Standard output.
     * @param avlDataPacket
     */
    private static void standardOutput(AvlDataPacket avlDataPacket) {
        System.out.println(fileLineNumber + " / " + fileLineTreated);
        avlDataPacket.soutStd();
    }

    public static void main(String[] args) {
        List<AvlDataPacket> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        File file = new File(Main.class.getClassLoader().getResource(INPUT_FILE_NAME).getFile());
        LineIterator it = null;
        imeiOccurancy = new HashMap<>();

        try {
            it = FileUtils.lineIterator(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (it.hasNext() && fileLineTreated < TESTING_OCCURRENCE) {
                String str = it.nextLine();
                AvlDataPacket avlDataPacket = new AvlDataPacket(str,
                    str.substring(0, 15),
                    str.substring(16, 24),
                    str.substring(24, 32),
                    str.substring(32, 34),
                    str.substring(34, 36),
                    str.substring(36));

                fileLineNumber++;
                treatmentSwitch(avlDataPacket);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        System.out.println();
        System.out.println("Total");
        System.out.println(fileLineNumber + " / " + fileLineTreated);

        System.out.println();
        System.out.println("the " + N_WORST + " worst");

        final IntWrapper dWrapper = new IntWrapper(N_WORST);

        imeiOccurancy.entrySet()
                .stream().limit(N_WORST)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(k -> {
                    System.out.println(String.format("%03d", dWrapper.value) + ". " + k.getKey() + " = " + k.getValue());
                    dWrapper.value--;
                });
    }
}
