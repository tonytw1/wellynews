package nz.co.searchwellington.linkchecking;

import java.io.IOException;

import nz.co.searchwellington.queues.LinkCheckerQueue;
import nz.co.searchwellington.queues.RabbitConnectionFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

@Component
public class LinkCheckerListener {

    private final static Logger log = Logger.getLogger(LinkCheckerListener.class);

    private final static String QUEUE_NAME = LinkCheckerQueue.QUEUE_NAME;

    private final LinkChecker linkChecker;
    private final Channel channel;
    private final Thread consumerThread;

    @Autowired
    public LinkCheckerListener(LinkChecker linkChecker, RabbitConnectionFactory rabbitConnectionFactory) throws IOException {
        this.linkChecker = linkChecker;

        final Connection connection = rabbitConnectionFactory.connect();
        channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        consumerThread = new Thread(new ConsumerThread());
        consumerThread.start();
    }

    class ConsumerThread implements Runnable {
        public void run() {

            QueueingConsumer consumer = new QueueingConsumer(channel);
            try {
                channel.basicConsume(QUEUE_NAME, true, consumer);
            } catch (IOException e) {
                log.error(e);
            }

            while (true) {
                try {
                    final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String message = new String(delivery.getBody());
                    log.info("Received: " + message);
                    linkChecker.scanResource(message);

                } catch (Exception e) {
                    log.error(e);
                }
            }

        }
    }

}
