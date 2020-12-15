package com.mw.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mw.utils.SSLUtils;

@Component
public class RestTemplateConfig {
	
	@Bean
	public RestTemplate restTemplate() throws KeyManagementException, NoSuchAlgorithmException{
		RestTemplate restTemplate = new RestTemplate();
		SSLUtils.fixUntrustCertificate();
		return restTemplate;
	};
}
