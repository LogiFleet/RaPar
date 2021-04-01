package parser;

import util.Converter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static parser.Main.FLAG_RAW_DATA;

public class AvlDataPacket {

    private String raw;

    private String imei;
    private String timeStamp;
    private String preamble;
    private String avlDataLength;
    private String codecID;
    private String avlDataCountBegin;

    private String avlDataAggregatedPlusBalance;    // = avlDataAggregated + (avlDataCountEnd + crc) at the end

    private List<AvlData> avlDataList;

    private String avlDataCountEnd;
    private String crc;

    private boolean atLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent;
    private boolean atLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange;

    public AvlDataPacket(String raw, String imei, String preamble, String avlDataLength, String codecID, String avlDataCount, String avlDataAggregated) {
        this.raw = raw;
        this.imei = imei;
        this.preamble = preamble;
        this.avlDataLength = avlDataLength;
        this.codecID = codecID;
        this.avlDataCountBegin = avlDataCount;
        this.avlDataAggregatedPlusBalance = avlDataAggregated;

        this.avlDataList = new ArrayList<>();
    }

    public AvlDataPacket(String raw, String imei, String timeStamp, String preamble, String avlDataLength, String codecID, String avlDataCount, String avlDataAggregated) {
        this(raw, imei, preamble, avlDataLength, codecID, avlDataCount, avlDataAggregated);

        this.timeStamp = timeStamp;
    }

    public void process() {
        //TODO Separate data and process -> service
        if (codecID.compareTo("08") == 0) splitAvlDataAggregated(); // Process only data frame Codec 08 encoded
            // TODO !NOT CLEAN! improve with Codec 0c (12) GPRS command decoding (device acknowledge)
        else if (codecID.compareTo("0c") == 0) this.raw = Converter.hexToAscii(avlDataAggregatedPlusBalance);    // Convert Codec 0c (12) GPRS command (device acknowledge) into ASCII String
    }

    @Override
    public String toString() {
        return "packet{" +
                "imei='" + imei + '\'' +
                ", length='" + avlDataLength + '\'' +
                ", codec='" + codecID + '\'' +
                ", cnt begin='" + avlDataCountBegin + '\'' +
                ", list=" + avlDataList +
                ", cnt end='" + avlDataCountEnd + '\'' +
                ", crc='" + crc + '\'' +
                ", raw='" + raw + '\'' +
                '}';
    }

//    @Override
//    public String toString() {
//        return "parser.AvlDataPacket{" +
//                "imei='" + imei + '\'' +
//                ", preamble='" + preamble + '\'' +
//                ", avlDataLength='" + avlDataLength + '\'' +
//                ", codecID='" + codecID + '\'' +
//                ", avlDataCountBegin='" + avlDataCountBegin + '\'' +
////                ", avlDataAggregatedPlusBalance='" + avlDataAggregatedPlusBalance + '\'' +
//                ", avlDataList=" + avlDataList +y§
//                ", avlDataCountEnd='" + avlDataCountEnd + '\'' +
//                ", crc='" + crc + '\'' +
//                ", raw='" + raw + '\'' +
//                '}';
//    }

    public void soutAvlDataCount() {
        int avlDataCount = Integer.parseInt(avlDataCountBegin);

        for (int i = 1; i <= avlDataCount; i++) {
            System.out.println(avlDataCount + " " + i + " of " + avlDataCount);
        }

        System.out.println();
    }

