package nz.co.searchwellington.queues;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class LinkCheckerQueue {

    private final static Logger log = Logger.getLogger(LinkCheckerQueue.class);

    public static final String QUEUE_NAME = "linkchecker";

    private final Channel channel;

    @Autowired
    public LinkCheckerQueue(RabbitConnectionFactory rabbitConnectionFactory) throws IOException {
        channel = rabbitConnectionFactory.connect().createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    }

    public void add(String id) {
        log.info("Adding resource id to queue: " + id);
        try {
            channel.basicPublish("", QUEUE_NAME, null, id.getBytes());
            final int messageCount = channel.queueDeclarePassive(QUEUE_NAME).getMessageCount();
            log.info("There are currently " + messageCount + " items in the link checker queue");    // TODO doesn't seem to work

        } catch (IOException e) {
            log.error(e);
        }
    }

}