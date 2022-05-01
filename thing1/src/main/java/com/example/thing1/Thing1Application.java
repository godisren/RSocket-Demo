package com.example.thing1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.time.Instant;

@SpringBootApplication
public class Thing1Application {

    public static void main(String[] args) {
        SpringApplication.run(Thing1Application.class, args);
    }

    @Bean
    WebClient client() {
        // reactor code hooks
        Hooks.onErrorDropped(e -> System.out.println("Client disconnect! Goodbye"));
        return WebClient.create("http://127.0.0.1:7634/aircraft");
    }

    @Bean
    RSocketRequester requester(RSocketRequester.Builder builder) {
        return builder.tcp("127.0.0.1", 7635);
    }

}

// @RestController is blocking model (synchronous)
//@RestController
//@AllArgsConstructor
//class Thing1Controller {
//
//	private final WebClient client;
//
//	// return Mono (Single)
//	/*
//	@GetMapping("/reqresp")
//	Mono<Aircraft> reqResp() {
//		return client.get().retrieve()
//				.bodyToFlux(Aircraft.class)
//				.next();
//	}
//	 */
//
//	// return Flux (Many)
//	@GetMapping("/reqresp")
//	Flux<Aircraft> reqResp() {
//		return client.get().retrieve()
//				.bodyToFlux(Aircraft.class);
//	}
//}

/**
 * Change RestController to RSocket :
 * - enable RSocket port by setting application.properties `spring.rsocket.server.port=9091`
 * - change @RestController to @Controller
 * - change @GetMapping("/reqresp") to @MessageMapping("reqstream")
 */
//@RestController
@Controller
@AllArgsConstructor
class Thing1Controller {

    //    private final WebClient client;
    private final RSocketRequester requester;

    // Request/stream
    // @GetMapping("/reqresp")
    @MessageMapping("reqstream")
    Flux<Aircraft> reqResp(Mono<Instant> tsMono) {

        return tsMono.doOnNext(ts -> System.out.println("time:" + ts))
                .thenMany(
                        requester.route("acstream")
                                .data(Instant.now())
                                .retrieveFlux(Aircraft.class));
    }

    // Bidirectional Channel
    @MessageMapping("channel")
    Flux<Aircraft> channel(Flux<Weather> weatherFlux) {
        return weatherFlux.doOnSubscribe(sub -> System.out.println("Subscribed to weather!"))
                .doOnNext(wx -> System.out.println("weather: " + wx))
                .switchMap(wx -> requester.route("acstream")
                        .data(Instant.now())
                        .retrieveFlux(Aircraft.class));

    }
}

@Data
class Weather {
    private Instant when;
    private String observation;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Aircraft {
    private String callsign, reg, flightno, type;
    private int altitude, heading, speed;
    private double lat, lon;
}
