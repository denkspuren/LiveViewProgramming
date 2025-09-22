package lvp.sinks.server_sink;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import lvp.Processor.MetaInformation;
import lvp.services.Interaction;
import lvp.sinks.Sink;
import lvp.skills.TriConsumer;

public class ServerSink implements Sink {

    Server server;
    HttpChannel channel;

    public ServerSink(int port) throws IOException {
        server = new Server(Math.abs(port));
        channel = HttpChannel.of(server);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    
    @Override
    public Map<String, BiFunction<MetaInformation, String, String>> registerTransformer() {
        return Map.of(
            "Button", Interaction::button,
            "Input", Interaction::input,
            "Checkbox", Interaction::checkbox);
    }
    @Override
    public Map<String, BiConsumer<MetaInformation, String>> registerChannel() {
        return Map.of(
            "Markdown", channel::consumeMarkdown, 
            "Dot", channel::consumeDot,
            "Html", channel::consumeHTML, 
            "JavaScript", channel::consumeJS, 
            "JavaScriptCall", channel::consumeJSCall,
            "Css", channel::consumeCss,
            "SubViewStyle", channel::consumeSubViewStyle,
            "Clear", channel::consumeClear);
    }

    @Override
    public void clear(String sourceId) {
        server.events.removeIf(event -> event.sourceId().equals(sourceId));
        if (server.waitingProcesses.containsKey(sourceId)) {
            server.waitingProcesses.get(sourceId).destroyForcibly();
            server.waitingProcesses.remove(sourceId);
        }
    }

    @Override
    public void error(MetaInformation meta, String message) {
        channel.consumeError(meta, message);
    }


    @Override
    public Map<String, TriConsumer<MetaInformation, Process, String>> registerScan() {
        return Map.of(
            "InputScan", channel::consumeInputScan
        );
    }
    
}
