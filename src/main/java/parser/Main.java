package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static util.CopyFile.copyRemoteToLocal;
import static util.CopyFile.createSession;

/**
 * Raw data Parser for Telematics Device
 *
 * e.g. launch arguments for fmc130:
 *
 * - STD:
 * tlt.fmc130 tlt.fmc130.avlid tlt.fmc130.avlid.description -ts
 *
 * - SCP raw data file from remote server:
 * tlt.fmc130 tlt.fmc130.avlid tlt.fmc130.avlid.description user host port rsaPublicKeyFilePath rsaPublicKeyFilePassword("null" if none) tlt.fmc130.rawdata.int.live.remote.folder tlt.fmc130.rawdata.int.live.remote.file tlt.fmc130.rawdata.int.live.local.folder -ts
 *
 * -ts = timestamp
 */
public class Main {
    public static LinkedHashMap<Integer, String> DEVICE_AVL_ID;
    public static LinkedHashMap<String, String> DEVICE_AVL_ID_DESCRIPTION;
    public static LinkedHashMap<String, Instant> IMEI_LAST_AVL_DATA_TIMESTAMP;
    public static String MANUFACTURER;
    public static String DEVICE;
    public static List<TeltonikaFotaWebDeviceInfoBean> TELONIKA_FOTA_WEB_DEVICE_INFO_LIST;
    public static Boolean FLAG_TIME_STAMP = false;

    private static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String PROPERTY_MANUFACTURERS_DEVICES_FILE_NAME = "properties/ManufacturersDevices.properties";
    private static String TELTONIKA_FOTA_WEB_DEVICE_CSV_FILE = "fota/fota_device_export.csv";
    private static String INPUT_FILE_NAME = "data/in-raw.txt";
    private static String OUTPUT_FILE_NAME = "data/out-ndjson.txt";
    private static int IGNITION_CODE = 239;
    private static String NUMBER_OF_FILE_LINES_TO_TREAT = "*";    // "*" for all
    private static int N_WORST = 10;
    private static HashMap<String, Integer> imeiOccurence;
    private static String ARG_FLAG_TIME_STAMP = "-ts";

