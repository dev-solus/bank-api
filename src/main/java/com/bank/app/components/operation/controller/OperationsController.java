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
import com.bank.app.components.user.model.User;

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

    // @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    // @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{accountId}")
    // // @Override
    // public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
    //         @PathVariable String sortBy, @PathVariable String sortDir, @PathVariable Long accountId) {
    //     var sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);

    //     var query = uow.operations.findAllQ(accountId, PageRequest.of(startIndex, pageSize, sort));

    //     var list = query.getContent();

    //     var count = query.getTotalElements();

    //     return ResponseEntity.ok(Map.of("count", count, "list", list));
    // }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{userId}/{accountId}")
    // @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
            @PathVariable String sortBy, @PathVariable String sortDir, @PathVariable Long userId, @PathVariable Long accountId) {
        var sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);

        var clientId = uow.utils.getUserId();
        var isAdmin = uow.utils.isAdmin();

        final var _userId = isAdmin ? userId : clientId;

        var query = uow.operations.findAll((r, q, cb) -> {
            var userPre = _userId.equals(0L) ? cb.and() : cb.or(
                cb.equal(r.get("accountDebit").get("user_id"), _userId),
                cb.equal(r.get("accountDebit").get("user_id"), _userId),
                cb.equal(r.get("accountDebit").get("user_id"), _userId),
                cb.equal(r.get("accountDebit").get("user_id"), _userId),
                //
                cb.equal(r.get("accountCredit").get("user_id"), _userId),
                cb.equal(r.get("accountCredit").get("user_id"), _userId),
                cb.equal(r.get("accountCredit").get("user_id"), _userId),
                cb.equal(r.get("accountCredit").get("user_id"), _userId)
            );

            var accountPre = accountId.equals(0L) ? cb.and() : cb.or(
                cb.equal(r.get("accountDebit_id"), accountId),
                cb.equal(r.get("accountCredit_id"), accountId)
            );

            return cb.and(userPre, accountPre);
        }, PageRequest.of(startIndex, pageSize, sort));

        var list = query.getContent();

        var count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }

    // private List<Account> getAccountsByUser(String filter) {
    //     var isAdmin = uow.utils.isAdmin();

    //     if (filter.equals("*") && isAdmin) {
    //         return new ArrayList<>();
    //     }

    //     var userId = uow.utils.getUserId();

    //     var accounts = uow.accounts.findAll((r, q, cb) -> {
    //         var clientPre = isAdmin ? cb.and() :  cb.equal(r.get("user_id"), userId);

    //         var filterPre = filter.equals("*") ? cb.and() : cb.or(
    //             cb.like(cb.lower(r.get("user").get("firstname")), "%" + filter.toLowerCase() + "%"),
    //             cb.like(cb.lower(r.get("user").get("lastname")), "%" + filter.toLowerCase() + "%"),
    //             cb.like(cb.lower(r.get("user").get("email")), "%" + filter.toLowerCase() + "%"),
    //             cb.like(cb.lower(r.get("user").get("cin")), "%" + filter.toLowerCase() + "%")
    //         );

    //         return cb.and(filterPre, clientPre);
    //     }).stream().map(e -> Account
    //             .builder()
    //             .id(e.getId())
    //             .accountNumber(e.getAccountNumber())
    //             .balance(e.getBalance())
    //             .user_id(e.getUser_id())
    //             .build()
    //         ).toList();

    //     return accounts;
    // }

    

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/get")
    @Override
    public ResponseEntity<?> get() {
        return super.get();
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/postRange")
    @Override
    public ResponseEntity<?> addRange(@RequestBody List<Operation> models) {
        return super.addRange(models);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getById/{id}")
    @Override
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PutMapping("/update/{id}")
    @Override
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Operation model) {
        var existedOperation = uow.operations.findById(id).orElse(null);

        var accountDebit = uow.accounts.findById(model.getAccountDebit_id()).orElse(null);
        var accountCredit = uow.accounts.findById(model.getAccountCredit_id()).orElse(null);

        if (accountDebit == null || accountCredit == null || model.getAmount() > accountDebit.getBalance() + existedOperation.getAmount()) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "Solde insuffisant"));
        }

        accountDebit.setBalance(accountDebit.getBalance() + existedOperation.getAmount() - model.getAmount());
        uow.accounts.save(accountDebit);

        accountCredit.setBalance(accountCredit.getBalance() - existedOperation.getAmount() + model.getAmount());
        uow.accounts.save(accountCredit);


        return super.update(id, model);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/add")
    @Override
    public ResponseEntity<?> add(@RequestBody Operation model) {

        var accountDebit = uow.accounts.findById(model.getAccountDebit_id()).orElse(null);
        var accountCredit = uow.accounts.findById(model.getAccountCredit_id()).orElse(null);

        if (accountDebit == null || accountCredit == null || model.getAmount() > accountDebit.getBalance()) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "Solde insuffisant"));
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

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @DeleteMapping("/delete/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable Long id) {
        var model = repository.findById(id).orElse(null);

        if (model == null) {
            return ResponseEntity.notFound().build();
        }

        var existedOperation = uow.operations.findById(id).orElse(null);

        var accountDebit = uow.accounts.findById(model.getAccountDebit_id()).orElse(null);
        var accountCredit = uow.accounts.findById(model.getAccountCredit_id()).orElse(null);

        

        accountDebit.setBalance(accountDebit.getBalance() +  model.getAmount());
        uow.accounts.save(accountDebit);

        accountCredit.setBalance(accountCredit.getBalance() - model.getAmount());
        uow.accounts.save(accountCredit);

        repository.deleteById(id);

        return ResponseEntity.ok(Boolean.TRUE);
    }
}