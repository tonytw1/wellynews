package nz.co.searchwellington.queues;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class RabbitConnectionFactory {

	private final ConnectionFactory factory;
	
	@Autowired
	public RabbitConnectionFactory(
			@Value("${rabbit.hostname}") String rabbitHost,
			@Value("${rabbit.port}") Integer rabbitPort
	) {
		this.factory = new ConnectionFactory();
		factory.setHost(rabbitHost);
		factory.setPort(rabbitPort);
	}
	
	public Connection connect() throws IOException {
		return factory.newConnection();
	}
	
}
