package com.bank.app.components.user.controller;

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
import com.bank.app.components.user.model.*;

import org.springframework.data.domain.*;
import org.hibernate.exception.ConstraintViolationException;

@RestController
@RequestMapping(value = "api/users", produces = { "application/json" })
public class UsersController extends SuperController<User, Long> {

    public UowService uow;

    public UsersController(UowService uow) {
        super(uow.users);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{firstname}/{cin}/{email}")
    // @Override
    public ResponseEntity<?> GetAll(
            @PathVariable int startIndex,
            @PathVariable int pageSize,
            @PathVariable String sortBy,
            @PathVariable String sortDir,
            @PathVariable String firstname,
            @PathVariable String cin,
            @PathVariable String email
    ) {
        var sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        var isAdmin = uow.utils.isAdmin();
        var userId = uow.utils.getUserId();
        var user = uow.utils.getUser();

        var query = uow.users.findAll((r, q, cb) -> cb.and(
                isAdmin ? cb.and() : cb.equal(r.get("id"), userId),
                firstname.equals("*") ? cb.and()
                        : cb.or(
                                cb.like(cb.lower(r.get("firstname")), "%" + firstname.toLowerCase() + "%"),
                                cb.like(cb.lower(r.get("lastname")), "%" + firstname.toLowerCase() + "%")),
                email.equals("*") ? cb.and() : cb.like(cb.lower(r.get("email")), "%" + email.toLowerCase() + "%"),
                cin.equals("*") ? cb.and() : cb.like(cb.lower(r.get("cin")), "%" + cin.toLowerCase() + "%")),
                PageRequest.of(startIndex, pageSize, sort));

        var list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("active", e.getActive());
                put("address", e.getAddress());
                put("avatar", e.getAvatar());
                put("birthdate", e.getBirthdate());
                put("cin", e.getCin());
                put("email", e.getEmail());
                put("firstname", e.getFirstname());
                put("gender", e.getGender());
                put("id", e.getId());
                put("lastname", e.getLastname());
                put("phone", e.getPhone());
                put("role", e.getRole());
            }
        }).toList();

        var count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/autoComplete/{filter}")
    // @Override
    public ResponseEntity<?> autoComplete(@PathVariable String filter) {
        var sort = Sort.by(Sort.Direction.fromString("desc"), "id");

        var query = uow.operations.findAll((r, q, cb) -> filter.equals("*") ? cb.and() : cb.or(
            cb.like(cb.lower(r.get("firstname")), "%" + filter.toLowerCase() + "%"),
            cb.like(cb.lower(r.get("lastname")), "%" + filter.toLowerCase() + "%"),
            cb.like(cb.lower(r.get("email")), "%" + filter.toLowerCase() + "%"),
            cb.like(cb.lower(r.get("cin")), "%" + filter.toLowerCase() + "%")
        ), PageRequest.of(0, 10, sort));

        var list = query.getContent();

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
    public ResponseEntity<?> addRange(@RequestBody List<User> models) {
        return super.addRange(models);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getById/{id}")
    @Override
    public ResponseEntity<?> getById(@PathVariable Long id) {
        var model = repository.findById(id).orElse(null);

        model.setPassword(null);

        return ResponseEntity.ok(model);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getForSelect")
    public ResponseEntity<?> getForSelect() {
        var list = uow.users.getForSelect();

        return ResponseEntity.ok(list);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getWithAccounts")
    public ResponseEntity<?> getWithAccounts() {
        // var list = uow.users.findAll().stream().map(e -> new HashMap<String, Object>() {
        //     {
        //         put("id", e.getId());
        //         put("name", e.getFirstname() + " " + e.getLastname());
        //         put("accounts", e.getAccounts().stream().map(a -> new HashMap<String, Object>() {
        //             {
        //                 put("id", a.getId());
        //                 put("accountNumber", a.getAccountNumber());
        //                 put("balance", a.getBalance());
        //                 put("status", a.getStatus());
        //             }
        //         }).toList());
        //     }
        // }).toList();

        var accounts = uow.accounts.getForSelect();

        var list = uow.users.findAll().stream().map(e -> new HashMap<String, Object>() {
            {
                put("id", e.getId());
                put("name", e.getFirstname() + " " + e.getLastname());
                put("accounts", accounts.stream().filter(a -> a.user_id() == e.getId()).toList());
            }
        }).toList();

        return ResponseEntity.ok(list);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PutMapping("/update/{id}")
    @Override
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User model) {
        var existed = repository.findById(id);

        if(existed.isPresent() == false){
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        if (model.getPassword() == null || model.getPassword().isEmpty() == true) {
            model.setPassword(existed.get().getPassword());
        } else {
            model.setPassword(uow.bCrypt.encode(model.getPassword()));
        }

        var o = repository.save(model);

        return ResponseEntity.ok(o);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/add")
    @Override
    public ResponseEntity<?> add(@RequestBody User model) {
        return super.add(model);
    }

    @PatchMapping(path = "/patch/{id}")
    @Override
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody JsonPatch patch) {
        var existant = repository.findById(id).orElse(null);

        if (existant == null) {
            return ResponseEntity.notFound().build();
        }

        try {

            var patched = patch.apply(uow.objectMapper.convertValue(existant, JsonNode.class));

            var model = uow.objectMapper.treeToValue(patched, User.class);

            repository.save(model);

            return ResponseEntity.ok(Map.of("message","Enregiseremet reussi", "code", 1));

        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException) e.getCause();
                return ResponseEntity.ok(Map.of("message", se.getSQLException().getMessage(), "code", -1));
            }
            return ResponseEntity.ok(Map.of("message", e.getMessage(), "code", -1));
        }
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @DeleteMapping("/delete/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return super.delete(id);
    }

}