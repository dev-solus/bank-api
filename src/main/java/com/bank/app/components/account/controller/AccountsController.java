package com.bank.app.components.account.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.*;

import jakarta.annotation.security.RolesAllowed;
import com.bank.app.shared.controllers.SuperController;
import com.bank.app.shared.repository.UowService;

import com.bank.app.shared.dto.Roles;
import com.bank.app.components.account.model.*;
import com.bank.app.components.account.repository.*;

import org.springframework.data.domain.*;
import org.hibernate.exception.ConstraintViolationException;

@RestController
@RequestMapping(value ="api/accounts", produces = { "application/json" })
public class AccountsController extends SuperController<Account, Long> {

    public UowService uow;

    public AccountsController(UowService uow) {
        super(uow.accounts);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}")
    @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
            @PathVariable String sortBy, @PathVariable String sortDir) {
        Sort sort = Sort.by(sortDir == "desc" ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        Page<Account> query = repository.findAll(PageRequest.of(startIndex, pageSize, sort));

        List<?> list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("accountNumber", e.getAccountNumber());
                put("balance", e.getBalance());
                put("id", e.getId());
                put("status", e.getStatus());
                put("user", e.getUser());
            }
        }).toList();

        Long count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/get")
    @Override
    public ResponseEntity<?> get() {
        return super.get();
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/postRange")
    @Override
    public ResponseEntity<?> postRange(@RequestBody List<Account> models) {
        return super.postRange(models);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getById/{id}")
    @Override
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PutMapping("/put/{id}")
    @Override
    public ResponseEntity<?> put(@PathVariable Long id, @RequestBody Account model) {
        return super.put(id, model);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/post")
    @Override
    public ResponseEntity<?> post(@RequestBody Account model) {
        return super.post(model);
    }

    @PatchMapping(path = "/patch/{id}")
    @Override
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody JsonPatch patch) {
        Optional<Account> optional = repository.findById(id);

        if (optional.isPresent() == false) {
            return ResponseEntity.notFound().build();
        }

        Account target = optional.get();

        try {

            JsonNode patched = patch.apply(uow.objectMapper.convertValue(target, JsonNode.class));

            Account model = uow.objectMapper.treeToValue(patched, Account.class);

            repository.save(model);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException) e.getCause();
                return new ResponseEntity<>(se.getSQLException().getMessage(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @DeleteMapping("/delete/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return super.delete(id);
    }

}