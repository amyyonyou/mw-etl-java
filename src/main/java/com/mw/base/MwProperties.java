package com.mw.base;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix="com.mw")
public class MwProperties {
	private String maximoApiUrl;
	private String weaverApiUrl;
	private String weaverApiSecrect;
	
	public String getMaximoApiUrl() {
		return maximoApiUrl;
	}

	public void setMaximoApiUrl(String maximoApiUrl) {
		this.maximoApiUrl = maximoApiUrl;
	}

	public String getWeaverApiSecrect() {
		return weaverApiSecrect;
	}

	public void setWeaverApiSecrect(String weaverApiSecrect) {
		this.weaverApiSecrect = weaverApiSecrect;
	}

	public String getWeaverApiUrl() {
		return weaverApiUrl;
	}

	public void setWeaverApiUrl(String weaverApiUrl) {
		this.weaverApiUrl = weaverApiUrl;
	}
}
