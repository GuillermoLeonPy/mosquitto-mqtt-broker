package py.com.middleware.application.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.integration.mqtt.support.MqttHeaders;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {
	void senToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
}
