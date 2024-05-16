package com.bank.app.components.operation.controller;

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
import com.bank.app.components.account.model.Account;
import com.bank.app.components.operation.model.*;

import org.springframework.data.domain.*;
import org.hibernate.exception.ConstraintViolationException;

@RestController
@RequestMapping(value = "api/operations", produces = { "application/json" })
public class OperationsController extends SuperController<Operation, Long> {

    public UowService uow;

    public OperationsController(UowService uow) {
        super(uow.operations);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{accountId}")
    // @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
            @PathVariable String sortBy, @PathVariable String sortDir, @PathVariable Long accountId) {
        var sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);

        var query = uow.operations.findAllQ(accountId, PageRequest.of(startIndex, pageSize, sort));

        var list = query.getContent();

        var count = query.getTotalElements();

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
    public ResponseEntity<?> addRange(@RequestBody List<Operation> models) {
        return super.addRange(models);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getById/{id}")
    @Override
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PutMapping("/update/{id}")
    @Override
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Operation model) {
        var existed = uow.operations.findById(id).orElse(null);

        var accountDebit = uow.accounts.findById(model.getAccountDebit_id()).orElse(null);
        var accountCredit = uow.accounts.findById(model.getAccountCredit_id()).orElse(null);

        if (accountDebit == null || accountCredit == null || model.getAmount() > accountDebit.getBalance()) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "Insufficient balance"));
        }

        accountDebit.setBalance(accountDebit.getBalance() + existed.getAmount() - model.getAmount());
        uow.accounts.save(accountDebit);

        accountCredit.setBalance(accountCredit.getBalance() - existed.getAmount() + model.getAmount());
        uow.accounts.save(accountCredit);


        return super.update(id, model);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/add")
    @Override
    public ResponseEntity<?> add(@RequestBody Operation model) {

        var accountDebit = uow.accounts.findById(model.getAccountDebit_id()).orElse(null);
        var accountCredit = uow.accounts.findById(model.getAccountCredit_id()).orElse(null);

        if (accountDebit == null || accountCredit == null || model.getAmount() > accountDebit.getBalance()) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "Insufficient balance"));
        }

        accountDebit.setBalance(accountDebit.getBalance() - model.getAmount());
        uow.accounts.save(accountDebit);

        accountCredit.setBalance(accountCredit.getBalance() + model.getAmount());
        uow.accounts.save(accountCredit);

        var o = uow.operations.save(model);

        return ResponseEntity.ok(o);
    }

    @PatchMapping(path = "/patch/{id}")
    @Override
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody JsonPatch patch) {
        Optional<Operation> optional = repository.findById(id);

        if (optional.isPresent() == false) {
            return ResponseEntity.notFound().build();
        }

        Operation target = optional.get();

        try {

            JsonNode patched = patch.apply(uow.objectMapper.convertValue(target, JsonNode.class));

            Operation model = uow.objectMapper.treeToValue(patched, Operation.class);

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