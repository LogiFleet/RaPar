package parser;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class IOElement {

    private int eventID;
    private int elementCount;

    private int oneByteElementCount;
    private LinkedHashMap<Integer, String> oneByteElement;

    private int twoByteElementCount;
    private LinkedHashMap<Integer, String> twoByteElement;

    private int fourByteElementCount;
    private LinkedHashMap<Integer, String> fourByteElement;

    private int eightByteElementCount;
    private LinkedHashMap<Integer, String> eightByteElement;

    public IOElement(int eventID, int elementCount,
                     int oneByteElementCount, LinkedHashMap<Integer, String> oneByteElement,
                     int twoByteElementCount, LinkedHashMap<Integer, String> twoByteElement,
                     int fourByteElementCount, LinkedHashMap<Integer, String> fourByteElement,
                     int eightByteElementCount, LinkedHashMap<Integer, String> eightByteElement) {
        this.eventID = eventID;
        this.elementCount = elementCount;
        this.oneByteElementCount = oneByteElementCount;
        this.oneByteElement = oneByteElement;
        this.twoByteElementCount = twoByteElementCount;
        this.twoByteElement = twoByteElement;
        this.fourByteElementCount = fourByteElementCount;
        this.fourByteElement = fourByteElement;
        this.eightByteElementCount = eightByteElementCount;
        this.eightByteElement = eightByteElement;
    }

    @Override
    public String toString() {
        return "\"eventID\":" + (Main.DEVICE_AVL_ID.containsKey(eventID) ? '"' + String.format("%-17s", Main.DEVICE_AVL_ID.get(eventID)) + '"' : String.format("\"%-17d\"", eventID)) +
                ",\"elementCount\":" + String.format("%2d", elementCount) +
                ",\"oneByteElementCount\":" + String.format("%2d", oneByteElementCount) +
                ',' + toStringWithFormatSize(oneByteElement, 3) +
                ",\"twoByteElementCount\":" + String.format("%2d", twoByteElementCount) +
                ',' + toStringWithFormatSize(twoByteElement, 5) +
                ",\"fourByteElementCount\":" + String.format("%2d", fourByteElementCount) +
                ',' + toStringWithFormatSize(fourByteElement, 10) +
                (eightByteElementCount !=0 ? ",\"eightByteElementCount\":" + String.format("%2d", eightByteElementCount) + ',' + toStringWithFormatSize(eightByteElement, 20) : "");
    }

    public String toStringWithFormatSize(LinkedHashMap<Integer, String> map, int size) {
        String mapAsString = map.keySet().stream()
                .map(key -> {
                    String str = "";

                    if (Main.DEVICE_AVL_ID.containsKey(key)) {
                        str += '"' + String.format("%-17s", Main.DEVICE_AVL_ID.get(key)) + '"';
                    } else {
                        str += String.format("\"%03d\"", key);
                    }

                    str += ':';

                    if (key == 78) {    // 78 Property ID is iButton ID for Teltonika FMx device
                        str += '"' + String.format("%" + size + "s", map.get(key)) + '"';
                    } else {
                        String avlIdDescriptionKey = key + "." + Main.DEVICE_AVL_ID.get(key) + "." + map.get(key);

                        if (Main.DEVICE_AVL_ID_DESCRIPTION.containsKey(avlIdDescriptionKey)) {
                            str += "\"" + String.format("%-20s", Main.DEVICE_AVL_ID_DESCRIPTION.get(avlIdDescriptionKey)) + "\"";
                        } else {
                            str += String.format("%" + size + "s", map.get(key));
                        }
                    }

                    return str;
                })
                .collect(Collectors.joining(","));

        return mapAsString;
    }

    public int getEventID() {
        return eventID;
    }

    public int getElementCount() {
        return elementCount;
    }

    public int getOneByteElementCount() {
        return oneByteElementCount;
    }

    public LinkedHashMap<Integer, String> getOneByteElement() {
        return oneByteElement;
    }

    public int getTwoByteElementCount() {
        return twoByteElementCount;
    }

    public LinkedHashMap<Integer, String> getTwoByteElement() {
        return twoByteElement;
    }

    public int getFourByteElementCount() {
        return fourByteElementCount;
    }

    public LinkedHashMap<Integer, String> getFourByteElement() {
        return fourByteElement;
    }

    public int getEightByteElementCount() {
        return eightByteElementCount;
    }

    public LinkedHashMap<Integer, String> getEightByteElement() {
        return eightByteElement;
    }

}
