package com.bank.app.shared.repository;


import com.bank.app.components.operation.repository.OperationRepository;
import com.bank.app.components.role.repository.RoleRepository;
import com.bank.app.components.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.bank.app.components.account.repository.*;


@Service
@Transactional
public class UowService {
	@Autowired
	public UserRepository users;
	@Autowired public RoleRepository roles;
	@Autowired public AccountRepository accounts;
	@Autowired public OperationRepository operations;


  	// @Autowired 
	public BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();;
	@Autowired public UtilsService utils;
	@Autowired public ObjectMapper objectMapper;
}