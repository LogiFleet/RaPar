
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

public class AvlData {

    private String raw;

    private String timeStamp;
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

    public AvlData(String raw) {
        this.raw = raw;

        //TODO Separate data and process -> service
        parse();
    }

    @Override
    public String toString() {
        return "data{" +
                "time='" + timeStamp + '\'' +
                ", prio='" + priority + '\'' +
                // Below output (lat, long) could directly be copied/pasted to https://www.google.com/maps/
                ", lat long=" + String.format("%.7f", latitude) + ", " + String.format("%.7f", longitude) +
                ", alt=" + String.format("%04d", altitude) +
                ", angle=" + String.format("%03d", angle) +
                ", sat=" + String.format("%02d", satellite) +
                ", speed=" + String.format("%03d", speed) +
                ", io=" + ioElement +
                '}';
    }

//    @Override
//    public String toString() {
//        return "AvlData{" +
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
        Instant instant = Instant.ofEpochMilli(Long.parseLong(raw.substring(0,16), 16));
        timeStamp = fmt.format(instant.atZone(ZoneId.systemDefault()));
        priority = raw.substring(16, 18);
        longitude = (float)(Integer.parseInt(raw.substring(18, 26), 16)) / 10000000;
        latitude = (float)(Integer.parseInt(raw.substring(26, 34), 16)) / 10000000;
        altitude = Short.parseShort(raw.substring(34, 38), 16);
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

        for (int i = 0; i < count; i++) {
            int key = Integer.parseInt(str.substring(0, 2), 16);
            str = str.substring(2);

            String value = String.format("%0" + size + "d", Long.parseUnsignedLong(str.substring(0, size), 16));
            str = str.substring(size);

            xByteElement.put(key, value);
        }

        return xByteElement;
    }

}
