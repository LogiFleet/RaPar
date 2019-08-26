
import java.util.LinkedHashMap;

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

    public IOElement(int eventID, int elementCount, int oneByteElementCount, LinkedHashMap<Integer, String> oneByteElement, int twoByteElementCount, LinkedHashMap<Integer, String> twoByteElement, int fourByteElementCount, LinkedHashMap<Integer, String> fourByteElement, int eightByteElementCount, LinkedHashMap<Integer, String> eightByteElement) {
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
                ", 1B elmnt=" + oneByteElement +
                ", 2B cnt=" + twoByteElementCount +
                ", 2B elmnt=" + twoByteElement +
                ", 4B cnt=" + fourByteElementCount +
                ", 4B elmnt=" + fourByteElement +
                ", 8B cnt=" + eightByteElementCount +
                ", 8B elmnt=" + eightByteElement +
                '}';
    }

//    @Override
//    public String toString() {
//        return "IOElement{" +
//                "eventID=" + String.format("%03d", eventID) +
//                ", elementCount=" + elementCount +
//                ", oneByteElementCount=" + oneByteElementCount +
//                ", oneByteElement=" + oneByteElement +
//                ", twoByteElementCount=" + twoByteElementCount +
//                ", twoByteElement=" + twoByteElement +
//                ", fourByteElementCount=" + fourByteElementCount +
//                ", fourByteElement=" + fourByteElement +
//                ", eightByteElementCount=" + eightByteElementCount +
//                ", eightByteElement=" + eightByteElement +
//                '}';
//    }

}
