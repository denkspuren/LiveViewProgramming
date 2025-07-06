package lvp.sinks;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import lvp.Processor.MetaInformation;
import lvp.skills.TriConsumer;

public interface Sink {
    void clear(String sourceId);
    void error(MetaInformation meta, String message);
    Map<String, BiFunction<MetaInformation, String, String>> registerTransformer();
    Map<String, BiConsumer<MetaInformation, String>> registerChannel();
    Map<String, TriConsumer<MetaInformation, Process, String>> registerScan();
}
