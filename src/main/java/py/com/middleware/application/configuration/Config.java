package py.com.middleware.application.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import py.com.middleware.application.gateway.MqttGateway;

@Configuration
public class Config {

	@Autowired
	private MqttGateway mqttGateway;
	
	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(new String[] {"ws://localhost:9001" });
		options.setUserName("user_00");
		options.setPassword("123456".toCharArray());
		options.setCleanSession(true);
		factory.setConnectionOptions(options);
		return factory;
	}
	
	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}
	
	@Bean
	public MessageProducer inbound() {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn",
				mqttClientFactory(), "testtopic/destination/middleware/#");
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(2);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}
	
	@Bean
	@ServiceActivator (inputChannel = "mqttInputChannel")
	public MessageHandler handler() {
		return new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				// TODO Auto-generated method stub
				String inBoundTopic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
				String outBoundTopic = preareOutBoundTopic(inBoundTopic);
				System.out.println("\nReceived message \nTopic: " + inBoundTopic + "\nmessage: " + message.getPayload());				
				mqttGateway.senToMqtt("Response to message: " + message.getPayload().toString(), outBoundTopic);
			}
		};
	}
	
	private String preareOutBoundTopic(String inboundTopic) {		
		return inboundTopic.replace("middleware", "client");
	}
	
	@Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }
	
	@Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        //clientId is generated using a random number
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("testtopic/destination/client/#");
        messageHandler.setDefaultRetained(false);
        return messageHandler;
    }
}
