package com.bank.app.components.role.controller;

import com.bank.app.components.role.model.Role;
import com.bank.app.shared.controllers.SuperController;
import com.bank.app.shared.repositories.UowService;
import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("roles")
public class RoleController extends SuperController<Role, Long> {

    private UowService uow;

	public RoleController(UowService uow) {
		super(uow.roles);
		this.uow = uow;
	}

}
