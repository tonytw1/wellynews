package nz.co.searchwellington.queues;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class LinkCheckerQueue {

    private final static Logger log = Logger.getLogger(LinkCheckerQueue.class);

    public static final String QUEUE_NAME = "wellynewslinkchecker";

    private final Channel channel;

    @Autowired
    public LinkCheckerQueue(RabbitConnectionFactory rabbitConnectionFactory) throws IOException, TimeoutException {
        channel = rabbitConnectionFactory.connect().createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    }

    public void add(String id) {
        log.info("Adding resource id to queue: " + id);
        try {
            channel.basicPublish("", QUEUE_NAME, null, id.getBytes());
            channel.basicPublish("", "meh", null, id.getBytes());   // TODO testing
        } catch (Exception e) {
            log.error(e);
        }
    }

}
