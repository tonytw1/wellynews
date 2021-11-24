package nz.co.searchwellington.queues;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class LinkCheckerQueue {
    // Try to hide our current use of rabbitmq from the rest of the application
    private final static Logger log = Logger.getLogger(LinkCheckerQueue.class);

    public static final String QUEUE_NAME = "wellynewslinkchecker";

    private final Channel channel;
    private final Counter queuedCounter;

    @Autowired
    public LinkCheckerQueue(RabbitConnectionFactory rabbitConnectionFactory, MeterRegistry registry) throws IOException, TimeoutException {
        this.queuedCounter = registry.counter("linkchecker_queued");
        channel = rabbitConnectionFactory.connect().createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    }

    public void add(String id) {
        try {
            log.debug("Adding resource id to queue: " + id);
            channel.basicPublish("", QUEUE_NAME, null, id.getBytes());
            queuedCounter.increment();
        } catch (Exception e) {
            log.error("Failed to add to link checker queue", e);
        }
    }

}
