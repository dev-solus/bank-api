package com.bank.app.shared.repositories;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bank.app.shared.dto.ResponseAuthServer;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class UtilsService {
    @Autowired
    public HttpServletRequest request;

    public Map<String, Object> getUser() {
        var r =   (ResponseAuthServer) ((UsernamePasswordAuthenticationToken) this.request.getUserPrincipal()).getPrincipal();

        Map<String, Object> user  = new HashMap<String, Object>() {{
			put("id", r.id);
			put("role", r.role);
			put("email", r.email);
		}};

        return user;
    }

	public Date castDate(String date) {
		try {
			return  (Date) new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public Long castLong(JsonNode number) {
		try {
			return number.asLong() == 0L ? null : number.asLong();
		} catch (Exception e) {
			return null;
		}
	}
}
        