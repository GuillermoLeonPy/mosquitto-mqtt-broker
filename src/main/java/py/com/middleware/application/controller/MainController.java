package py.com.middleware.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import py.com.middleware.application.gateway.MqttGateway;

@RestController
@RequestMapping("main")
public class MainController {
	
	@Autowired
	private MqttGateway mqttGateway;
	
	@GetMapping("/ping")
	public ResponseEntity<String> ping(){
		return ResponseEntity.ok("pong");
	}
	
	@PostMapping("/sendMessage")
	public ResponseEntity<String> sendMessage(@RequestBody String body, @Header("topic") String topic){		
		mqttGateway.senToMqtt(body, topic);
		return ResponseEntity.ok("processed");		
	}

}
