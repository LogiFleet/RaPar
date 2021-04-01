package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static util.CopyFile.copyRemoteToLocal;
import static util.CopyFile.createSession;

/**
 * Raw data Parser for Telematics Device
 */
public class Main {
    public static LinkedHashMap<Integer, String> DEVICE_AVL_ID;
    public static LinkedHashMap<String, String> DEVICE_AVL_ID_DESCRIPTION;
    public static LinkedHashMap<String, Instant> IMEI_LAST_AVL_DATA_TIMESTAMP;
    public static LinkedHashMap<String, String> IMEI_NAME;
    public static LinkedHashMap<String, Boolean> IMEI_DIGITAL_INPUT_2_STATE_HAS_CHANGED;
    public static LinkedHashMap<String, Instant> IMEI_I_BUTTON_VALUE_HAS_CHANGED;
    public static String MANUFACTURER;
    public static String DEVICE;
    public static List<TeltonikaFotaWebDeviceInfoBean> TELONIKA_FOTA_WEB_DEVICE_INFO_LIST;
    public static boolean FLAG_TIME_STAMP = false;
    public static boolean FLAG_RAW_DATA = false;

    private static final String AVL_ID_SUFFIX = "avlid";
    private static final String AVL_ID_DESCRIPTION_SUFFIX = "description";

    private static final String OPTION_MANUFACTURER = "man";
    private static final String OPTION_MANUFACTURER_DESCRIPTION = "Manufacturer";
    private static final String OPTION_DEVICE = "dev";
    private static final String OPTION_DEVICE_DESCRIPTION = "Device";

    private static final String OPTION_SCP = "scp";
    private static final String OPTION_SCP_DESCRIPTION = "Secure copy file";

    private static final String OPTION_USR = "usr";
    private static final String OPTION_USR_DESCRIPTION = "User";
    private static final String OPTION_HST = "hst";
    private static final String OPTION_HST_DESCRIPTION = "Host";
    private static final String OPTION_PRT = "prt";
    private static final String OPTION_PRT_DESCRIPTION = "Port";
    private static final String OPTION_KEY = "key";
    private static final String OPTION_KEY_DESCRIPTION = "Key";
    private static final String OPTION_PWD = "pwd";
    private static final String OPTION_PWD_DESCRIPTION = "Password";
    private static final String OPTION_RFD = "rfd";
    private static final String OPTION_RFD_DESCRIPTION = "Remote folder";
    private static final String OPTION_RFL = "rfl";
    private static final String OPTION_RFL_DESCRIPTION = "Remote file";
    private static final String OPTION_LFD = "lfd";
    private static final String OPTION_LFD_DESCRIPTION = "Local folder";

    private static final String OPTION_TS = "ts";
    private static final String OPTION_TS_DESCRIPTION = "Time stamp";
    private static final String OPTION_RD = "rd";
    private static final String OPTION_RD_DESCRIPTION = "Raw data";

    private static final String PROPERTY_MANUFACTURERS_DEVICES_FILE_NAME = "properties/ManufacturersDevices.properties";
    private static final String TELTONIKA_FOTA_WEB_DEVICE_EXPORT_FOLDER = "teltonikafotawebexport";
    private static final String TELTONIKA_FOTA_WEB_DEVICE_EXPORT_FILE_EXTENSION = "csv";
    private static final String IMEI_NAME_FILE_NAME = "msc/imeiName.txt";
    private static final String INPUT_FILE_NAME = "data/in-raw.txt";
    private static final String OUTPUT_FILE_NAME = "data/out-ndjson.txt";

    private static final String NUMBER_OF_FILE_LINES_TO_TREAT = "*";    // "*" for all

    /**
     * Add a dot character separator between every keys
     *
     * @param args keyLevel1 keyLevel2 keyLevel3
     * @return keyLevel1.keyLevel2.keyLevel3
     */
    static String keyDotSeparatedBuilder(String... args) {
        String key = "";

        for (String str : args) {
            key += str + '.';
        }

        // remove last '.' from key String
        return key.substring(0, key.length() - 1);
    }

    private static CommandLine parseCommandLineArguments(String... args) {
        CommandLine cmd = null;
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(OPTION_MANUFACTURER, true, OPTION_MANUFACTURER_DESCRIPTION);
        options.addOption(OPTION_DEVICE, true, OPTION_DEVICE_DESCRIPTION);

        options.addOption(OPTION_SCP, false, OPTION_SCP_DESCRIPTION);

        options.addOption(OPTION_USR, true, OPTION_USR_DESCRIPTION);
        options.addOption(OPTION_HST, true, OPTION_HST_DESCRIPTION);
        options.addOption(OPTION_PRT, true, OPTION_PRT_DESCRIPTION);
        options.addOption(OPTION_KEY, true, OPTION_KEY_DESCRIPTION);
        options.addOption(OPTION_PWD, true, OPTION_PWD_DESCRIPTION);
        options.addOption(OPTION_RFD, true, OPTION_RFD_DESCRIPTION);
        options.addOption(OPTION_RFL, true, OPTION_RFL_DESCRIPTION);
        options.addOption(OPTION_LFD, true, OPTION_LFD_DESCRIPTION);

        options.addOption(OPTION_TS, false, OPTION_TS_DESCRIPTION);
        options.addOption(OPTION_RD, false, OPTION_RD_DESCRIPTION);

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cmd;
    }

