/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EDSClientApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(EDSClientApplication.class, args);
	}
	
	@Bean
	@Qualifier("restTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
