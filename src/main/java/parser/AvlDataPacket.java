package parser;

import util.Converter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class AvlDataPacket {

    private String raw;

    private String imei;
    private String preamble;
    private String avlDataLength;
    private String codecID;
    private String avlDataCountBegin;

    private String avlDataAggregatedPlusBalance;    // = avlDataAggregated + (avlDataCountEnd + crc) at the end

    private List<AvlData> avlDataList;

    private String avlDataCountEnd;
    private String crc;

    public AvlDataPacket(String raw, String imei, String preamble, String avlDataLength, String codecID, String avlDataCount, String avlDataAggregated) {
        this.raw = raw;
        this.imei = imei;
        this.preamble = preamble;
        this.avlDataLength = avlDataLength;
        this.codecID = codecID;
        this.avlDataCountBegin = avlDataCount;
        this.avlDataAggregatedPlusBalance = avlDataAggregated;

        this.avlDataList = new ArrayList<>();

        //TODO Separate data and process -> service
        if (codecID.compareTo("08") == 0) splitAvlDataAggregated(); // Process only data frame Codec 08 encoded
        // TODO !NOT CLEAN! improve with Codec 0c (12) GPRS command decoding (device acknowledge)
        else if (codecID.compareTo("0c") == 0) this.raw = Converter.hexToAscii(avlDataAggregated);    // Convert Codec 0c (12) GPRS command (device acknowledge) into ASCII String
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

            avlDataList.add(new AvlData(rawAvlData));
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

                writer.write(
                        '{' +

                            "\"line\":" + (fileLineTreated + i) +
                            ",\"rawLine\":" + fileLineNumber +

                            ",\"manufacturer\":\"" + Main.MANUFACTURER + '\"' +
                            ",\"device\":\"" + Main.DEVICE + '\"' +

                            ",\"imei\":\"" + imei + "\",\"messageTotal\":" + size + ",\"messageIndex\":" + (i + 1) + ',' + avlDataList.get(i) +

                            ",\"length\":\"" + avlDataLength + '\"' +
                            ",\"codec\":\"" + codecID + '\"' +
                            ",\"cntBegin\":" + Integer.parseInt(avlDataCountBegin, 16) +
                            ",\"cntEnd\":" + Integer.parseInt(avlDataCountEnd,16) +
                            ",\"crc\":\"" + crc + '\"' +

                                (tltBean != null ?
                                        (",\"sn\":\"" + (tltBean.getSn() != "" ? tltBean.getSn() : "na") + '\"') +
                                        (",\"model\":\"" + (tltBean.getModel() != "" ? tltBean.getModel() : "na") + '\"') +
                                        (",\"firmware\":\"" + (tltBean.getFirmware() != "" ? tltBean.getFirmware() : "na") + '\"') +
                                        (",\"configuration\":\"" + (tltBean.getConfiguration() != "" ? tltBean.getConfiguration() : "na") + '\"') +
                                        (",\"description\":\"" + (tltBean.getDescription() != "" ? tltBean.getDescription() : "na") + '\"') +
                                        (",\"companyName\":\"" + (tltBean.getCompanyName() != "" ? tltBean.getCompanyName() : "na") + '\"') +
                                        (",\"group\":\"" + (tltBean.getGroup() != "" ? tltBean.getGroup() : "na") + '\"') +
                                        (",\"lastLogin\":\"" + (tltBean.getLastLogin() != "" ? tltBean.getLastLogin() : "na") + '\"')
                                        : "") +

                            ",\"raw\":\"" + raw + "\"" +

                        '}' +

                        "\r\n");
            }
//            writer.write("\r\n");
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

}
