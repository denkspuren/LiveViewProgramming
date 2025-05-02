package lvp.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileDestination implements LogDestination {
    private final BufferedWriter writer;
    private final Lock lock = new ReentrantLock();

    public static FileDestination of(String path) throws IOException { return new FileDestination(path); }
    private FileDestination(String path) throws IOException{
        try {
            this.writer = new BufferedWriter(new FileWriter(path, true));
        } catch (IOException e) {
            System.err.println("Failed to open file: " + path);
            throw e;
        }
    }

    @Override
    public void log(String formattedMessage) {
        lock.lock();
        try {
            writer.write(formattedMessage);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Logging to file failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}