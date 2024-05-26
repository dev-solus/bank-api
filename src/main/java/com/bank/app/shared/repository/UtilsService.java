package com.bank.app.shared.repository;

import java.util.HashMap;
import java.util.Map;

import com.bank.app.shared.dto.ResponseAuthServer;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class UtilsService {
	@Autowired
	public HttpServletRequest request;

	public Map<String, Object> getUser() {
		var r = (ResponseAuthServer) ((UsernamePasswordAuthenticationToken) this.request.getUserPrincipal())
				.getPrincipal();

		var user = new HashMap<String, Object>() {
			{
				put("id", r.id);
				put("role", r.role);
				put("email", r.email);
			}
		};

		return user;
	}

	public boolean isAdmin() {
		return this.getUser().get("role").toString().equals("AGENT_GUICHET");
	}

	public Long getUserId() {
		return (Long) this.getUser().get("id");
	}

	public String getUserRole() {
		return this.getUser().get("role").toString();
	}


}
