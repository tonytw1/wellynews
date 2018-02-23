package nz.co.searchwellington.queues;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class RabbitConnectionFactory {

	//private final ConnectionFactory factory;
	
	@Autowired
	public RabbitConnectionFactory(@Value("#{config['rabbit.hostname']}") String rabbitHost) {
		//this.factory = new ConnectionFactory();
		//factory.setHost(rabbitHost);
	}
	
	public Connection connect() throws IOException {
	//	return factory.newConnection();
		return null;
	}
	
}