    private static void sysOutFormatted(String key, String value) {
        System.out.println(String.format("%-25s", key + ":").replace(' ', '_') + value);
    }

    /**
     * To secure copy (scp) raw data file from remote server to local
     *
     * @param commandLine
     */
    private static void secureCopyFileFromRemoteServer(CommandLine commandLine) {
        String user, host, port;
        String keyFilePath, keyPassword;
        String remoteFolder, remoteFile, localFolder;

        user = commandLine.getOptionValue(OPTION_USR);
        host = commandLine.getOptionValue(OPTION_HST);
        port = commandLine.getOptionValue(OPTION_PRT);

        keyFilePath = commandLine.getOptionValue(OPTION_KEY);
        keyPassword = commandLine.hasOption(OPTION_KEY) ? commandLine.getOptionValue(OPTION_PWD) : null;

        remoteFolder = commandLine.getOptionValue(OPTION_RFD);
        remoteFile = commandLine.getOptionValue(OPTION_RFL);
        localFolder = commandLine.getOptionValue(OPTION_LFD);

        sysOutFormatted(OPTION_USR_DESCRIPTION, user);
        sysOutFormatted(OPTION_HST_DESCRIPTION, host);
        sysOutFormatted(OPTION_PRT_DESCRIPTION, port);

        sysOutFormatted(OPTION_KEY_DESCRIPTION, keyFilePath);
        sysOutFormatted(OPTION_PWD_DESCRIPTION, keyPassword);

        sysOutFormatted(OPTION_RFD_DESCRIPTION, remoteFolder);
        sysOutFormatted(OPTION_RFL_DESCRIPTION, remoteFile);
        sysOutFormatted(OPTION_LFD_DESCRIPTION, localFolder);

        System.out.println();
        System.out.println(String.format("Connecting on %s as %s on port: %s", host, user, port));

        Session session = createSession(user, host, Integer.parseInt(port), keyFilePath, keyPassword);

        System.out.println(String.format("Copying raw data file %s from remote to local, may take a while depending on file size..", remoteFolder + remoteFile));

        try {
            copyRemoteToLocal(session, remoteFolder, localFolder, remoteFile);

            File source = new File(localFolder + "\\" + remoteFile);
            File dest = new File(INPUT_FILE_NAME);
            FileUtils.copyFile(source, dest);
            System.out.println();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        CommandLine commandLine;
        String deviceSelector;
        String manufacturerDeviceAvlId;
        String manufacturerDeviceAvlIdDescription;
        InputStream propertyFile;
        boolean scpOption;

        commandLine = parseCommandLineArguments(args);

        MANUFACTURER = commandLine.getOptionValue(OPTION_MANUFACTURER);
        DEVICE = commandLine.getOptionValue(OPTION_DEVICE);
        deviceSelector = keyDotSeparatedBuilder(MANUFACTURER, DEVICE);

        sysOutFormatted("Selector", deviceSelector);
        sysOutFormatted(OPTION_MANUFACTURER_DESCRIPTION, MANUFACTURER);
        sysOutFormatted(OPTION_DEVICE_DESCRIPTION, DEVICE);

        propertyFile = new FileInputStream(Main.class.getClassLoader().getResource(PROPERTY_MANUFACTURERS_DEVICES_FILE_NAME).getFile());
        Properties properties = new Properties();
        properties.load(propertyFile);

        // get the property value and print it out
        manufacturerDeviceAvlId = properties.getProperty(keyDotSeparatedBuilder(deviceSelector, AVL_ID_SUFFIX));
        if (manufacturerDeviceAvlId != null && !manufacturerDeviceAvlId.isEmpty()) {
            sysOutFormatted("AVL ID", manufacturerDeviceAvlId);
        }

        manufacturerDeviceAvlIdDescription = properties.getProperty(keyDotSeparatedBuilder(deviceSelector, AVL_ID_SUFFIX, AVL_ID_DESCRIPTION_SUFFIX));
        if (manufacturerDeviceAvlIdDescription != null && !manufacturerDeviceAvlIdDescription.isEmpty()) {
            sysOutFormatted("AVL ID DESCRIPTION", manufacturerDeviceAvlIdDescription);
        }

        scpOption = commandLine.hasOption(OPTION_SCP);

        if (scpOption) {
            System.out.println();
        }

        sysOutFormatted("SCP Option", String.valueOf(scpOption));
        System.out.println();

        if (scpOption) {
            secureCopyFileFromRemoteServer(commandLine);
        }

        File propertyIdFile = new File(manufacturerDeviceAvlId);
        DEVICE_AVL_ID = new LinkedHashMap<>();

        File propertyIdFileDescription = new File(manufacturerDeviceAvlIdDescription);
        DEVICE_AVL_ID_DESCRIPTION = new LinkedHashMap<>();

        File file = new File(INPUT_FILE_NAME);
        LineIterator it = null;
        long fileLineCount = 0;
        Writer fileTxtWriter = new FileWriter(OUTPUT_FILE_NAME, false); //overwrites file
        int fileLineNumber = 0;
        int matchLine = 0;
        char[] animationChars = new char[]{'|', '/', '-', '\\'};
        int processedPercentage = 0;
        long lineToTreat = 0;
        ObjectMapper mapper = new ObjectMapper();

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

                Integer key = Integer.parseInt(parts[0].trim());
                String value = parts[1].trim();

                DEVICE_AVL_ID.put(key, value);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

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

        // ### FOTA Web export device info file dropped in teltonikafotawebexport folder as is

        Optional<String> firstFile = Optional.empty();

        try {
            firstFile = findFirstFile(Paths.get(TELTONIKA_FOTA_WEB_DEVICE_EXPORT_FOLDER), TELTONIKA_FOTA_WEB_DEVICE_EXPORT_FILE_EXTENSION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (firstFile.isPresent()) {
            TELONIKA_FOTA_WEB_DEVICE_INFO_LIST = new CsvToBeanBuilder<TeltonikaFotaWebDeviceInfoBean>(new FileReader(firstFile.get()))
                    .withType(TeltonikaFotaWebDeviceInfoBean.class).build().parse();
        }

        // ### Imei=key separator ":" name=value file -> msc/imeiName.txt

        File imeiName = new File(IMEI_NAME_FILE_NAME);
        IMEI_NAME = new LinkedHashMap<>();

        try {
            it = FileUtils.lineIterator(imeiName, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (it.hasNext()) {
                String str = it.nextLine();
                String[] parts = str.split(":");

                String key = parts[0].trim();
                String value = parts[1].trim();

                IMEI_NAME.put(key, value);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        // ### Command line flag arguments

        FLAG_TIME_STAMP = commandLine.hasOption(OPTION_TS);
        FLAG_RAW_DATA = commandLine.hasOption(OPTION_RD);

        // ### Digital Input 2 (for Business / Private switch mode use case)

        IMEI_DIGITAL_INPUT_2_STATE_HAS_CHANGED = new LinkedHashMap<>();

        // ### iButton for driver authentication

        IMEI_I_BUTTON_VALUE_HAS_CHANGED = new LinkedHashMap<>();

        // ### Parser Main Process

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

                // Size of timestamp is dynamic, because sometimes there is and sometimes there is no millisecond at the end, e.g.:
                // - 2020-11-09T06:44:50.120Z
                // - 2020-11-09T06:44:54Z
                int zuluTimeZPosition = str.indexOf('Z');
                int zuluTimeZPositionOffset;

                if (zuluTimeZPosition == 39) {
                    zuluTimeZPositionOffset = 0;
                } else if (zuluTimeZPosition == 35) {
                    zuluTimeZPositionOffset = -4;
                } else {
                    throw new java.lang.RuntimeException("Z, for zulu time, in log timestamp, bad position.");
                }

                if (FLAG_TIME_STAMP) {
                    avlDataPacket = new AvlDataPacket(str,
                            str.substring(0, 15),
                            str.substring(16, 40 + zuluTimeZPositionOffset),
                            str.substring(41 + zuluTimeZPositionOffset, 49 + zuluTimeZPositionOffset),
                            str.substring(49 + zuluTimeZPositionOffset, 57 + zuluTimeZPositionOffset),
                            str.substring(57 + zuluTimeZPositionOffset, 59 + zuluTimeZPositionOffset),
                            str.substring(59 + zuluTimeZPositionOffset, 61 + zuluTimeZPositionOffset),
                            str.substring(61 + zuluTimeZPositionOffset));
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

                matchLine++;
                int more = avlDataPacket.soutStd(fileTxtWriter, fileLineNumber, matchLine);
                matchLine += more;

                processedPercentage = (int) Math.round((((double) fileLineNumber / (double) lineToTreat) * 100.0));
                System.out.print("Processing: " + processedPercentage + "% " + animationChars[processedPercentage % 4] + "\r");
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        System.out.println("Processing: Done!");
        System.out.print("Total: ");
        System.out.print(matchLine + " matched in " + lineToTreat);
        System.out.println();

        fileTxtWriter.close();
    }

    public static Optional<String> findFirstFile(Path path, String fileExtension)
            throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        Optional<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    // this is a path, not string,
                    // this only test if path end with a certain path
                    //.filter(p -> p.endsWith(fileExtension))
                    // convert path to string first
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .findFirst();
        }

        return result;
    }

}
