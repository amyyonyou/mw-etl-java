package com.mw.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mw.common.interceptor.ApiInterceptor;

@Configuration
public class AppConfig implements WebMvcConfigurer {

	@Bean
	public ApiInterceptor configApiInterceptor() {
		return new ApiInterceptor();
	}

	public void addInterceptors(InterceptorRegistry interceptorRegistry) {
		interceptorRegistry.addInterceptor(configApiInterceptor())
				.addPathPatterns(new String[] { "/wechatwork/**", "/eam/**", "/crm/**" });
	}
	
	@Bean
	public MessageSource messageSource() {
	    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
	    messageSource.setBasename("classpath:messages/messages");
	    messageSource.setCacheSeconds(10); //reload messages every 10 seconds
	    messageSource.setDefaultEncoding("UTF-8");
	    return messageSource;
	}
}
