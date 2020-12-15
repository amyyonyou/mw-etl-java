package com.mw.base;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class GlobalData {

	public HttpHeaders getHttpHeaders() {
		// Prepare acceptable media type
		List<MediaType> acceptableMediaTypes = new ArrayList<>();
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
		// Prepare header
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		return headers;
	}

	public HttpHeaders getHttpHeadersForFile() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "multipart/form-data");
		return headers;
	}
}
