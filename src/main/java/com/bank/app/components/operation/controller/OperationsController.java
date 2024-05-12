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
import com.bank.app.components.operation.model.*;
import com.bank.app.components.operation.repository.*;

import org.springframework.data.domain.*;
import org.hibernate.exception.ConstraintViolationException;

@RestController
@RequestMapping(value ="api/operations", produces = { "application/json" })
public class OperationsController extends SuperController<Operation, Long> {

    public UowService uow;

    public OperationsController(UowService uow) {
        super(uow.operations);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}")
    @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
            @PathVariable String sortBy, @PathVariable String sortDir) {
        Sort sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        Page<Operation> query = repository.findAll(PageRequest.of(startIndex, pageSize, sort));

        List<?> list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("account", e.getAccountDebit());
                put("accountDist", e.getAccountCredit());
                put("amount", e.getAmount());
                put("date", e.getDate());
                put("description", e.getDescription());
                put("id", e.getId());
                put("operationType", e.getOperationType());
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
        return super.update(id, model);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/add")
    @Override
    public ResponseEntity<?> add(@RequestBody Operation model) {
        return super.add(model);
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