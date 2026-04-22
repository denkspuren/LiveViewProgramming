package lvp.skills;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class Scan {
    private Scan() {}
    public static void sendToSource(Process source, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(source.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(content + "\n");
        writer.flush();
    }
}
