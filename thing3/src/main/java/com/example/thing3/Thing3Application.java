package com.example.thing3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rabbitmq.client.AMQP;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.function.Consumer;

@SpringBootApplication
public class Thing3Application {

	public static void main(String[] args) {
		SpringApplication.run(Thing3Application.class, args);
	}

}

@Configuration
class Thing3Config {
	@Bean
	Consumer<Aircraft> logIt() {
		return ac -> System.out.println("log : "+ ac);
	}
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Aircraft {
	private String callsign, reg, flightno, type;
	private int altitude, heading, speed;
	private double lat, lon;
}

@Data
@AllArgsConstructor
class Weather {
	private Instant when;
	private String observation;
}