    private static int treatmentSwitch(AvlDataPacket avlDataPacket, Writer writer, int fileLineNumber, int matchLine) {

        // Standard
        matchLine++;
        int more = avlDataPacket.soutStd(writer, fileLineNumber, matchLine);

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

        return matchLine + more;
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

        // e.g.:
        // - args[0]=tlt.fmm130
        // - args[1]=tlt.fmm130.avlid
        // - args[2]=tlt.fmm130.avlid.description
        // see ManufacturersDevices.properties for more

        String deviceSelector = null;
        String manufacturerDeviceAvlId = null;
        String manufacturerDeviceAvlIdDescription = null;

        // To secure copy (scp) raw data file from remote server to local
        String user;
        String host;
        int port;

        String keyFilePath;
        String keyPassword;

        String remoteFolder;
        String remoteFile;
        String localFolder;

        if(args.length == 0 || args[0] == null)
        {
            System.out.println();
            System.out.println("Proper usage is args[0] args[1] args[2] as: manufacturer.device manufacturer.device.avlid manufacturer.device.avlid.description");
            System.exit(0);
        } else {
            deviceSelector = args[0];
        }

        MANUFACTURER = deviceSelector.split("\\.")[0];
        DEVICE = deviceSelector.split("\\.")[1];

        System.out.print("Selector: " + deviceSelector + ", ");
        System.out.print("Manufacturer: " + MANUFACTURER + ", ");
        System.out.print("Device: " + DEVICE + ", ");

        try (InputStream input = new FileInputStream(Main.class.getClassLoader().getResource(PROPERTY_MANUFACTURERS_DEVICES_FILE_NAME).getFile())) {
            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            manufacturerDeviceAvlId = prop.getProperty(args[1]);
            if(manufacturerDeviceAvlId != null && !manufacturerDeviceAvlId.isEmpty()) {
                System.out.print(manufacturerDeviceAvlId + ", ");
            }

            manufacturerDeviceAvlIdDescription = prop.getProperty(args[2]);
            if(manufacturerDeviceAvlIdDescription != null && !manufacturerDeviceAvlIdDescription.isEmpty()) {
                System.out.println(manufacturerDeviceAvlIdDescription);
            }

            if (args.length > 4) {  // Command line arguments contain remote server raw data file information
                user = args[3];
                host = args[4];
                port = Integer.parseInt(args[5]);

                keyFilePath = args[6];
                keyPassword = args[7].compareTo("null") == 0 ? null : args[7];

                remoteFolder = prop.getProperty(args[8]);
                remoteFile = prop.getProperty(args[9]);
                localFolder = prop.getProperty(args[10]);

                System.out.println(String.format("Connecting on %s as %s on port: %s", host, user, port));

                Session session = createSession(user, host, port, keyFilePath, keyPassword);

                System.out.println(String.format("Copying raw data file %s from remote to local, may take a while depending on file size..", remoteFolder + remoteFile));

                copyRemoteToLocal(session, remoteFolder, localFolder, remoteFile);

                File source = new File(localFolder + "\\" + remoteFile);
                File dest = new File(INPUT_FILE_NAME);

                FileUtils.copyFile(source, dest);
            }
        } catch (IOException | JSchException ex) {
            ex.printStackTrace();
        }

        if(manufacturerDeviceAvlId == null || manufacturerDeviceAvlId.isEmpty())
        {
            System.out.println("Proper usage in ManufacturersDevices.properties file is: manufacturer.device.avlid=file name (avl-id)");
            System.exit(0);
        }

        if(manufacturerDeviceAvlIdDescription == null || manufacturerDeviceAvlIdDescription.isEmpty())
        {
            System.out.println("Proper usage in ManufacturersDevices.properties file is: manufacturer.device.avlid.description=file name (avl-id with description)");
            System.exit(0);
        }

        File propertyIdFile = new File(manufacturerDeviceAvlId);
        DEVICE_AVL_ID = new LinkedHashMap<>();

        File propertyIdFileDescription = new File(manufacturerDeviceAvlIdDescription);
        DEVICE_AVL_ID_DESCRIPTION = new LinkedHashMap<>();

        List<AvlDataPacket> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        File file = new File(INPUT_FILE_NAME);
        LineIterator it = null;
        imeiOccurence = new HashMap<>();
        long fileLineCount=0;
        Writer fileTxtWriter = new FileWriter(OUTPUT_FILE_NAME, false); //overwrites file
        int fileLineNumber = 0;
        int matchLine = 0;
        char[] animationChars = new char[]{'|', '/', '-', '\\'};
        int processedPercentage = 0;
        long lineToTreat = 0;
        ObjectMapper mapper = new ObjectMapper();

        // ### Argument flags: BEGIN

        for(String arg: args) {

            // -ts for timestamp in raw data log for server gateway date
            // log without timestamp = imei;raw e.g. 864394040101331;000000000000008408010000...
            // log with timestamp = imei;timestamp;raw e.g. 864394040101331;2020-06-15T05:12:45.303Z;000000000000008408010000...
            if (arg.compareTo(ARG_FLAG_TIME_STAMP) == 0) {
                FLAG_TIME_STAMP = true;
            }
        }

        // ### Argument flags: END

        // ### Device AVL ID file

        try {
            it = FileUtils.lineIterator(propertyIdFile, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (it.hasNext()) {
                String str = it.nextLine();
                String[] parts = str.split(":");

                Integer key = Integer.parseInt( parts[0].trim());
                String value = parts[1].trim();

                DEVICE_AVL_ID.put(key, value);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        // sout DEVICE_AVL_ID
//        for (Map.Entry<Integer, String> entry : DEVICE_AVL_ID.entrySet()) {
//            Integer key = entry.getKey();
//            String value = entry.getValue();
//
//            System.out.println(key + " -> " + value);
//        }

        // ### Device AVL ID DESCRIPTION file

        try {
            it = FileUtils.lineIterator(propertyIdFileDescription, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (it.hasNext()) {
                String str = it.nextLine();
                String[] parts = str.split(":");

                String key = parts[0].trim();
                String value = parts[1].trim();

                DEVICE_AVL_ID_DESCRIPTION.put(key, value);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        // sout DEVICE_AVL_ID
//        for (Map.Entry<Integer, String> entry : DEVICE_AVL_ID.entrySet()) {
//            Integer key = entry.getKey();
//            String value = entry.getValue();
//
//            System.out.println(key + " -> " + value);
//        }

        // ###

        // ### Fota device file

        try (Reader csvReader = new BufferedReader(new FileReader(TELTONIKA_FOTA_WEB_DEVICE_CSV_FILE))) {

            Map<String, String> mapping = new HashMap<String, String>();
            mapping.put("imei", "imei");
            mapping.put("sn", "sn");
            mapping.put("model", "model");
            mapping.put("firmware", "firmware");
            mapping.put("configuration", "configuration");
            mapping.put("description", "description");
            mapping.put("companyname", "companyName");
            mapping.put("group", "group");
            mapping.put("lastlogin", "lastLogin");

            HeaderColumnNameTranslateMappingStrategy<TeltonikaFotaWebDeviceInfoBean> strategy = new HeaderColumnNameTranslateMappingStrategy<TeltonikaFotaWebDeviceInfoBean>();
            strategy.setType(TeltonikaFotaWebDeviceInfoBean.class);
            strategy.setColumnMapping(mapping);

            CsvToBean<TeltonikaFotaWebDeviceInfoBean> csvToBean = new CsvToBean<TeltonikaFotaWebDeviceInfoBean>();
            TELONIKA_FOTA_WEB_DEVICE_INFO_LIST = csvToBean.parse(strategy, csvReader);

//            // Debug
//            for(parser.TeltonikaFotaWebDeviceInfoBean t : TELONIKA_FOTA_WEB_DEVICE_INFO_LIST)
//            {
//                System.out.println(t);
//            }
        }

        // ### parser.Main process

        IMEI_LAST_AVL_DATA_TIMESTAMP = new LinkedHashMap<>();

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
                AvlDataPacket avlDataPacket;

                if (FLAG_TIME_STAMP) {
                    avlDataPacket = new AvlDataPacket(str,
                            str.substring(0, 15),
                            str.substring(16, 40),
                            str.substring(41, 49),
                            str.substring(49, 57),
                            str.substring(57, 59),
                            str.substring(59, 61),
                            str.substring(61));
                } else {
                    avlDataPacket = new AvlDataPacket(str,
                            str.substring(0, 15),
                            str.substring(16, 24),
                            str.substring(24, 32),
                            str.substring(32, 34),
                            str.substring(34, 36),
                            str.substring(36));
                }

                avlDataPacket.process();

                fileLineNumber++;
                matchLine = treatmentSwitch(avlDataPacket, fileTxtWriter, fileLineNumber, matchLine);

                processedPercentage = (int)Math.round((((double)fileLineNumber / (double)lineToTreat) * 100.0));
                System.out.print("Processing: " + processedPercentage + "% " + animationChars[processedPercentage % 4] + "\r");
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        System.out.println("Processing: Done!");
        System.out.print("Total: ");
        System.out.print(matchLine + " matched in " + lineToTreat);

        fileTxtWriter.close();

//        System.out.println();
//        System.out.println("the " + N_WORST + " worst");
//
//        final util.IntWrapper dWrapper = new util.IntWrapper(N_WORST);
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
