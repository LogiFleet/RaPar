package parser;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

import static parser.Main.*;
import static util.Converter.StringToByteArray;

public class AvlData {

    // Common for Teltonika FM3001, FM36M1 (not Axis_*, not present for FM36M1), FMM130, FMC130
    private static final int I_BUTTON_PROPERTY_ID = 78;
    private static final int MODULE_ID_PROPERTY_ID = 101;
    private static final int SECURITY_STATE_FLAGS_PROPERTY_ID = 132;
    private static final int CONTROL_STATE_FLAGS_PROPERTY_ID = 123;
    private static final int DIGITAL_INPUT_2_PROPERTY_ID = 2;
    private static final int Axis_X_PROPERTY_ID = 17;
    private static final int Axis_Y_PROPERTY_ID = 18;
    private static final int Axis_Z_PROPERTY_ID = 19;

    // Other static values
    private static final int DELAY_BETWEEN_TWO_I_BUTTON_EVENT_MS = 20000;

    private static final String ZERO_8BYTES = "0000000000000000";
    private static final String ZERO_4BYTES = "00000000";
    private static final String ZERO_STRING = "0";

    // Static values from LF360 constraints
    private static final int MINUTES_UNTIL_IGNITION_ON_NO_SIGNAL = 15;

    private static final int CABU_SSF_B4B6_ENGINE_WORKING_AVL_ID = 132038;    // 1320-38:Engine_is_working -> 132 for Security_State_Flags * 1000 (margin), 38 for 38th bit -> SRC: https://wiki.teltonika-gps.com/view/FMB120_CAN_adapters#CAN_Adapter_State_Flags
    private static final byte CABU_SSF_B4B6_ENGINE_WORKING_VALUE_BITMASK = 0x40;
    private static final int CABU_SSF_B4B6_ENGINE_WORKING_BYTE_POS = 4;

    private static final int CABU_SSF_B6B7_CAN_MODULE_GOES_TO_SLEEP_MODE_AVL_ID = 132055;    // 1320-55:CAN_Module_Goes_To_Sleep_Mode
    private static final short CABU_SSF_B6B7_CAN_MODULE_GOES_TO_SLEEP_MODE_VALUE_BITMASK = 0x80;
    private static final int CABU_SSF_B6B7_CAN_MODULE_GOES_TO_SLEEP_MODE_BYTE_POS = 6;

    private final AvlDataPacket avlDataPacket;

    private final String raw;

    private String timeStamp;
    private String lastTimeStamp;
    private String timeStampDiff;
    private boolean timeStampDiffIsNegative;
    private String gatewayDate;
    private String gatewayDateMinusTimeStamp;
    private boolean gatewayDateMinusTimeStampIsNegative;
    private boolean gatewayDateMinusTimeStampGreaterThan15Min;  //todo refactoring, naming, greater or equal to 15 min, but it's too long
    private boolean digitalInput2StateHasChanged;   // Business Private switch
    private boolean iButtonSetResetInLessThanNSeconds;
    private boolean previousAvlDataOdometerGreaterThanCurrent;
    private String priority;
    private float longitude;
    private float latitude;
    private Short altitude;
    private Short angle;
    private Byte satellite;
    private Short speed;
    private int eventID;

    private IOElement ioElement;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private Instant timeStampInstant;

    public AvlData(AvlDataPacket avlDataPacket, String raw) {
        this.avlDataPacket = avlDataPacket;
        this.raw = raw;

        //TODO Separate data and process -> service
        parse();
    }

