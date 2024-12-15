package com.malem.sample.spring.security.api.v1;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/v1")
public class SecurityAPI {

	private static final String HOST = "localhost";
	private static final String URL_TEMPLATE = "http://%s:%s";
	private final RestClient restClient;
	private final long price = 100;
	private final ServerPortService port;

	public SecurityAPI(final RestClient restClient, ServerPortService serverPortService) {
		this.restClient = restClient;
		this.port = serverPortService;
	}

	@GetMapping(path = "/prices", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Price> prices() {
		Tax tax = this.restClient.mutate().baseUrl(URL_TEMPLATE.formatted(HOST, port.get()))
				.build()
				.get()
				.uri("/api/v1/tax")
				.accept(MediaType.APPLICATION_JSON)
				.attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("spring-security-client"))
				.retrieve()
				.body(Tax.class);
		return List.of(new Price("someone", "Garbage", tax.tax() / 100 * price, "How to not to do in JAVA."));
	}

	@GetMapping(path = "/tax", produces = MediaType.APPLICATION_JSON_VALUE)
	public Tax tax() {
		return new Tax(19);
	}

}
