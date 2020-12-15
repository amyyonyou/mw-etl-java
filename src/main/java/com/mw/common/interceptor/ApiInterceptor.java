package com.mw.common.interceptor;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.mw.base.filter.log.RequestWrapper;
import com.mw.base.filter.log.ResponseWrapper;
import com.mw.plugins.message.MessageService;

public class ApiInterceptor extends HandlerInterceptorAdapter {
	@Resource
	private MessageService messageService;

	private static final String[] RESPONSE_CONTENT_TYPE_PRINT = { "application/json" };

	private String getReqSrc(String path) {
		String[] paths = StringUtils.split(path, "/");
		String reqSrc = null;
		if (StringUtils.startsWith(path, "/wechatwork/") || StringUtils.startsWith(path, "/eam/") || StringUtils.startsWith(path, "/crm/")) {
			reqSrc = paths[1];
		}

		return reqSrc;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String path = StringUtils.substring(request.getRequestURI(), request.getContextPath().length());
		logger.info("postHandle-> path={}", path);
		String reqSrc = getReqSrc(path);
		logger.info("postHandle-> reqSrc={}", reqSrc);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		try {
			String path = StringUtils.substring(request.getRequestURI(), request.getContextPath().length());
			logger.info("afterCompletion-> path={}", path);
			String reqSrc = getReqSrc(path);
			logger.info("afterCompletion-> reqSrc={}", reqSrc);

			if (StringUtils.isBlank(reqSrc)) {
				return;//TODO will validate illegal reqSrc
			}

			// request = new RequestWrapper(requestId, request);
			// response = new ResponseWrapper(requestId, response);

			String reqData = getReqData(request);
			String respData = getRespData((ResponseWrapper) response);
			
			logger.info("afterCompletion-> reqData={}", reqData);
			logger.info("afterCompletion-> respData={}", respData);
			
		} catch (Exception e) {
			logger.warn("afterCompletion", e);
		}
	}

	private String getReqData(final HttpServletRequest request) {
		StringBuilder msg = new StringBuilder();

		if (request instanceof RequestWrapper) {
			RequestWrapper requestWrapper = (RequestWrapper) request;
			try {
				String charEncoding = requestWrapper.getCharacterEncoding() != null
						? requestWrapper.getCharacterEncoding()
						: "UTF-8";

				byte[] data = requestWrapper.toByteArray();
				if (isMultipart(request)) {
					if (data != null) {
						msg.append(data.length);
					}
				} else {
					msg.append(new String(data, charEncoding));
				}
			} catch (UnsupportedEncodingException e) {
				logger.warn("Failed to parse request payload", e);
			}
		}

		return msg.toString();
	}

	private boolean isMultipart(final HttpServletRequest request) {
		return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
	}

	private String getRespData(final ResponseWrapper response) {
		StringBuilder msg = new StringBuilder();
		try {
			boolean ifPrint = false;
			for (String ctyp : RESPONSE_CONTENT_TYPE_PRINT) {
				if (StringUtils.startsWith(response.getContentType(), ctyp)) {
					ifPrint = true;
					break;
				}
			}

			byte[] data = response.toByteArray();
			if (ifPrint) {
				msg.append(new String(data, response.getCharacterEncoding()));
			} else {
				if (data != null) {
					msg.append(data.length);
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.warn("Failed to parse response payload", e);
		}

		return msg.toString();
	}

	private final static Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);
}