    @Override
    public String toString() {
        return (FLAG_TIME_STAMP ? ("\"gatewayDate\":\"" + gatewayDate + "\",") : ("")) +
                "\"timeStamp\":\"" + timeStamp + '\"' +
                ",\"lastTimeStamp\":\"" + (lastTimeStamp != null ? lastTimeStamp : '0') + '\"' +
                ",\"timeStampDiff\":\"" + timeStampDiff + '\"' +
                ",\"timeStampDiffIsNegative\":\"" + (timeStampDiffIsNegative ? '1' : '0') + '\"' +
                (FLAG_TIME_STAMP ? (",\"gatewayDateMinusTimeStamp\":\"" + gatewayDateMinusTimeStamp + "\"") : ("")) +
                (FLAG_TIME_STAMP ? (",\"gatewayDateMinusTimeStampIsNegative\":\"" + (gatewayDateMinusTimeStampIsNegative ? '1' : '0') + "\"") : ("")) +
                (FLAG_TIME_STAMP ? (",\"gatewayDateMinusTimeStampGreaterThan15Min\":\"" + (gatewayDateMinusTimeStampGreaterThan15Min ? '1' : '0') + "\"") : ("")) +
                ",\"digitalInput2StateHasChanged\":\"" + (IMEI_DIGITAL_INPUT_2_STATE_HAS_CHANGED.get(avlDataPacket.getImei()) != null ? digitalInput2StateHasChanged : "na") + '\"' +
                ",\"iButtonSetResetInLessThanNSeconds\":\"" + iButtonSetResetInLessThanNSeconds + '\"' +
                ",\"previousAvlDataOdometerGreaterThanCurrent\":\"" + previousAvlDataOdometerGreaterThanCurrent + '\"' +
                ",\"priority\":\"" + priority + '\"' +
                ",\"location\":{" + // JSON format (e.g. for Kibana import (geo map))
                "\"lat\":" + String.format("%.7f", latitude) +
                ",\"lon\":" + String.format("%.7f", longitude) +
                "}" +
                ",\"lat\":" + String.format("%.7f", latitude) +  // Google map xlsx import format
                ",\"lon\":" + String.format("%.7f", longitude) +
                ",\"altitude\":" + altitude +
                ",\"angle\":" + angle +
                ",\"satellite\":" + satellite +
                ",\"speed\":" + speed +
                "," + ioElement;

//        long duration = Duration.between(timeStampInstant, Instant.parse(avlDataPacket.getTimeStamp())).toMillis() / 1000;
//
//        return (gatewayDate + "," + timeStamp + "," + duration);
//        return (gatewayDate.substring(0,10) + "," + duration);
    }

