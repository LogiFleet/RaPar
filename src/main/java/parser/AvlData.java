package parser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

import static parser.Main.FLAG_TIME_STAMP;

public class AvlData {

    private AvlDataPacket avlDataPacket;

    private String raw;

    private String timeStamp;
    private String gatewayDate;
    private String priority;
    private float longitude;
    private float latitude;
    private Short altitude;
    private Short angle;
    private Byte satellite;
    private Short speed;

    private String ioElementRaw;
    private IOElement ioElement;

    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AvlData(AvlDataPacket avlDataPacket, String raw) {
        this.avlDataPacket = avlDataPacket;
        this.raw = raw;

        //TODO Separate data and process -> service
        parse();
    }

    @Override
    public String toString() {
        return  (FLAG_TIME_STAMP ? ("\"gatewayDate\":\"" + gatewayDate + "\",") : ("")) +
                "\"timeStamp\":\"" + timeStamp + '\"' +
                ",\"priority\":\"" + priority + '\"' +
                ",\"location\":{" +
                    "\"lat\":" + String.format("%.7f", latitude) +
                    ",\"lon\":" + String.format("%.7f", longitude) +
                "}" +
                ",\"altitude\":" + altitude +
                ",\"angle\":" + angle +
                ",\"satellite\":" + satellite +
                ",\"speed\":" + speed +
                "," + ioElement;
    }

//    @Override
//    public String toString() {
//        return "parser.AvlData{" +
////                "raw='" + raw + '\'' +
//                "timeStamp='" + timeStamp + '\'' +
//                ", priority='" + priority + '\'' +
//                // Below output (lat, long) could directly be copied/pasted to https://www.google.com/maps/
//                ", latitude longitude=" + String.format("%.7f", latitude) + ", " + String.format("%.7f", longitude) +
//                ", altitude=" + String.format("%04d", altitude) +
//                ", angle=" + String.format("%03d", angle) +
//                ", satellite=" + String.format("%02d", satellite) +
//                ", speed=" + String.format("%03d", speed) +
////                ", ioElementRaw='" + ioElementRaw + '\'' +
//                ", ioElement=" + ioElement +
//                '}';
//    }

    private void parse(){
        Instant instant;

        if (FLAG_TIME_STAMP) {
            instant = Instant.parse(avlDataPacket.getTimeStamp());
            gatewayDate = fmt.format(instant.atZone(ZoneId.systemDefault()));
        }

        instant = Instant.ofEpochMilli(Long.parseLong(raw.substring(0,16), 16));
        timeStamp = fmt.format(instant.atZone(ZoneId.systemDefault()));

        priority = raw.substring(16, 18);

        longitude = (float)(Integer.parseInt(raw.substring(18, 26), 16)) / 10000000;
        latitude = (float)(Integer.parseInt(raw.substring(26, 34), 16)) / 10000000;

        // fix negative altitude eg. "alt=-045"
        try {
            altitude = (short)(Integer.parseInt(raw.substring(34, 38), 16));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        angle = Short.parseShort(raw.substring(38, 42), 16);
        satellite = Byte.parseByte(raw.substring(42, 44), 16);
        speed = Short.parseShort(raw.substring(44, 48), 16);
        ioElementRaw = raw.substring(48);

        int eventID = Integer.parseInt(raw.substring(48, 50), 16);
        int elementCount = Integer.parseInt(raw.substring(50, 52), 16);

        String str;

        // 1 byte
        str = raw.substring(52);
        int oneByteElementCount = Integer.parseInt(str.substring(0, 2), 16);
        LinkedHashMap<Integer, String> oneByteElement = parseXbIOElement(str.substring(2), oneByteElementCount, 2);

        // 2 bytes
        str = str.substring(2 + (oneByteElementCount * 2) + (oneByteElementCount * 2));
        int twoByteElementCount = Integer.parseInt(str.substring(0, 2), 16);
        LinkedHashMap<Integer, String> twoByteElement = parseXbIOElement(str.substring(2), twoByteElementCount, 4);

        // 4 bytes
        str = str.substring(2 + (twoByteElementCount * 2) + (twoByteElementCount * 4));
        int fourByteElementCount = Integer.parseInt(str.substring(0, 2), 16);
        LinkedHashMap<Integer, String> fourByteElement = parseXbIOElement(str.substring(2), fourByteElementCount, 8);

        // 8 bytes
        str = str.substring(2 + (fourByteElementCount * 2) + (fourByteElementCount * 8));
        int eightByteElementCount = Integer.parseInt(str.substring(0, 2), 16);
        LinkedHashMap<Integer, String> eightByteElement = parseXbIOElement(str.substring(2), eightByteElementCount, 16);

        ioElement = new IOElement(eventID, elementCount,
                oneByteElementCount, oneByteElement,
                twoByteElementCount, twoByteElement,
                fourByteElementCount, fourByteElement,
                eightByteElementCount, eightByteElement);
    }

    private LinkedHashMap<Integer, String> parseXbIOElement(String str, int count, int size) {
        LinkedHashMap<Integer, String> xByteElement = new LinkedHashMap<>();
        String value;

        for (int i = 0; i < count; i++) {
            int key = Integer.parseInt(str.substring(0, 2), 16);
            str = str.substring(2);

            if (key != 78) {    // 78 Property ID is iButton ID for Teltonika FMM130
                value = String.valueOf(Long.parseUnsignedLong(str.substring(0, size), 16));
            } else {
                String hexID = str.substring(0, size);
                value = "0000000000000000".compareTo(hexID) == 0 ? "0" : hexID.toUpperCase();
            }

            str = str.substring(size);

            xByteElement.put(key, value);
        }

        return xByteElement;
    }

    public String getRaw() {
        return raw;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getPriority() {
        return priority;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public Short getAltitude() {
        return altitude;
    }

    public Short getAngle() {
        return angle;
    }

    public Byte getSatellite() {
        return satellite;
    }

    public Short getSpeed() {
        return speed;
    }

    public String getIoElementRaw() {
        return ioElementRaw;
    }

    public IOElement getIoElement() {
        return ioElement;
    }

    public DateTimeFormatter getFmt() {
        return fmt;
    }

}
