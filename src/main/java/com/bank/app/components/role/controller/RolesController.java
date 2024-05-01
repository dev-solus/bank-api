package com.bank.app.components.role.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;

import java.time.LocalDate;
import java.util.*;

import jakarta.annotation.security.RolesAllowed;
import com.bank.app.shared.controllers.SuperController;
import com.bank.app.shared.repository.UowService;

import com.bank.app.shared.dto.Roles;
import com.bank.app.components.account.model.Account;
import com.bank.app.components.operation.model.Operation;
import com.bank.app.components.role.model.*;
import com.bank.app.components.role.repository.*;
import com.bank.app.components.user.model.User;

import org.springframework.data.domain.*;
import org.hibernate.exception.ConstraintViolationException;

@RestController
@RequestMapping("api/roles")
public class RolesController extends SuperController<Role, Long> {

    public UowService uow;

    public RolesController(UowService uow) {
        super(uow.roles);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}")
    @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
            @PathVariable String sortBy, @PathVariable String sortDir) {
        Sort sort = Sort.by(sortDir == "desc" ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        Page<Role> query = repository.findAll(PageRequest.of(startIndex, pageSize, sort));

        List<?> list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("id", e.getId());
                put("name", e.getName());
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
    public ResponseEntity<?> postRange(@RequestBody List<Role> models) {
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
    public ResponseEntity<?> put(@PathVariable Long id, @RequestBody Role model) {
        return super.put(id, model);
    }

    @RolesAllowed({ Roles.ADMIN, Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/post")
    @Override
    public ResponseEntity<?> post(@RequestBody Role model) {
        return super.post(model);
    }

    @PatchMapping(path = "/patch/{id}")
    @Override
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody JsonPatch patch) {
        Optional<Role> optional = repository.findById(id);

        if (optional.isPresent() == false) {
            return ResponseEntity.notFound().build();
        }

        Role target = optional.get();

        try {

            JsonNode patched = patch.apply(uow.objectMapper.convertValue(target, JsonNode.class));

            Role model = uow.objectMapper.treeToValue(patched, Role.class);

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

    @GetMapping("/generateTestingData")
    public ResponseEntity<?> generateTestingData() {

        // delete operations, accounts, users, roles
        uow.operations.deleteAll();
        uow.accounts.deleteAll();
        uow.users.deleteAll();
        // uow.roles.deleteAll();

        // generate 3 roles on each one 1 user as admin, 1 user as client, 1 user as
        // agent guichet
        // List<Role> roles = new ArrayList<Role>();

        // Role role = new Role();
        // role.setName("ADMIN");
        // roles.add(role);

        // role = new Role();
        // role.setName("CLIENT");
        // roles.add(role);

        // role = new Role();
        // role.setName("AGENT_GUICHET");
        // roles.add(role);

        // repository.saveAll(roles);

        // create users
        List<User> users = new ArrayList<User>();
        var password = uow.bCrypt.encode("123");

        User user = new User();
        user.setCin("AD1");
        user.setFirstname("Admin");
        user.setLastname("Admin");
        user.setAddress("Temara");
        user.setBirthdate(new Date(1990, 1, 1));
        user.setEmail("admin@bank.com");
        user.setRole_id(1L);
        user.setPassword(password);
        user.setActive(true);
        users.add(user);

        user = new User();
        user.setCin("AG1");
        user.setFirstname("Agent");
        user.setLastname("Agent");
        user.setAddress("Temara");
        user.setBirthdate(new Date(1990, 1, 1));
        user.setEmail("agent@bank.com");
        user.setRole_id(3L);
        user.setPassword(password);
        user.setActive(true);
        users.add(user);

        for (int i = 1; i <= 10; i++) {
            user = new User();
            user.setCin("AA" + i);
            user.setFirstname("Client_" + i);
            user.setLastname("Client_" + i);
            user.setAddress("Temara");
            user.setBirthdate(new Date(1990, 1, 1));
            user.setEmail("client_" + i + "@bank.com");
            user.setRole_id(2L);
            user.setPassword(password);
            user.setActive(true);
            users.add(user);
        }

        uow.users.saveAll(users);

        var accounts = new ArrayList<Account>();
        // for each user generate 2 accounts
        for (User u : users.stream().filter(e -> e.getRole_id().equals(2L)).toList()) {
            for (int i = 0; i < 2; i++) {
                Account account = new Account();
                account.setBalance(1000L);
                account.setAccountNumber("TN" + (int) (Math.random() * 1000000));
                account.setStatus(i == 0 ? "Clôturé" : "Ouvert");
                account.setUser_id(u.getId());
                accounts.add(account);
            }
        }

        uow.accounts.saveAll(accounts);

        var operations = new ArrayList<Operation>();
        // for each account generate 2 operations
        var filtred = accounts.stream().filter(e -> e.getStatus().equals("Ouvert")).limit(5).toList();
        var filtered2 = accounts.stream()
                .filter(e -> e.getStatus().equals("Ouvert"))
                .sorted(Comparator.comparing(Account::getId).reversed())
                .limit(5)
                .toList();

        for (int i = 0; i < 5; i++) {

            var debit = filtred.get(i);
            var credit = filtered2.get(i);
            var operation = new Operation();

            operation.setAmount(150L);
            operation.setDescription("Testing data");

            operation.setDate(new Date());
            operation.setOperationType("");

            operation.setAccountDebit_id(debit.getId());
            operation.setAccountCredit_id(filtered2.get(i).getId());

            debit.setBalance(debit.getBalance() - 150);
            credit.setBalance(credit.getBalance() + 150);

            operations.add(operation);
        }

        uow.operations.saveAll(operations);
        uow.accounts.saveAll(filtred);
        uow.accounts.saveAll(filtered2);


        return ResponseEntity.ok(Map.of("message", "Testing data generated successfully"));
    }

}