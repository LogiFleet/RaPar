
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
        return "elmnt{" +
                "event=" + String.format("%03d", eventID) +
                ", elmnt cnt=" + elementCount +
                ", 1B cnt=" + oneByteElementCount +
                ", 1B elmnt=" + toStringWithKeyOnThreeDigits(oneByteElement) +
                ", 2B cnt=" + twoByteElementCount +
                ", 2B elmnt=" + toStringWithKeyOnThreeDigits(twoByteElement) +
                ", 4B cnt=" + fourByteElementCount +
                ", 4B elmnt=" + toStringWithKeyOnThreeDigits(fourByteElement) +
                ", 8B cnt=" + eightByteElementCount +
                ", 8B elmnt=" + toStringWithKeyOnThreeDigits(eightByteElement) +
                '}';
    }

    public String toStringWithKeyOnThreeDigits(LinkedHashMap<Integer, String> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> String.format("%03d", key) + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
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
