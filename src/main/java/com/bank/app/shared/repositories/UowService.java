package com.bank.app.shared.repositories;

import javax.transaction.Transactional;

import com.bank.app.components.role.repositories.RolesRepository;
import com.bank.app.components.user.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UowService {
	@Autowired public UserRepository users;
	@Autowired public RolesRepository roles;
	

	
	@Autowired public UtilsService utils;
	@Autowired public ObjectMapper objectMapper;
	
}