    private void splitAvlDataAggregated() {
        String rawAvlData;
        String str = avlDataAggregatedPlusBalance;

        int avlDataCount = Integer.parseUnsignedInt(avlDataCountBegin, 16);

        for (int i = 0; i < avlDataCount; i++) {
            int elementCount = 0;
            int elementCountRetain = 0;

            rawAvlData = str.substring(0, 52);
            str = str.substring(52);

            for (int j = 1; j <= 4; j++) {
                elementCount += Integer.parseInt(str.substring(0, 2), 16);
                rawAvlData += str.substring(0, 2);
                str = str.substring(2);

                for (int k = elementCountRetain; k < elementCount; k++) {
                    rawAvlData += str.substring(0, 2);
                    str = str.substring(2); // eat ID

                    // eat value
                    int nChar = (int) Math.pow(2, j);

                    try {
                        String.format("%0" + nChar + "d", Long.parseUnsignedLong(str.substring(0, nChar), 16));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    rawAvlData += str.substring(0, nChar);
                    str = str.substring(nChar);
                }

                elementCountRetain = elementCount;
            }

            avlDataList.add(new AvlData(this, rawAvlData));
        }

        avlDataCountEnd = str.substring(0, 2);
        crc = str.substring(2);
    }

    public int soutStd(Writer writer, int fileLineNumber, int fileLineTreated) {
        int size = 0;

        String header = (codecID.compareTo("08") != 0 ? ">>>>!> " : "") + "imei='" + imei + '\'' + // NO Codec 08 line starts with special mark
                ", length='" + avlDataLength + '\'' +
                ", codec='" + codecID + '\'' +
                ", cnt begin='" + avlDataCountBegin + '\'' +
                ", cnt end='" + avlDataCountEnd + '\'' +
                ", crc='" + crc + '\'' +
                ", raw='" + raw + '\'';

        //todo NO 08 codec special treatment handling (c.f. above for header string construction)

//        if (codecID.compareTo("08") != 0) {
//            System.out.println("codec not equal to 08");
//        }

        try {
//            writer.write((fileLineNumber + " / " + fileLineTreated) + ": " + header + "\r\n");
            size = avlDataList.size();
            for (int i = 0; i < size; i++) {
                TeltonikaFotaWebDeviceInfoBean tltBean = Main.TELONIKA_FOTA_WEB_DEVICE_INFO_LIST.stream()
                        .filter(device -> imei.equals(device.getImei()))
                        .findFirst()
                        .orElse(null);

                String rawWithoutLogHeader = "";

                if (FLAG_RAW_DATA) {
                    rawWithoutLogHeader = raw.substring(raw.indexOf(";") + 1); // Remove imei
                    rawWithoutLogHeader = rawWithoutLogHeader.substring(rawWithoutLogHeader.indexOf(";") + 1);    // Remove timestamp
                }

                writer.write(
                        '{' +

                            "\"line\":" + (fileLineTreated + i) +
                            ",\"rawLine\":" + fileLineNumber +

                            ",\"manufacturer\":\"" + Main.MANUFACTURER + '\"' +
                            ",\"device\":\"" + Main.DEVICE + '\"' +

                            ",\"imei1\":\"" + imei + "\"," + (Main.IMEI_NAME.containsKey(imei) ? "\"imeiName\":\"" + Main.IMEI_NAME.get(imei) + "\"," : "") + "\"messageTotal\":" + size + ",\"messageIndex\":" + (i + 1) + ',' + avlDataList.get(i) +

                            ",\"length\":\"" + avlDataLength + '\"' +
                            ",\"codec\":\"" + codecID + '\"' +
                            ",\"cntBegin\":" + Integer.parseInt(avlDataCountBegin, 16) +
                            ",\"cntEnd\":" + Integer.parseInt(avlDataCountEnd,16) +
                            ",\"crc\":\"" + crc + '\"' +

                                (tltBean != null ?

                                        (",\"imei2\":\"" + (tltBean.getImei() != "" ? tltBean.getImei() : "na") + '\"') +
                                        (",\"description\":\"" + (tltBean.getDescription() != "" ? tltBean.getDescription() : "na") + '\"') +
                                        (",\"model\":\"" + (tltBean.getModel() != "" ? tltBean.getModel() : "na") + '\"') +
                                        (",\"currentFirmware\":\"" + (tltBean.getCurrentFirmware() != "" ? tltBean.getCurrentFirmware() : "na") + '\"') +
                                        (",\"currentConfiguration\":\"" + (tltBean.getCurrentConfiguration() != "" ? tltBean.getCurrentConfiguration() : "na") + '\"') +
                                        (",\"serial\":\"" + (tltBean.getSerial() != "" ? tltBean.getSerial() : "na") + '\"') +
                                        (",\"companyName\":\"" + (tltBean.getCompanyName() != "" ? tltBean.getCompanyName() : "na") + '\"') +
                                        (",\"groupName\":\"" + (tltBean.getGroupName() != "" ? tltBean.getGroupName() : "na") + '\"') +
                                        (",\"seenAt\":\"" + (tltBean.getSeenAt() != "" ? tltBean.getSeenAt() : "na") + '\"') +
                                        (",\"createdAt\":\"" + (tltBean.getCreatedAt() != "" ? tltBean.getCreatedAt() : "na") + '\"') +
                                        (",\"updatedAt\":\"" + (tltBean.getUpdatedAt() != "" ? tltBean.getUpdatedAt() : "na") + '\"')

                                        : "") +

                                (FLAG_RAW_DATA ? (",\"rawData\":\"" + rawWithoutLogHeader + "\"") : ("")) +

                        '}' +

                        "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return --size;
    }

    public String getRaw() {
        return raw;
    }

    public String getImei() {
        return imei;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getPreamble() {
        return preamble;
    }

    public String getAvlDataLength() {
        return avlDataLength;
    }

    public String getCodecID() {
        return codecID;
    }

    public String getAvlDataCountBegin() {
        return avlDataCountBegin;
    }

    public String getAvlDataAggregatedPlusBalance() {
        return avlDataAggregatedPlusBalance;
    }

    public List<AvlData> getAvlDataList() {
        return avlDataList;
    }

    public String getAvlDataCountEnd() {
        return avlDataCountEnd;
    }

    public String getCrc() {
        return crc;
    }

    public boolean isAtLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent() {
        return atLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent;
    }

    public void setAtLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent(boolean atLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent) {
        this.atLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent = atLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent;
    }

    public boolean isAtLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange() {
        return atLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange;
    }

    public void setAtLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange(boolean atLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange) {
        this.atLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange = atLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange;
    }
}
