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
@RequestMapping(value = "api/accounts", produces = { "application/json" })
public class AccountsController extends SuperController<Account, Long> {

    public UowService uow;

    public AccountsController(UowService uow) {
        super(uow.accounts);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{cin}/{balanceMin}/{balanceMax}/{user_id}")
    public ResponseEntity<?> GetAll(
            @PathVariable int startIndex, @PathVariable int pageSize, @PathVariable String sortBy,
            @PathVariable String sortDir, @PathVariable String cin, @PathVariable Long balanceMin,
            @PathVariable Long balanceMax, @PathVariable Long user_id) {
        var sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        var isAdmin = uow.utils.isAdmin();
        var userId = uow.utils.getUserId();

        var query = uow.accounts.findAll((r, q, cb) -> cb.and(
            //
            isAdmin ? cb.and() : cb.equal(r.get("user_id"), userId),
            //
            cin.equals("*") ? cb.and()
                    : cb.like(cb.lower(r.get("user").get("cin")), "%" + cin.toLowerCase() + "%"),
            balanceMin.equals(balanceMax) ? cb.and() : cb.between(r.get("balance"), balanceMin, balanceMax),
            user_id.equals(0L) ? cb.and() : cb.equal(r.get("user_id"), user_id)),
            PageRequest.of(startIndex, pageSize, sort));

        var list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("accountNumber", e.getAccountNumber());
                put("balance", e.getBalance());
                put("id", e.getId());
                put("status", e.getStatus());
                put("user_id", e.getUser_id());
                put("user", new HashMap<String, Object>() {
                    {
                        put("id", e.getUser().getId());
                        put("firstname", e.getUser().getFirstname());
                        put("lastname", e.getUser().getLastname());
                        // put("email", e.getUser().getEmail());
                        put("cin", e.getUser().getCin());
                        // put("address", e.getUser().getAddress());
                        // put("birthdate", e.getUser().getBirthdate());
                        // put("active", e.getUser().getActive());
                        // put("avatar", e.getUser().getAvatar());
                    }
                });
            }
        }).toList();

        var count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }

    // add endpoint to get all accounts by user_id
    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAllByUserId/{user_id}")
    public ResponseEntity<?> GetAllByUserId( @PathVariable Long user_id) {
        var isAdmin = uow.utils.isAdmin();
        var userId = uow.utils.getUserId();

        var clientId = isAdmin ? user_id : userId;

        var list = uow.accounts
            .findAll((r, q, cb) -> cb.equal(r.get("user_id"), clientId))
            .stream().map(e -> new HashMap<String, Object>() {
                {
                    put("accountNumber", e.getAccountNumber());
                    put("balance", e.getBalance());
                    put("id", e.getId());
                    put("status", e.getStatus());
                    put("user_id", e.getUser_id());
                }
            })
            .toList();


        return ResponseEntity.ok(list);
    }
    

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/get")
    @Override
    public ResponseEntity<?> get() {
        return super.get();
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/postRange")
    @Override
    public ResponseEntity<?> addRange(@RequestBody List<Account> models) {
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
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Account model) {
        return super.update(id, model);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/add")
    @Override
    public ResponseEntity<?> add(@RequestBody Account model) {
        return super.add(model);
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

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @DeleteMapping("/delete/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return super.delete(id);
    }

}