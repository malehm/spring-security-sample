package com.malem.sample.spring.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class SpringSecurityApplication {
	
	static {
	    System.setProperty("jdk.httpclient.HttpClient.log", "all");
	  }

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityApplication.class, args);
	}
	
	@Bean
	SecurityFilterChain web(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> authenticationConverter) throws Exception {
	    return http
	    	.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(authorize -> authorize
	        		.requestMatchers("/api/v1/tax").hasAuthority("user")
	        		.requestMatchers("/api/v1/*").anonymous()
	        )
	        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtDecoder -> jwtDecoder.jwtAuthenticationConverter(authenticationConverter)))
	        .build();
	}
	
	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter(Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> authoritiesConverter.convert(jwt.getClaims()));
		return authenticationConverter;
	}
	
	interface AuthoritiesConverter extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {}

	@Bean
	AuthoritiesConverter realmRolesAuthoritiesConverter() {
	  return jwtClaims -> Optional.ofNullable((Map<String, Object>) jwtClaims.get("resource_access"))
			.flatMap(resources -> Optional.ofNullable((Map<String, Object>) resources.get("spring-security-service")))
			.flatMap(resource -> Optional.ofNullable((List<String>) resource.get("roles")))
			.map(List::stream)
			.orElse(Stream.empty())
			.map(SimpleGrantedAuthority::new)
			.map(GrantedAuthority.class::cast)
			.toList();
	}
	
	@Bean
	RestClient restClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager) {
		OAuth2ClientHttpRequestInterceptor requestInterceptor =
			new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

		return builder.requestInterceptor(requestInterceptor).build();
	}

}
