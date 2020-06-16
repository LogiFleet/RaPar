package parser;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IOElement {

    private static Function<String, String> dQS = s -> "\"" + s + "\"";    // Surround given String with double quote
    private static Function<Integer, String> dQI = i -> "\"" + i + "\"";    // Surround given Integer with double quote

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
        return "\"eventID\":" + (Main.DEVICE_AVL_ID.containsKey(eventID) ? dQS.apply(Main.DEVICE_AVL_ID.get(eventID)) : dQI.apply(eventID)) +
                ",\"elementCount\":" + elementCount +
                ",\"1bElementCount\":" + oneByteElementCount + ',' + toStringWithFormatSize(oneByteElement) +
                ",\"2bElementCount\":" + twoByteElementCount + ',' + toStringWithFormatSize(twoByteElement) +
                (fourByteElementCount !=0 ? ",\"4bElementCount\":" + fourByteElementCount + ',' + toStringWithFormatSize(fourByteElement) : "") +
                (eightByteElementCount !=0 ? ",\"8bElementCount-+\":" + eightByteElementCount + ',' + toStringWithFormatSize(eightByteElement) : "");   // -+ means count without additional parsed values from Flags (Control, Security, ...)
    }

    public String toStringWithFormatSize(LinkedHashMap<Integer, String> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> {
                    String str = "";

                    if (Main.DEVICE_AVL_ID.containsKey(key)) {
                        str += dQS.apply(Main.DEVICE_AVL_ID.get(key));
                    } else {
                        str += key;
                    }

                    str += ':';

                    String avlIdDescriptionKey = key + "." + Main.DEVICE_AVL_ID.get(key) + "." + map.get(key);

                    if (Main.DEVICE_AVL_ID_DESCRIPTION.containsKey(avlIdDescriptionKey)) {
                        str += dQS.apply(Main.DEVICE_AVL_ID_DESCRIPTION.get(avlIdDescriptionKey));
                    } else {
                        str += dQS.apply(map.get(key));
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
