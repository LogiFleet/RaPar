package util;

public class Converter {

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static byte[] StringToByteArray(char[] hex) {
        if (hex.length % 2 == 1) {
            System.out.println("The binary key cannot have an odd number of digits");
        }

        byte[] arr = new byte[hex.length >> 1];

        for (int i = 0; i < hex.length >> 1; ++i)
        {
            arr[i] = (byte)((GetHexVal(hex[i << 1]) << 4) + (GetHexVal(hex[(i << 1) + 1])));
        }

        return arr;
    }

    public static int GetHexVal(char hex) {
        int val = hex;
        //For uppercase A-F letters:
        //return val - (val < 58 ? 48 : 55);
        //For lowercase a-f letters:
        //return val - (val < 58 ? 48 : 87);
        //Or the two combined, but a bit slower:
        return val - (val < 58 ? 48 : (val < 97 ? 55 : 87));
    }

}
