
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static  String INPUT_FILE_NAME = "sampleLog.txt";

    public static void main(String[] args) {
        List<AvlDataPacket> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(Main.class.getClassLoader().getResource(INPUT_FILE_NAME).toURI()))) {

            list = stream.map(str -> new AvlDataPacket(str,
                    str.substring(0, 15),
                    str.substring(16, 24),
                    str.substring(24, 32),
                    str.substring(32, 34),
                    str.substring(34, 36),
                    str.substring(36))).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // debug mode
//        list.forEach(System.out::println);

        // count mode
//        list.forEach(AvlDataPacket::soutAvlDataCount);

        // std mode
        list.forEach(AvlDataPacket::soutStd);
    }

}
