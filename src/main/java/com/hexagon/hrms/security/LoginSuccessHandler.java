package com.hexagon.hrms.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexagon.hrms.service.AccountService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final AccountService accountService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RequestCache requestCache = new HttpSessionRequestCache();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		AccountDetails accountDetails = (AccountDetails) authentication.getPrincipal();
		accountService.loginSucceeded(accountDetails.getAccount());

		String url = request.getContextPath() + "/dashboard";
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		if (savedRequest != null) {
			url = savedRequest.getRedirectUrl();
			requestCache.removeRequest(request, response);
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("result", "success");
		body.put("url", url);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), body);
	}
}