    private void parse() {
        Instant gatewayDateInstant = null;
        Instant lastAvlDataTimestampInstant;
        Duration duration;

        if (FLAG_TIME_STAMP) {
            gatewayDateInstant = Instant.parse(avlDataPacket.getTimeStamp());
            gatewayDate = fmt.format(gatewayDateInstant.atZone(ZoneId.systemDefault()));
        }

        lastAvlDataTimestampInstant = IMEI_LAST_AVL_DATA_TIMESTAMP.get(avlDataPacket.getImei());
        lastTimeStamp = lastAvlDataTimestampInstant != null ? fmt.format(lastAvlDataTimestampInstant.atZone(ZoneId.systemDefault())) : null;

        timeStampInstant = Instant.ofEpochMilli(Long.parseLong(raw.substring(0, 16), 16));
        timeStamp = fmt.format(timeStampInstant.atZone(ZoneId.systemDefault()));

        IMEI_LAST_AVL_DATA_TIMESTAMP.put(avlDataPacket.getImei(), timeStampInstant);

        if (lastAvlDataTimestampInstant != null) {
            duration = Duration.between(lastAvlDataTimestampInstant, timeStampInstant);

            if (duration.isNegative()) {
                duration = Duration.between(timeStampInstant, lastAvlDataTimestampInstant);
                timeStampDiffIsNegative = true;
            } else {
                timeStampDiffIsNegative = false;
            }
            timeStampDiff = DurationFormatUtils.formatDurationHMS(duration.toMillis());
        } else {
            timeStampDiff = "0";
        }

        if (FLAG_TIME_STAMP) {
            duration = Duration.between(timeStampInstant, gatewayDateInstant);

            gatewayDateMinusTimeStampIsNegative = duration.isNegative();

            if (gatewayDateMinusTimeStampIsNegative) {
                duration = duration.abs();
            }

            try {
                gatewayDateMinusTimeStamp = DurationFormatUtils.formatDurationHMS(duration.toMillis());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            Duration fifteenMinutes = Duration.ofMinutes(MINUTES_UNTIL_IGNITION_ON_NO_SIGNAL);
            gatewayDateMinusTimeStampGreaterThan15Min = duration.compareTo(fifteenMinutes) >= 0;

//            if (gatewayDateMinusTimeStampGreaterThan15Min) {
//                try {
//                    SAMPLE_FILE_TXT_WRITER.write(avlDataPacket.getRaw() + "\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }

        priority = raw.substring(16, 18);

        longitude = (float) ((int) Long.parseLong(raw.substring(18, 26), 16)) / 10000000;
        latitude = (float) ((int) Long.parseLong(raw.substring(26, 34), 16)) / 10000000;

        // fix negative altitude eg. "alt=-045"
        try {
            altitude = (short) (Integer.parseInt(raw.substring(34, 38), 16));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        angle = Short.parseShort(raw.substring(38, 42), 16);
        satellite = Byte.parseByte(raw.substring(42, 44), 16);
        speed = Short.parseShort(raw.substring(44, 48), 16);

        eventID = Integer.parseInt(raw.substring(48, 50), 16);
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

            int engineIsWorkingSubKey = 0;
            String engineIsWorkingSubValue = "";
            int canModuleGoesToSleepModeSubKey = 0;
            String canModuleGoesToSleepModeSubValue = "";

            // Some PROPERTY_ID values stay in Hex format (no sense to convert them in Dec format)
            if (key == I_BUTTON_PROPERTY_ID || key == MODULE_ID_PROPERTY_ID || key == SECURITY_STATE_FLAGS_PROPERTY_ID) {  // 8 bytes
                String hexID = str.substring(0, size);
                value = ZERO_8BYTES.compareTo(hexID) == 0 ? ZERO_STRING : hexID.toUpperCase();

                if (eventID == I_BUTTON_PROPERTY_ID && key == I_BUTTON_PROPERTY_ID && ZERO_8BYTES.compareTo(hexID) != 0) {
                    IMEI_I_BUTTON_VALUE_HAS_CHANGED.put(avlDataPacket.getImei(), timeStampInstant);
                }

                if (eventID == I_BUTTON_PROPERTY_ID &&
                        key == I_BUTTON_PROPERTY_ID &&
                        ZERO_8BYTES.compareTo(hexID) == 0 &&
                        IMEI_I_BUTTON_VALUE_HAS_CHANGED.get(avlDataPacket.getImei()) != null &&
                        Duration.between(IMEI_I_BUTTON_VALUE_HAS_CHANGED.get(avlDataPacket.getImei()), timeStampInstant).toMillis() <= DELAY_BETWEEN_TWO_I_BUTTON_EVENT_MS) {

                    //todo check that avlDataPacket output only once even this behavior pattern happen multiple times in same packet
                    //todo output in dedicated file
//                     System.out.println(avlDataPacket.getRaw());
                    iButtonSetResetInLessThanNSeconds = true;
                } else {
                    iButtonSetResetInLessThanNSeconds = false;
                }

                // Additional parsing of "Engine is working" value
                if (key == SECURITY_STATE_FLAGS_PROPERTY_ID && value.compareTo(ZERO_STRING) != 0) {
                    byte[] securityStateFlags = StringToByteArray(value.toCharArray());

                    ArrayUtils.reverse(securityStateFlags); // Reverse to be aligned with byte ref number from Teltonika documentation, when converted from Hex [..., B4, B3, B2, ...] but to be aligned with byte array indices -> reverse, then -> [B0, B1, B2, B3, B4, ...]

                    int engineIsWorking = (securityStateFlags[CABU_SSF_B4B6_ENGINE_WORKING_BYTE_POS] & CABU_SSF_B4B6_ENGINE_WORKING_VALUE_BITMASK) == 0 ? 0 : 1;
                    engineIsWorkingSubKey = CABU_SSF_B4B6_ENGINE_WORKING_AVL_ID;
                    engineIsWorkingSubValue = String.valueOf(engineIsWorking);

                    int canModuleGoesToSleepMode = (securityStateFlags[CABU_SSF_B6B7_CAN_MODULE_GOES_TO_SLEEP_MODE_BYTE_POS] & CABU_SSF_B6B7_CAN_MODULE_GOES_TO_SLEEP_MODE_VALUE_BITMASK) == 0 ? 0 : 1;
                    canModuleGoesToSleepModeSubKey = CABU_SSF_B6B7_CAN_MODULE_GOES_TO_SLEEP_MODE_AVL_ID;
                    canModuleGoesToSleepModeSubValue = String.valueOf(canModuleGoesToSleepMode);
                }
            } else if (key == CONTROL_STATE_FLAGS_PROPERTY_ID) {    // 4 bytes
                String hexID = str.substring(0, size);
                value = ZERO_4BYTES.compareTo(hexID) == 0 ? ZERO_STRING : hexID.toUpperCase();
            } else if (key == Axis_X_PROPERTY_ID || key == Axis_Y_PROPERTY_ID || key == Axis_Z_PROPERTY_ID) {    // Value is a signed short
                String hexValue = str.substring(0, size);
                short s = (short) Integer.parseInt(hexValue, 16);
                value = "" + s;
            } else if (key == DIGITAL_INPUT_2_PROPERTY_ID) {
                value = String.valueOf(Long.parseUnsignedLong(str.substring(0, size), 16));

                Boolean currentDigitalInput2State = value.compareTo("1") == 0 ? Boolean.TRUE : Boolean.FALSE;

                if (IMEI_DIGITAL_INPUT_2_STATE_HAS_CHANGED.get(avlDataPacket.getImei()) != null) {
                    Boolean lastDigitalInput2State = IMEI_DIGITAL_INPUT_2_STATE_HAS_CHANGED.get(avlDataPacket.getImei());
                    if (currentDigitalInput2State != lastDigitalInput2State) {
                        digitalInput2StateHasChanged = true;

                        if (!avlDataPacket.isAtLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent() && eventID != DIGITAL_INPUT_2_PROPERTY_ID) {
                            avlDataPacket.setAtLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent(true);

                            // to avoid getting twice same output message
                            if (!avlDataPacket.isAtLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange()) {
                                //todo output in dedicated file
//                                 System.out.println(avlDataPacket.getRaw());
                            }
                        }
                    } else {
                        digitalInput2StateHasChanged = false;
                    }
                } else {
                    digitalInput2StateHasChanged = false;
                }

                if (eventID == DIGITAL_INPUT_2_PROPERTY_ID && !digitalInput2StateHasChanged) {
                    if (!avlDataPacket.isAtLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange()) {
                        avlDataPacket.setAtLeastOneAvlDataInAvlDataPacketContainDI2TriggeredEventWithoutDI2PropertyStateChange(true);

                        // to avoid getting twice same output message
                        if (!avlDataPacket.isAtLeastOneAvlDataInAvlDataPacketContainDI2PropertyStateChangeWithoutDI2TriggeredEvent()) {
                            //todo output in dedicated file
//                             System.out.println(avlDataPacket.getRaw());
                        }
                    }
                }

                IMEI_DIGITAL_INPUT_2_STATE_HAS_CHANGED.put(avlDataPacket.getImei(), currentDigitalInput2State);

            } else if (key == ODOMETER_PROPERTY_ID) {
                value = String.valueOf(Long.parseUnsignedLong(str.substring(0, size), 16));
                long odometer = Long.parseLong(value);

                if (ODOMETER.get(avlDataPacket.getImei()) != null && ODOMETER.get(avlDataPacket.getImei()) > odometer) {
                    previousAvlDataOdometerGreaterThanCurrent = true;

                    //todo check that avlDataPacket output only once even this issue (or other already outputted) happen multiple times in same packet
                    //todo output in dedicated file
//                     System.out.println(avlDataPacket.getRaw());
                }

                ODOMETER.put(avlDataPacket.getImei(), odometer);

            } else {
                value = String.valueOf(Long.parseUnsignedLong(str.substring(0, size), 16));
            }

            str = str.substring(size);

            xByteElement.put(key, value);

            if (engineIsWorkingSubKey != 0) {
                xByteElement.put(engineIsWorkingSubKey, engineIsWorkingSubValue);
            }

            if (canModuleGoesToSleepModeSubKey != 0) {
                xByteElement.put(canModuleGoesToSleepModeSubKey, canModuleGoesToSleepModeSubValue);
            }
        }

        return xByteElement;
    }

    public String getRaw() {
        return raw;
    }

    public String getPriority() {
        return priority;
    }

    public Short getAngle() {
        return angle;
    }

    public Short getSpeed() {
        return speed;
    }

    public IOElement getIoElement() {
        return ioElement;
    }

    public DateTimeFormatter getFmt() {
        return fmt;
    }

}
